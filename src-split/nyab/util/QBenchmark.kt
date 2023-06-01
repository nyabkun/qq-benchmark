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

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.system.measureNanoTime

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
@DslMarker
private annotation class QBenchDsl

// << Root of the CallChain >>
fun qBenchmark(action: QBenchmark.() -> Unit) {
    val scope = QBenchmark()

    scope.action()

    scope.start()
}

// << Root of the CallChain >>
@QBenchDsl
class QBenchmark {
    // << Root of the CallChain >>
    private val blocks = mutableListOf<QBlock>()

    // << Root of the CallChain >>
    /**
     * Number of trials
     */
    var nTry: Int = 100

    // << Root of the CallChain >>
    /**
     * If this instance has several [block]s, it will shuffle them in randomized order and measure the time.
     * [nSingleMeasureLoop] represents how many times a block is executed in one measurement.
     * Eventually, the code snippet in the [block] will be executed [nSingleMeasureLoop] * [nTry] times.
     */
    var nSingleMeasureLoop: Int = 1

    // << Root of the CallChain >>
    /**
     * In the early executions, the execution of the [block] takes more time,
     * so we first perform some executions which are not counted in the measurements.
     */
    var nWarmUpTry: Int = 100

    // << Root of the CallChain >>
    var out: QOut = QOut.CONSOLE

    // << Root of the CallChain >>
    /**
     * Set the target code block for time measurement.
     */
    fun block(label: String, task: (callCount: Long) -> Any?) {
        if (nSingleMeasureLoop <= 1) {
            blocks.add(QBlock(label, task))
        } else {
            blocks.add(QBlockLoop(label, nSingleMeasureLoop, task))
        }
    }

    // << Root of the CallChain >>
    /**
     * Set the target code block for time measurement.
     */
    fun block(task: (callCount: Long) -> Any?) {
        block("Block ${blocks.size + 1}", task)
    }
}

// << Root of the CallChain >>
@Suppress("UNCHECKED_CAST")
private fun QBenchmark.start() {
    val blocksProp = this::class.declaredMemberProperties.find { it.name == "blocks" }!!
        as KProperty1<QBenchmark, MutableList<QBlock>>

    blocksProp.isAccessible = true
    val blocks = blocksProp.get(this)

    repeat(nWarmUpTry) {
        for (block in blocks.shuffled()) {
            block.timeItWarmup()
        }
    }

    repeat(nTry) {
        for (block in blocks.shuffled()) {
            block.timeIt()
        }
    }

    val result: String = blocks.joinToString("\n\n")

    val formatted = result.qAlignRight("""\[2]""".re)
        .qAlignRight("""\[3]""".re)
        .qAlignRight("""\[10]""".re)
        .qAlignRight("""\[100]""".re)
        .qAlignRight("""\[1000]""".re)
        .qAlignRight("""\[10000]""".re)
        .qAlignRight("""\[100000]""".re)
        .qAlignRight("""\[1000000]""".re)
        .qAlignRight("""\[10000000]""".re)
        .qAlignRight("""\[100000000]""".re)

    out.println(formatted)

    out.close()
}

// << Root of the CallChain >>
private class QBlockLoop(
    label: String,
    val nSingleMeasureLoop: Int,
    task: (callCount: Long) -> Any?,
) : QBlock(label, { callCount ->
    var firstResult: Any? = null
    for (i in 1..nSingleMeasureLoop) {
        firstResult = task(callCount * (nSingleMeasureLoop - 1) + i)
    }

    firstResult
}) {
    // << Root of the CallChain >>
    override val timeAverage: Double
        get() = timeTotal / (nMeasureTried * nSingleMeasureLoop).toDouble()

    // << Root of the CallChain >>
    override val timeStandardDeviation: Double
        get() = stat.σ / nSingleMeasureLoop

    // << Root of the CallChain >>
    override val timeMedian: Double
        get() = stat.median / nSingleMeasureLoop
}

// << Root of the CallChain >>
private open class QBlock(
    val label: String,
    val task: (callCount: Long) -> Any?,
) {
    // << Root of the CallChain >>
    protected val stat: QOnePassStat = QOnePassStat()

    // << Root of the CallChain >>
    var resultFirst: Any? = Unit

    // << Root of the CallChain >>
    var timeTotal: Long = 0

    // << Root of the CallChain >>
    var nWarmupCall: Long = 0

    // << Root of the CallChain >>
    var nMeasureTried: Long = 0

    // << Root of the CallChain >>
    val nCallTotal: Long
        get() = nWarmupCall + nMeasureTried

    // << Root of the CallChain >>
    val resultsWarmup = mutableListOf<QTimeAndResult>()

    // << Root of the CallChain >>
    open val timeAverage: Double
        get() = timeTotal / nMeasureTried.toDouble()

    // << Root of the CallChain >>
    open val timeStandardDeviation: Double
        get() = stat.σ

    // << Root of the CallChain >>
    open val timeMedian: Double
        get() = stat.median

    // << Root of the CallChain >>
    fun timeItWarmup(): Any? {
        var result: Any?
        val time = measureNanoTime {
            result = task(nWarmupCall)
        }

        nWarmupCall++

        val curTryCount = nWarmupCall.toInt()
        val size = resultsWarmup.size

        var iSize = 3

        if (size < iSize ||
            size < ++iSize && curTryCount % 10 == 0 ||
            size < ++iSize && curTryCount % 100 == 0 ||
            size < ++iSize && curTryCount % 1000 == 0 ||
            size < ++iSize && curTryCount % 10000 == 0 ||
            size < ++iSize && curTryCount % 100000 == 0 ||
            size < ++iSize && curTryCount % 1000000 == 0 ||
            size < ++iSize && curTryCount % 10000000 == 0 ||
            size < ++iSize && curTryCount % 100000000 == 0
        ) {
            resultsWarmup += if (this is QBlockLoop) {
                QTimeAndResult(curTryCount - 1, time, nSingleMeasureLoop, result)
            } else {
                QTimeAndResult(curTryCount - 1, time, 1, result)
            }
        }

        if (nWarmupCall == 1L) {
            resultFirst = result
        }

        return result
    }

    // << Root of the CallChain >>
    fun timeIt(): Any? {
        var result: Any?
        val time = measureNanoTime {
            result = task(nWarmupCall)
        }

        timeTotal += time
        stat.add(time.toDouble())
        nMeasureTried++

        if (nMeasureTried == 1L) {
            resultFirst = result
        }

        return result
    }

    // << Root of the CallChain >>
    fun summary(): String = "$label    [Average]  ${timeAverage.toLong().qFormatDuration()}"

    // << Root of the CallChain >>
    override fun toString(): String {
        val countMulti = if (this is QBlockLoop) {
            this.nSingleMeasureLoop
        } else {
            1
        }

        val measureLoop = if (this is QBlockLoop) {
            "[SingleMeasureLoop]  ${nSingleMeasureLoop.qFormat().trim()}"
        } else {
            "[SingleMeasureLoop]  1"
        }

        // TODO variance / unbiased variance /
        var text = """
${"■".red} ${label.blue}    ${timeAverage.toLong().qFormatDuration().trim().yellow}
${"[ Average ]"}  ${timeAverage.toLong().qFormatDuration().trim()}   [ Median ]  ${timeMedian.qFormatDuration().trim()}
[ Standard Deviation ]  ${(timeStandardDeviation).qFormatDuration().trim()}    [ Total ]  ${
        timeTotal.qFormatDuration().trim()
        }
[ Call Count ]  ${nMeasureTried * countMulti}    [ Call Count (Warmup) ]  ${nWarmupCall * countMulti}
$measureLoop"""
            .qMakeEqualWidth(Regex("""\[(.*?)]"""), align = QAlign.CENTER)
            .qAlignRightAll(Regex("""\[(.*?)]"""), keepLength = false)
            .replace(Regex("""\[(.*?)]"""), "[$1]".green)
            .trimStart()

        if (resultsWarmup.isNotEmpty()) {
            text += "\n - Warmups (The following values do not affect the above measurements) :\n  ".dark_gray + resultsWarmup.joinToString(
                "     "
            ) {
                it.str(false)
            }.dark_gray
        }

        return text
    }
}