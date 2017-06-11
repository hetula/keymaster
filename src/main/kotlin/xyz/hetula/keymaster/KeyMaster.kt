/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.hetula.keymaster

import xyz.hetula.keymaster.device.Device
import xyz.hetula.keymaster.device.DeviceManager
import java.io.DataInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Tuomo Heino
 * @version 11.6.2017.
 */
object KeyMaster {
    var printExceptions = true
    var printSystemOuts = false

    private val LOCK = Object()
    private val CLOSE_INPUT = RawInput(TimeVal(-1, -1), -1, -1, -1)

    private val inputQueue = ArrayBlockingQueue<RawInput>(16) // 16 inputs buffer
    private val monitors = Executors.newCachedThreadPool()
    private val runners = ArrayList<MonitorRunnable>()
    private val listeners = HashSet<(RawInput) -> Unit>()

    fun initialize() {
        monitors.execute(this::runInputs)
    }

    /**
     * Starts monitoring for given input stream.
     * Throws error if input stream cannot be opened!
     */
    fun monitor(name: String, inputEvent: String) {
        monitors.execute(createMonitor(
                name,
                openInput(inputEvent)
                        ?: throw KeyMasterException("No permission for '$inputEvent'! Is user member of input group?")))
    }

    fun release() {
        inputQueue.add(CLOSE_INPUT)
        val it = runners.iterator()
        while (it.hasNext()) {
            it.next().shutdown()
            it.remove()
        }
        monitors.shutdown()
        monitors.awaitTermination(5, TimeUnit.SECONDS)
        monitors.shutdownNow()
    }

    fun registerInputListener(listener: (RawInput) -> Unit) {
        synchronized(LOCK, { listeners.add(listener) })
    }

    fun unregisterInputListener(listener: (RawInput) -> Unit) {
        synchronized(LOCK, { listeners.remove(listener) })
    }

    private fun createMonitor(device: String, dataIn: DataInputStream): MonitorRunnable {
        println("Creating monitor: $device")
        val monitor = MonitorRunnable(device, dataIn, { inputQueue.add(it) })
        runners.add(monitor)
        return monitor
    }

    private fun openInput(inputEvent: String): DataInputStream? {
        try {
            return DataInputStream(Files.newInputStream(Paths.get(inputEvent)))
        } catch (ex: IOException) {
            if (printExceptions) {
                ex.printStackTrace()
            }
            return null
        }
    }

    private fun runInputs() {
        while (true) {
            val input = inputQueue.take()
            if (input == CLOSE_INPUT) break
            synchronized(LOCK, { listeners.forEach { it(input) } })
        }
    }
}

fun main(args: Array<String>) {
    KeyMaster.initialize()
    KeyMaster.registerInputListener { println(it) }
    DeviceManager.readDevices().forEach(::monitorDevice)
    println("Testing for 10 seconds!")
    Thread.sleep(10000)
    println("Closing!")
    KeyMaster.release()
}

private fun monitorDevice(device: Device) {
    if (!device.isKeyboard()) return
    val event = device.getEventBus()
    if (event.isEmpty()) return
    println("Registering: $device")
    KeyMaster.monitor(device.name, event)
}