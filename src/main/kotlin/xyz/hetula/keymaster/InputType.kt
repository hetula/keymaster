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

/**
 * @author Tuomo Heino
 * @version 11.6.2017.
 */
enum class InputType(val type: Int) {
    EV_SYN(0x00),
    EV_KEY(0x01),
    EV_REL(0x02),
    EV_ABS(0x03),
    EV_MSC(0x04),
    EV_SW(0x05),
    EV_LED(0x11),
    EV_SND(0x12),
    EV_REP(0x14),
    EV_FF(0x15),
    EV_PWR(0x16),
    EV_FF_STATUS(0x17),
    EV_MAX(0x1f),
    EV_CNT(InputType.EV_MAX.type + 1);

    companion object {
        fun toInputType(type: Int): InputType {
            when(type) {
                InputType.EV_SYN.type -> return InputType.EV_SYN
                InputType.EV_KEY.type -> return InputType.EV_KEY
                InputType.EV_REL.type -> return InputType.EV_REL
                InputType.EV_ABS.type -> return InputType.EV_ABS
                InputType.EV_MSC.type -> return InputType.EV_MSC
                InputType.EV_SW.type  -> return InputType.EV_SW
                InputType.EV_LED.type -> return InputType.EV_LED
                InputType.EV_SND.type -> return InputType.EV_SND
                InputType.EV_REP.type -> return InputType.EV_REP
                InputType.EV_FF.type  -> return InputType.EV_FF
                InputType.EV_PWR.type -> return InputType.EV_PWR
                InputType.EV_MAX.type -> return InputType.EV_MAX
                InputType.EV_CNT.type -> return InputType.EV_CNT
                InputType.EV_FF_STATUS.type -> return InputType.EV_FF_STATUS
                else -> throw IllegalArgumentException("No such InputType! -> " + type)
            }
        }
    }
}