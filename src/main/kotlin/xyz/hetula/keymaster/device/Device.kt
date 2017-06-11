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

import xyz.hetula.keymaster.InputType

/**
 * @author Tuomo Heino
 * @version 11.6.2017.
 */
data class Device(val id: DeviceId,
                  val name: String,
                  val physPath: String,
                  val sysfs: String,
                  val uniqId: String,
                  val handlers: Set<String>,
                  val bitmaps: Map<String, List<String>>
) {
    fun isValid(): Boolean {
        return !sysfs.isEmpty()
    }

    fun getEVBitmap(): Int {
        val evVal = bitmaps["EV"]
        if(evVal == null || evVal.isEmpty()) return -1
        return Integer.parseInt(evVal[0], 16)
    }

    fun getEventBus(): String {
        return handlers
                .firstOrNull { it.startsWith("event") }
                ?.let { "/dev/input/"+ it }
                ?: ""
    }

    fun isKeyboard(): Boolean {
        val ev = getEVBitmap()
        if(ev == -1) return false
        return testBit(ev, InputType.EV_SYN) &&
                testBit(ev, InputType.EV_KEY) &&
                testBit(ev, InputType.EV_MSC) &&
                testBit(ev, InputType.EV_REP)

    }

    private fun testBit(value: Int, bit: InputType): Boolean {
        return value and (1 shl bit.type) != 0
    }
}