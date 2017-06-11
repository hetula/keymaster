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

package xyz.hetula.keymaster.device

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Tuomo Heino
 * @version 11.6.2017.
 */
object DeviceManager {

    fun readDevices(): List<Device> {
        val ins = Files.readAllLines(Paths.get("/proc/bus/input/devices"))
        val devices = ArrayList<Device>()
        val lineBuffer = ArrayList<String>()
        for(item in ins) {
            if(item.isEmpty()) {
                val dev = parseDevice(lineBuffer)
                if(dev.isValid()) {
                    devices.add(dev)
                }
                lineBuffer.clear()
            } else {
                lineBuffer.add(item)
            }
        }
        return devices
    }

    private fun parseDevice(lines: ArrayList<String>): Device {
        var id: DeviceId = DeviceId(0,0,0,0)
        var name = ""
        var physPath = ""
        var sysfs = ""
        var uniqId = ""
        val handlers = HashSet<String>()
        val bitmaps = HashMap<String, List<String>>()

        for(line in lines) {
            when(line[0]) {
                'I' -> id = parseDeviceId(getVal(line))
                'N' -> name = getVal(line).drop("Name=".length+1).dropLast(1)
                'P' -> physPath = getVal(line).drop("Phys=".length)
                'S' -> sysfs = getVal(line).drop("Sysfs=".length)
                'U' -> uniqId = getVal(line).drop("Uniq=".length)
                'H' -> parseHandlers(getVal(line), handlers)
                'B' -> parseBitmap(getVal(line), bitmaps)
            }
        }

        return Device(
                id,
                name,
                physPath,
                sysfs,
                uniqId,
                handlers,
                bitmaps
        )
    }

    private fun getVal(line: String): String {
        return line.drop(3)
    }

    private fun parseDeviceId(idString: String): DeviceId {
        val splits = idString.split(' ')
        var bus     = "0000"
        var vendor  = "0000"
        var product = "0000"
        var version = "0000"

        for(idPart in splits) {
            when(idPart.takeWhile { it != '=' }) {
                "Bus"     -> bus     = idPart.takeLast(4)
                "Vendor"  -> vendor  = idPart.takeLast(4)
                "Product" -> product = idPart.takeLast(4)
                "Version" -> version  = idPart.takeLast(4)
            }
        }

        return DeviceId(
                Integer.parseInt(bus,     16),
                Integer.parseInt(vendor,  16),
                Integer.parseInt(product, 16),
                Integer.parseInt(version, 16)
        )
    }

    private fun parseHandlers(value: String, handlers: MutableSet<String>) {
        val list = value.drop(9)
        if(!list.isEmpty()) {
            list.split(' ').filterNotTo(handlers) { it.isEmpty() }
        }
    }

    private fun parseBitmap(value: String, bitmaps: MutableMap<String, List<String>>) {
        val splitted = value.split('=', limit = 2)
        val bits = ArrayList<String>()
        splitted[1].split(' ').mapTo(bits) { it }
        bitmaps.put(splitted[0], bits)
    }
}