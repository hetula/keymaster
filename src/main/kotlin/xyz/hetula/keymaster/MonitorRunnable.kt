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

import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException

/**
 * @author Tuomo Heino
 * @version 11.6.2017.
 */
class MonitorRunnable(private val device: String, private val dataIn: DataInputStream,
                      private val onEvent: (RawInput) -> Unit): Runnable {

    fun shutdown() {
        try {
            dataIn.close()
        } catch (ignored: Exception) {
            // Not interested...
        }
    }

    override fun run() {
        dataIn.use { dataIn ->
            val buffer = ByteArray(24)
            while (true) {
                val read = read(buffer, dataIn)
                if(read <= 0) break
                if (read != 24) continue
                val raw = toRawInput(buffer)
                if (raw.type != InputType.EV_KEY.type) continue
                if(KeyMaster.printSystemOuts) {
                    println("From device: $device")
                    println(raw)
                    println(raw.toKeyEvent())
                }
                onEvent(raw)
            }
            if(KeyMaster.printSystemOuts) {
                println("Monitor over! " + device)
            }
        }
    }

    private fun read(buffer: ByteArray, dataIn: DataInputStream): Int {
        try {
            return dataIn.read(buffer, 0, 24)
        } catch (ignored: AsynchronousCloseException) {
            // Occurs when closing
            return -1
        } catch (iex: IOException) {
            if(KeyMaster.printExceptions) {
                iex.printStackTrace()
            }
            return -2
        }
    }

    private fun toRawInput(buffer: ByteArray): RawInput {
        return RawInput(
                getTimeVal(buffer),
                ByteBuffer.wrap(buffer, 15, 2).short.toInt(),
                ByteBuffer.wrap(buffer, 17, 2).short.toInt(),
                ByteBuffer.wrap(buffer.copyOfRange(20, 24).reversedArray(), 0, 4).int
        )
    }

    private fun getTimeVal(buffer: ByteArray): TimeVal {
        return TimeVal(
                ByteBuffer.wrap(buffer, 0, 8).long,
                ByteBuffer.wrap(buffer, 8, 8).long
        )
    }
}