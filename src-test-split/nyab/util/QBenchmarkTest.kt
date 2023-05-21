/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.util

import nyab.test.QTestHumanCheckRequired
import nyab.test.qTestHumanCheck

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
fun main() {
    qTestHumanCheck()
}

// << Root of the CallChain >>
class QBenchmarkTest {
    // << Root of the CallChain >>
    @QTestHumanCheckRequired
    fun stringConcatenation() {
        qBenchmark {
            // Number of trials
            nTry = 500

            // If this instance has several [block]s, it will shuffle them in randomized order and measure the time.
            // [nSingleMeasureLoop] represents how many times a block is executed in one measurement.
            // Eventually, the code snippet in the [block] will be executed [nSingleMeasureLoop] * [nTry] times.
            nSingleMeasureLoop = 5

            // In the early executions, the execution of the [block] takes more time,
            // so we first perform some executions which are not counted in the measurements.
            nWarmUpTry = 50

            block("Raw String Concat") {
                var str = ""
                for (i in 1..3000) {
                    str += i.toString()
                }
                str
            }

            block("StringBuilder") {
                val sb = StringBuilder()
                for (i in 1..3000) {
                    sb += i.toString()
                }
                sb.toString()
            }

            block("StringBuffer") {
                val sb = StringBuffer()
                for (i in 1..3000) {
                    sb.append(i.toString())
                }
                sb.toString()
            }
        }
    }

    // << Root of the CallChain >>
    @QTestHumanCheckRequired
    fun cachedRegex() {
        val testRegex =
            """https?://(www\.)?[-a-zA-Z\d@:%._+~#=]{1,256}\.[a-zA-Z\d()]{1,6}\b([-a-zA-Z\d()@:%_+.~#?&/=]*)"""

        qBenchmark {
            nTry = 1000
            nWarmUpTry = 1000
            nSingleMeasureLoop = 10
            out = QOut.CONSOLE

            block("no cache") {
                Regex(testRegex)
            }

            block("with cache (Thread Safe)") {
                qCacheItOneSec(testRegex) { Regex(testRegex) }
            }

            block("with cache (Thread Local)") {
                qCacheItOneSecThreadLocal(testRegex) { Regex(testRegex) }
            }
        }
    }
}