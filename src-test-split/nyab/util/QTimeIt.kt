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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=5] = QTimeAndResult <-[Call]- QBlock.timeItWarmup() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QTimeAndResult(val index: Int, val time: Long, val nTry: Int = 1, val result: Any? = null) {
    // CallChain[size=5] = QTimeAndResult.str() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun str(printResult: Boolean, resultMaxLength: Int = 15): String {
        return if (result == Unit || !printResult) {
            "[${index + 1}]  ${(time / nTry.toDouble()).qFormatDuration().trim()}"
        } else {
            "[${index + 1}]  ${(time / nTry.toDouble()).qFormatDuration().trim()}   ${result?.toString()?.qWithMaxLength(resultMaxLength) ?: "null"}"
        }
    }
}

// CallChain[size=5] = qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@OptIn(ExperimentalContracts::class)
internal inline fun <T> qTimeIt(label: String = qThisSrcLineSignature, quiet: Boolean = false, out: QOut? = QOut.CONSOLE, block: () -> T): QTimeItResult<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val start = System.nanoTime()

    val blockResult = block()

    val time = System.nanoTime() - start

    val result = QTimeItResult(label, time, blockResult)

    if (!quiet)
        out?.println(result.toString())

    return result
}

// CallChain[size=6] = QTimeItResult <-[Ref]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal class QTimeItResult<T>(val label: String, val time: Long, val result: T) {
    // CallChain[size=6] = QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun toString(): String {
        return qBrackets(label, time.qFormatDuration())
    }
}