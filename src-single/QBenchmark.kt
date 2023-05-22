/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("UNCHECKED_CAST")

import com.sun.nio.file.ExtendedOpenOption
import java.io.LineNumberReader
import java.io.PrintStream
import java.lang.StackWalker.StackFrame
import java.nio.charset.Charset
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream
import kotlin.concurrent.withLock
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.name
import kotlin.io.path.reader
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberExtensionFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.streams.asSequence
import kotlin.system.measureNanoTime
import org.intellij.lang.annotations.Language


// qq-benchmark is self-contained single-file library created by nyabkun.
//  - It can be added to your codebase with a simple copy and paste.
//  - You can modify and redistribute it under the MIT License.
//  - Please add a package declaration if necessary.

// << Root of the CallChain >>
@DslMarker
annotation class QBenchDsl

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


// region Src from Non-Root API Files -- Auto Generated by nyab.conf.QCompactLib.kt
// ================================================================================


// CallChain[size=5] = QMyException <-[Ref]- QE <-[Ref]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QMyException {
    // CallChain[size=6] = QMyException.Other <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Other,

    // CallChain[size=12] = QMyException.Unreachable <-[Call]- qUnreachable() <-[Call]- QFetchRule.SINGL ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Unreachable,
    // CallChain[size=13] = QMyException.ShouldNotBeNull <-[Call]- T?.qaNotNull() <-[Call]- qSrcFileAtFr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    ShouldNotBeNull,
    // CallChain[size=8] = QMyException.ShouldNotBeZero <-[Call]- Int?.qaNotZero() <-[Call]- CharSequenc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    ShouldNotBeZero,
    // CallChain[size=11] = QMyException.ShouldBeEvenNumber <-[Call]- qBrackets() <-[Call]- qMySrcLinesA ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    ShouldBeEvenNumber,
    // CallChain[size=12] = QMyException.FileNotFound <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLine ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    FileNotFound,
    // CallChain[size=12] = QMyException.FetchLinesFail <-[Call]- Path.qFetchLinesAround() <-[Call]- qSr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    FetchLinesFail,
    // CallChain[size=13] = QMyException.LineNumberExceedsMaximum <-[Call]- Path.qLineAt() <-[Call]- Pat ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LineNumberExceedsMaximum,
    // CallChain[size=4] = QMyException.IllegalArgument <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    IllegalArgument;
    companion object {
        // Required to implement extended functions.

        // CallChain[size=6] = QMyException.STACK_FRAME_FILTER <-[Call]- QException.QException() <-[Ref]- QE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val STACK_FRAME_FILTER: (StackWalker.StackFrame) -> Boolean = {
            !it.className.startsWith("org.gradle") &&
                !it.className.startsWith("org.testng") &&
                !it.className.startsWith("worker.org.gradle") &&
                !it.methodName.endsWith("\$default") && it.fileName != null &&
//            && !it.className.startsWith(QException::class.qualifiedName!!)
//            && it.methodName != "invokeSuspend"
                it.declaringClass != null
//            && it.declaringClass.canonicalName != null
//            && !it.declaringClass.canonicalName.startsWith("kotlin.coroutines.jvm.internal.")
//            && !it.declaringClass.canonicalName.startsWith("kotlinx.coroutines")
        }

        
    }
}

// CallChain[size=10] = QMyLog <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QExcept ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMyLog {
    // CallChain[size=10] = QMyLog.out <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /**
     * Default output stream
     */
    val out: QOut = QOut.CONSOLE

    // CallChain[size=6] = QMyLog.no_format <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var no_format: Boolean = false
}

// CallChain[size=6] = QMyMark <-[Ref]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMyMark {
    // CallChain[size=6] = QMyMark.WARN <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val WARN = "⚠".yellow
    
}

// CallChain[size=9] = QMyPath <-[Ref]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Call ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMyPath {
    // -- dirs

    // CallChain[size=10] = QMyPath.src <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src = "src".path
    // CallChain[size=10] = QMyPath.src_java <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_java = "src-java".path
    // CallChain[size=10] = QMyPath.src_build <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_build = "src-build".path
    // CallChain[size=10] = QMyPath.src_experiment <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_experiment = "src-experiment".path
    // CallChain[size=10] = QMyPath.src_plugin <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_plugin = "src-plugin".path
    // CallChain[size=10] = QMyPath.src_config <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_config = "src-config".path
    // CallChain[size=10] = QMyPath.src_test <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_test = "src-test".path
    // --- dir list
    // CallChain[size=9] = QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndSta ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val src_root: List<Path> by lazy {
        val base = listOf(
            src,
            src_test,
            src_experiment,
            src_config,
            src_plugin,
            src_java,
            src_build,
            "src".path,
            "test".path,
            "src/main/kotlin".path,
            "src/test/kotlin".path,
            "src/main/java".path,
            "src/test/java".path,
//            ".".path,
        ).filter { it.exists() }

        val search = Paths.get(".").qListByMatch(type = QFType.Dir, nameMatch = QM.startsWith("src-"), maxDepth = 1)

        (base + search).distinct()
    }

    // -- files
}

// CallChain[size=8] = QMyToString <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMyToString {
    
}

// CallChain[size=4] = QE <-[Ref]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private typealias QE = QMyException

// CallChain[size=14] = String.qMatches() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qMatches(matcher: QM): Boolean = matcher.matches(this)

// CallChain[size=14] = QMatchAny <-[Call]- QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchAny : QM {
    // CallChain[size=15] = QMatchAny.matches() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]- Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return true
    }

    // CallChain[size=15] = QMatchAny.toString() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=14] = QMatchNone <-[Call]- QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchNone : QM {
    // CallChain[size=15] = QMatchNone.matches() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return false
    }

    // CallChain[size=15] = QMatchNone.toString() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=13] = QM <-[Ref]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private interface QM {
    // CallChain[size=13] = QM.matches() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun matches(text: String): Boolean

    // CallChain[size=13] = QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isAny(): Boolean = this == QMatchAny

    // CallChain[size=13] = QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isNone(): Boolean = this == QMatchNone

    companion object {
        // CallChain[size=12] = QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun exact(text: String, ignoreCase: Boolean = false): QM = QExactMatch(text, ignoreCase)

        // CallChain[size=10] = QM.startsWith() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun startsWith(text: String, ignoreCase: Boolean = false): QM = QStartsWithMatch(text, ignoreCase)

        
    }
}

// CallChain[size=13] = QExactMatch <-[Call]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcF ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QExactMatch(val textExact: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=14] = QExactMatch.matches() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call]- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return text.equals(textExact, ignoreCase)
    }

    // CallChain[size=14] = QExactMatch.toString() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textExact=$textExact, ignoreCase=$ignoreCase)"
    }
}

// CallChain[size=11] = QStartsWithMatch <-[Call]- QM.startsWith() <-[Call]- QMyPath.src_root <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QStartsWithMatch(val textStartsWith: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=12] = QStartsWithMatch.matches() <-[Propag]- QStartsWithMatch <-[Call]- QM.startsW ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return text.startsWith(textStartsWith, ignoreCase)
    }

    // CallChain[size=12] = QStartsWithMatch.toString() <-[Propag]- QStartsWithMatch <-[Call]- QM.starts ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textStartsWith=$textStartsWith, ignoreCase=$ignoreCase)"
    }
}

// CallChain[size=9] = qAnd() <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qAnd(vararg matches: QMFunc): QMFunc = QMatchFuncAnd(*matches)

// CallChain[size=8] = QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private infix fun QMFunc.and(match: QMFunc): QMFunc {
    return if (this is QMatchFuncAnd) {
        QMatchFuncAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=10] = QMatchFuncAny <-[Call]- QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsI ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncAny : QMFuncA() {
    // CallChain[size=11] = QMatchFuncAny.matches() <-[Propag]- QMatchFuncAny <-[Call]- QMFunc.isAny() < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=10] = QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtension ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncNone : QMFuncA() {
    // CallChain[size=11] = QMatchFuncNone.matches() <-[Propag]- QMatchFuncNone <-[Call]- QMFunc.isNone( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return false
    }
}

// CallChain[size=9] = QMatchFuncDeclaredOnly <-[Call]- QMFunc.DeclaredOnly <-[Call]- qToStringRegis ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncDeclaredOnly : QMFuncA() {
    // CallChain[size=10] = QMatchFuncDeclaredOnly.declaredOnly <-[Propag]- QMatchFuncDeclaredOnly <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly = true

    // CallChain[size=10] = QMatchFuncDeclaredOnly.matches() <-[Propag]- QMatchFuncDeclaredOnly <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=9] = QMatchFuncIncludeExtensionsInClass <-[Call]- QMFunc.IncludeExtensionsInClass  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncIncludeExtensionsInClass : QMFuncA() {
    // CallChain[size=10] = QMatchFuncIncludeExtensionsInClass.includeExtensionsInClass <-[Propag]- QMat ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass = true

    // CallChain[size=10] = QMatchFuncIncludeExtensionsInClass.matches() <-[Propag]- QMatchFuncIncludeEx ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=9] = QMatchFuncAnd <-[Ref]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMatchFuncAnd(vararg match: QMFunc) : QMFuncA() {
    // CallChain[size=9] = QMatchFuncAnd.matchList <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val matchList = match

    // CallChain[size=10] = QMatchFuncAnd.declaredOnly <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMF ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=10] = QMatchFuncAnd.includeExtensionsInClass <-[Propag]- QMatchFuncAnd.matchList < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass: Boolean = matchList.any { it.includeExtensionsInClass }

    // CallChain[size=10] = QMatchFuncAnd.matches() <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFunc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=11] = QMFuncA <-[Call]- QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFun ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private abstract class QMFuncA : QMFunc {
    // CallChain[size=12] = QMFuncA.declaredOnly <-[Propag]- QMFuncA <-[Call]- QMatchFuncNone <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly: Boolean = false
    // CallChain[size=12] = QMFuncA.includeExtensionsInClass <-[Propag]- QMFuncA <-[Call]- QMatchFuncNon ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass: Boolean = false
}

// CallChain[size=9] = QMFunc <-[Ref]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private interface QMFunc {
    // CallChain[size=9] = QMFunc.declaredOnly <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qTo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val declaredOnly: Boolean

    // CallChain[size=9] = QMFunc.includeExtensionsInClass <-[Propag]- QMFunc.IncludeExtensionsInClass < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val includeExtensionsInClass: Boolean

    // CallChain[size=9] = QMFunc.matches() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun matches(value: KFunction<*>): Boolean

    // CallChain[size=9] = QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStrin ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isAny(): Boolean = this == QMatchFuncAny

    // CallChain[size=9] = QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isNone(): Boolean = this == QMatchFuncNone

    companion object {
        // CallChain[size=8] = QMFunc.DeclaredOnly <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val DeclaredOnly: QMFunc = QMatchFuncDeclaredOnly

        // CallChain[size=8] = QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry <-[Call]- Any?.qT ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        // TODO OnlyExtensionsInClass
        val IncludeExtensionsInClass: QMFunc = QMatchFuncIncludeExtensionsInClass

        

        // TODO vararg, nullability, param names, type parameter
        // TODO handle createType() more carefully

        // CallChain[size=8] = QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun nameExact(text: String, ignoreCase: Boolean = false): QMFunc {
            return QMatchFuncName(QM.exact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=9] = QMatchFuncName <-[Call]- QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMatchFuncName(val nameMatcher: QM) : QMFuncA() {
    // CallChain[size=10] = QMatchFuncName.matches() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return nameMatcher.matches(value.name)
    }

    // CallChain[size=10] = QMatchFuncName.toString() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameEx ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName + ":" + nameMatcher.toString()
    }
}

// CallChain[size=13] = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL <-[Call]- QCacheMap.QCacheMap()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL = 1000L

// CallChain[size=11] = qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOne ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qThreadLocalCache: ThreadLocal<QCacheMap> by lazy {
    ThreadLocal.withInitial {
        QCacheMap(
            qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL
        )
    }
}

// CallChain[size=12] = qCacheThreadSafe <-[Call]- qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qCacheThreadSafe: QCacheMap by lazy { QCacheMap(qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL, true) }

// CallChain[size=10] = qCacheItOneSec() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFrames()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <K : Any, V : Any?> qCacheItOneSec(key: K, block: () -> V): V = qCacheItTimed(key, 1000L, block)

// CallChain[size=11] = qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- qMySrcLinesAtFrame() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <K : Any, V : Any?> qCacheItTimed(key: K, duration: Long, block: () -> V): V =
    qCacheThreadSafe.getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=9] = qCacheItOneSecThreadLocal() <-[Call]- qRe() <-[Call]- QException.mySrcAndStac ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <K : Any, V : Any> qCacheItOneSecThreadLocal(key: K, block: () -> V): V =
    qCacheItTimedThreadLocal(key, 1000L, block)

// CallChain[size=10] = qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <K : Any, V : Any> qCacheItTimedThreadLocal(key: K, duration: Long, block: () -> V): V =
    qThreadLocalCache.get().getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=12] = QCacheMap <-[Ref]- qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QCacheMap(
    val expirationCheckInterval: Long = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL,
    val threadSafe: Boolean = false
) {
    // CallChain[size=12] = QCacheMap.lastCheck <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedTh ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var lastCheck: Long = -1
    // CallChain[size=12] = QCacheMap.lock <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadL ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val lock: ReentrantLock = ReentrantLock()
    // CallChain[size=12] = QCacheMap.map <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val map: MutableMap<Any, QCacheEntry> = mutableMapOf()

    // CallChain[size=12] = QCacheMap.clearExpired() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTi ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun clearExpired(): Int = lock.qWithLock(threadSafe) {
        val toRemove = map.filterValues { it.isExpired() }
        toRemove.forEach { map.remove(it.key) }
        return toRemove.count()
    }

    // CallChain[size=11] = QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheIt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun getOrPut(key: Any, defaultValue: () -> QCacheEntry): QCacheEntry = lock.qWithLock(threadSafe) {
        val now = qNow
        if (now - lastCheck > expirationCheckInterval) {
            lastCheck = now
            clearExpired()
        }

        map.getOrPut(key, defaultValue)
    }
}

// CallChain[size=11] = QCacheEntry <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private data class QCacheEntry(val value: Any?, val duration: Long, val creationTime: Long = qNow) {
    // CallChain[size=13] = QCacheEntry.isExpired() <-[Call]- QCacheMap.clearExpired() <-[Call]- QCacheM ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isExpired() = (qNow - creationTime) > duration
}

// CallChain[size=12] = Lock.qWithLock() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThrea ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private inline fun <T> Lock.qWithLock(threadSafe: Boolean, block: () -> T): T {
    return if (threadSafe) {
        withLock(block)
    } else {
        block()
    }
}

// CallChain[size=4] = QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun QE.throwIt(msg: Any? = "", e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(
        this,
        if (msg is String && msg.isEmpty()) {
            "No detailed error messages".light_gray
        } else {
            msg.qToLogString()
        },
        e, stackDepth = stackDepth + 1
    )
}

// CallChain[size=13] = QE.throwItFile() <-[Call]- LineNumberReader.qFetchLinesAround() <-[Call]- Pa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun QE.throwItFile(path: Path, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets("File", path.absolutePathString()), e, stackDepth = stackDepth + 1)
}

// CallChain[size=11] = QE.throwItBrackets() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAtFrame() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun QE.throwItBrackets(vararg keysAndValues: Any?, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets(*keysAndValues), e, stackDepth = stackDepth + 1)
}

// CallChain[size=11] = qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qUnreachable(msg: Any? = ""): Nothing {
    QE.Unreachable.throwIt(msg)
}

// CallChain[size=5] = QException <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QException(
    val type: QE = QE.Other,
    msg: String = QMyMark.WARN,
    e: Throwable? = null,
    val stackDepth: Int = 0,
    stackSize: Int = 20,
    stackFilter: (StackWalker.StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
    private val srcCut: QSrcCut = QSrcCut.MULTILINE_NOCUT,
) : RuntimeException(msg, e) {

    // CallChain[size=6] = QException.printStackTrace() <-[Propag]- QException.QException() <-[Ref]- QE. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun printStackTrace(s: PrintStream) {
        s.println("\n" + qToString() + "\n" + mySrcAndStack)
    }

    // CallChain[size=7] = QException.stackFrames <-[Call]- QException.getStackTrace() <-[Propag]- QExce ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val stackFrames = qStackFrames(stackDepth + 2, size = stackSize, filter = stackFilter)

    // CallChain[size=7] = QException.mySrcAndStack <-[Call]- QException.printStackTrace() <-[Propag]- Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val mySrcAndStack: String by lazy {
        qLogStackFrames(frames = stackFrames, style = QLogStyle.SRC_AND_STACK, srcCut = srcCut, quiet = true)
            .qColorTarget(qRe("""\sshould[a-zA-Z]+"""), QShColor.LIGHT_YELLOW)
    }

    // CallChain[size=6] = QException.getStackTrace() <-[Propag]- QException.QException() <-[Ref]- QE.th ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun getStackTrace(): Array<StackTraceElement> {
        return stackFrames.map {
            it.toStackTraceElement()
        }.toTypedArray()
    }

    // CallChain[size=7] = QException.qToString() <-[Call]- QException.toString() <-[Propag]- QException ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun qToString(): String {
        val msg = message

        return if (!msg.isNullOrEmpty() && type.name != message) {
            "${type.name.yellow} ${":".yellow}${
            msg.qWithSpacePrefix(onlyIf = QOnlyIfStr.SingleLine).qWithNewLinePrefix(onlyIf = QOnlyIfStr.Multiline)
            }".trim()
        } else {
            type.name.yellow
        }
    }

    // CallChain[size=6] = QException.toString() <-[Propag]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return qToString()
//         used by @Test
//        return type.name.yellow
    }
}

// CallChain[size=12] = T?.qaNotNull() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <T : Any> T?.qaNotNull(exceptionType: QE = QE.ShouldNotBeNull, msg: Any? = ""): T {
    if (this != null) {
        return this
    } else {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    }
}

// CallChain[size=7] = Int?.qaNotZero() <-[Call]- CharSequence.qMask() <-[Call]- Any?.qToLogString() ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Int?.qaNotZero(exceptionType: QE = QE.ShouldNotBeZero, msg: Any? = ""): Int {
    if (this == null) {
        QE.ShouldNotBeNull.throwIt(stackDepth = 1, msg = msg)
    } else if (this == 0) {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    } else {
        return this
    }
}

// CallChain[size=13] = qBUFFER_SIZE <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qBUFFER_SIZE = DEFAULT_BUFFER_SIZE

// CallChain[size=13] = QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
// @formatter:off
private enum class QOpenOpt(val opt: OpenOption) : QFlagEnum<QOpenOpt> {
    // CallChain[size=15] = QOpenOpt.TRUNCATE_EXISTING <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    TRUNCATE_EXISTING(StandardOpenOption.TRUNCATE_EXISTING),
    // CallChain[size=15] = QOpenOpt.CREATE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    CREATE(StandardOpenOption.CREATE),
    // CallChain[size=15] = QOpenOpt.CREATE_NEW <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    CREATE_NEW(StandardOpenOption.CREATE_NEW),
    // CallChain[size=15] = QOpenOpt.WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    WRITE(StandardOpenOption.WRITE),
    // CallChain[size=15] = QOpenOpt.READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    READ(StandardOpenOption.READ),
    // CallChain[size=15] = QOpenOpt.APPEND <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    APPEND(StandardOpenOption.APPEND),
    // CallChain[size=15] = QOpenOpt.DELETE_ON_CLOSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DELETE_ON_CLOSE(StandardOpenOption.DELETE_ON_CLOSE),
    // CallChain[size=15] = QOpenOpt.DSYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DSYNC(StandardOpenOption.DSYNC),
    // CallChain[size=15] = QOpenOpt.SYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    SYNC(StandardOpenOption.SYNC),
    // CallChain[size=15] = QOpenOpt.SPARSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    SPARSE(StandardOpenOption.SPARSE),
    // CallChain[size=15] = QOpenOpt.EX_DIRECT <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt> ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    EX_DIRECT(ExtendedOpenOption.DIRECT),
    // CallChain[size=15] = QOpenOpt.EX_NOSHARE_DELETE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    EX_NOSHARE_DELETE(ExtendedOpenOption.NOSHARE_DELETE),
    // CallChain[size=15] = QOpenOpt.EX_NOSHARE_READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    EX_NOSHARE_READ(ExtendedOpenOption.NOSHARE_READ),
    // CallChain[size=15] = QOpenOpt.EX_NOSHARE_WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QO ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    EX_NOSHARE_WRITE(ExtendedOpenOption.NOSHARE_WRITE),
    // CallChain[size=15] = QOpenOpt.LN_NOFOLLOW_LINKS <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LN_NOFOLLOW_LINKS(LinkOption.NOFOLLOW_LINKS);

    companion object {
        
    }
}

// CallChain[size=13] = QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.qReader() <-[Call]- Path.qFetchL ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun QFlag<QOpenOpt>.toOptEnums(): Array<OpenOption> {
    return toEnumValues().map { it.opt }.toTypedArray()
}

// CallChain[size=12] = Path.qLineSeparator() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileL ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qLineSeparator(charset: Charset = Charsets.UTF_8): QLineSeparator {
    this.bufferedReader(charset).use { reader ->
        var ch: Char

        while (true) {
            ch = reader.read().toChar()

            if (ch == '\u0000') return QLineSeparator.DEFAULT

            if (ch == '\r') {
                val nextCh = reader.read().toChar()

                if (nextCh == '\u0000') return QLineSeparator.CR

                return if (nextCh == '\n') QLineSeparator.CRLF
                else QLineSeparator.CR
            } else if (ch == '\n') {
                return QLineSeparator.LF
            }
        }
    }
}

// CallChain[size=11] = QFetchEnd <-[Ref]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QFetchEnd {
    // CallChain[size=12] = QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE,
    // CallChain[size=11] = QFetchEnd.END_WITH_THIS_LINE <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    END_WITH_THIS_LINE,
    // CallChain[size=12] = QFetchEnd.END_WITH_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    END_WITH_NEXT_LINE,
    // CallChain[size=12] = QFetchEnd.END_WITH_PREVIOUS_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    END_WITH_PREVIOUS_LINE
}

// CallChain[size=11] = QFetchStart <-[Ref]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QFetchStart {
    // CallChain[size=11] = QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE <-[Call]- QFetchRule.SIN ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE,
    // CallChain[size=11] = QFetchStart.START_FROM_THIS_LINE <-[Call]- QFetchRule.SINGLE_LINE <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    START_FROM_THIS_LINE,
    // CallChain[size=12] = QFetchStart.START_FROM_NEXT_LINE <-[Propag]- QFetchStart.FETCH_THIS_LINE_AND ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    START_FROM_NEXT_LINE,
    // CallChain[size=12] = QFetchStart.START_FROM_PREVIOUS_LINE <-[Propag]- QFetchStart.FETCH_THIS_LINE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    START_FROM_PREVIOUS_LINE
}

// CallChain[size=11] = QFetchRuleA <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private abstract class QFetchRuleA(
        override val numLinesBeforeTargetLine: Int = 10,
        override val numLinesAfterTargetLine: Int = 10,
) : QFetchRule

// CallChain[size=10] = QFetchRule <-[Ref]- QSrcCut.QSrcCut() <-[Call]- qLogStackFrames() <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private interface QFetchRule {
    // CallChain[size=11] = QFetchRule.numLinesBeforeTargetLine <-[Propag]- QFetchRule.SINGLE_LINE <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val numLinesBeforeTargetLine: Int
    // CallChain[size=11] = QFetchRule.numLinesAfterTargetLine <-[Propag]- QFetchRule.SINGLE_LINE <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val numLinesAfterTargetLine: Int

    // CallChain[size=11] = QFetchRule.fetchStartCheck() <-[Propag]- QFetchRule.SINGLE_LINE <-[Call]- QS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun fetchStartCheck(
            line: String,
            currentLineNumber: Int,
            targetLine: String,
            targetLineNumber: Int,
            context: MutableSet<String>,
    ): QFetchStart

    // CallChain[size=11] = QFetchRule.fetchEndCheck() <-[Propag]- QFetchRule.SINGLE_LINE <-[Call]- QSrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun fetchEndCheck(
            line: String,
            currentLineNumber: Int,
            targetLine: String,
            targetLineNumber: Int,
            context: MutableSet<String>,
    ): QFetchEnd

    companion object {
        // CallChain[size=10] = QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[Call]- qLogStackFrames ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val SINGLE_LINE = object : QFetchRuleA(0, 0) {
            override fun fetchStartCheck(
                    line: String,
                    currentLineNumber: Int,
                    targetLine: String,
                    targetLineNumber: Int,
                    context: MutableSet<String>,
            ): QFetchStart = if (currentLineNumber == targetLineNumber) {
                QFetchStart.START_FROM_THIS_LINE
            } else {
                QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
            }

            override fun fetchEndCheck(
                    line: String,
                    currentLineNumber: Int,
                    targetLine: String,
                    targetLineNumber: Int,
                    context: MutableSet<String>,
            ): QFetchEnd = if (currentLineNumber == targetLineNumber) {
                QFetchEnd.END_WITH_THIS_LINE
            } else {
                qUnreachable()
            }
        }

        // CallChain[size=9] = QFetchRule.SMART_FETCH <-[Call]- qLogStackFrames() <-[Call]- QException.mySrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val SMART_FETCH = object : QFetchRuleA(10, 10) {
            override fun fetchStartCheck(
                    line: String,
                    currentLineNumber: Int,
                    targetLine: String,
                    targetLineNumber: Int,
                    context: MutableSet<String>,
            ): QFetchStart {
                val nIndentThis = line.qCountLeftSpace()
                val nIndentTarget = targetLine.qCountLeftSpace()
                val trimmed = line.trimStart()

                return if (arrayOf(
                                "\"\"\".",
                                "}",
                                ")",
                                ".",
                                ",",
                                "?",
                                "//",
                                "/*",
                                "*"
                        ).any { trimmed.startsWith(it) }
                ) {
                    QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
                } else if (nIndentThis <= nIndentTarget) {
                    QFetchStart.START_FROM_THIS_LINE
                } else {
                    QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
                }
            }

            override fun fetchEndCheck(
                    line: String,
                    currentLineNumber: Int,
                    targetLine: String,
                    targetLineNumber: Int,
                    context: MutableSet<String>,
            ): QFetchEnd = if (currentLineNumber >= targetLineNumber) {
                val nIndentThis = line.qCountLeftSpace()
                val nIndentTarget = targetLine.qCountLeftSpace()

                if (currentLineNumber == targetLineNumber && line.trimStart()
                                .startsWith("\"\"\"") && line.qCountOccurrence("\"\"\"") == 1
                ) {
                    // """               <<< targetLine
                    // some text
                    // """ shouldBe """
                    // some text
                    // """

                    // """
                    // some text
                    // """.log           <<< targetLine
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.qEndsWith(""".* should[a-zA-Z]+ ""${'"'}""".re)) {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.endsWith("{") || line.endsWith("(") || line.endsWith(".")) {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (nIndentThis == nIndentTarget) {
                    QFetchEnd.END_WITH_THIS_LINE
                } else {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                }
            } else {
                QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
            }
        }
    }
}

// CallChain[size=14] = LineNumberReader.qFetchLinesBetween() <-[Call]- LineNumberReader.qFetchTarge ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun LineNumberReader.qFetchLinesBetween(
        lineNumberStartInclusive: Int,
        lineNumberEndInclusive: Int,
): List<String> {
    var fetching = false
    val lines = mutableListOf<String>()

    while (true) {
        val n = this.lineNumber + 1
        val line = this.readLine() ?: break

        if (n == lineNumberStartInclusive) {
            fetching = true
            lines += line
        } else if (fetching) {
            lines += line

            if (n == lineNumberEndInclusive) {
                break
            }
        }
    }

    return lines
}

// CallChain[size=14] = TargetSurroundingLines <-[Ref]- LineNumberReader.qFetchTargetSurroundingLine ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class TargetSurroundingLines(
        val targetLineNumber: Int,
        val startLineNumber: Int,
        val endLineNumber: Int,
        val targetLine: String,
        val linesBeforeTargetLine: List<String>,
        val linesAfterTargetLine: List<String>,
) {
    // CallChain[size=13] = TargetSurroundingLines.linesBetween() <-[Call]- LineNumberReader.qFetchLines ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun linesBetween(lineNumberStartInclusive: Int, lineNumberEndInclusive: Int): List<String> {
        val lines = mutableListOf<String>()

        lines += linesBeforeTargetLine
        lines += targetLine
        lines += linesAfterTargetLine

        val startIdx = lineNumberStartInclusive - startLineNumber
        val endIdx = lineNumberEndInclusive - startLineNumber

        return lines.subList(startIdx, endIdx + 1)
    }
}

// CallChain[size=13] = LineNumberReader.qFetchTargetSurroundingLines() <-[Call]- LineNumberReader.q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun LineNumberReader.qFetchTargetSurroundingLines(
        targetLineNumber: Int,
        numLinesBeforeTargetLine: Int = 10,
        numLinesAfterTargetLine: Int = 10,
): TargetSurroundingLines {
    val start = max(1, targetLineNumber - numLinesBeforeTargetLine)
    val end = targetLineNumber + numLinesAfterTargetLine

    val lines = qFetchLinesBetween(start, end)

    return TargetSurroundingLines(
            targetLineNumber = targetLineNumber,
            startLineNumber = start,
            endLineNumber = end,
            targetLine = lines[targetLineNumber - start],
            linesBeforeTargetLine = lines.subList(0, targetLineNumber - start),
            linesAfterTargetLine = lines.subList(targetLineNumber - start + 1, lines.size)
    )
}

// CallChain[size=12] = LineNumberReader.qFetchLinesAround() <-[Call]- Path.qFetchLinesAround() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun LineNumberReader.qFetchLinesAround(
        file: Path,
        targetLineNumber: Int,
        targetLine: String,
        fetchRule: QFetchRule = QFetchRule.SMART_FETCH,
        lineSeparator: QLineSeparator = QLineSeparator.LF,
): String {
    val surroundingLines = qFetchTargetSurroundingLines(
            targetLineNumber,
            fetchRule.numLinesBeforeTargetLine,
            fetchRule.numLinesAfterTargetLine
    )
    val context: MutableSet<String> = mutableSetOf()

    var start: Int = -1
    var end: Int = -1

    val checkStartLines = mutableListOf<String>()
    checkStartLines += surroundingLines.linesBeforeTargetLine
    checkStartLines += targetLine

    val checkEndLines = mutableListOf<String>()
    checkEndLines += targetLine
    checkEndLines += surroundingLines.linesAfterTargetLine

    for ((i, line) in checkStartLines.asReversed().withIndex()) {
        val curLineNumber = targetLineNumber - i

        val check = fetchRule.fetchStartCheck(
                line,
                curLineNumber,
                targetLine,
                targetLineNumber,
                context
        )

        when (check) {
            QFetchStart.START_FROM_PREVIOUS_LINE -> {
                start = curLineNumber - 1
                break
            }

            QFetchStart.START_FROM_THIS_LINE -> {
                start = curLineNumber
                break
            }

            QFetchStart.START_FROM_NEXT_LINE -> {
                start = curLineNumber + 1
                break
            }

            QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE -> {
            }
        }
    }

    if (start == -1) {
        start = max(0, targetLineNumber - fetchRule.numLinesBeforeTargetLine)
    }

    for ((i, line) in checkEndLines.withIndex()) {
        val curLineNumber = targetLineNumber + i

        val check = fetchRule.fetchEndCheck(
                line,
                curLineNumber,
                targetLine,
                targetLineNumber,
                context
        )

        when (check) {
            QFetchEnd.END_WITH_PREVIOUS_LINE -> {
                end = curLineNumber - 1
                break
            }

            QFetchEnd.END_WITH_THIS_LINE -> {
                end = curLineNumber
                break
            }

            QFetchEnd.END_WITH_NEXT_LINE -> {
                end = curLineNumber + 1
                break
            }

            QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE -> {
            }
        }
    }

    if (end == -1) {
        end = targetLineNumber + fetchRule.numLinesAfterTargetLine
    }

    return try {
        surroundingLines.linesBetween(start, end).joinToString(lineSeparator.value)
    } catch (e: Exception) {
        QE.FetchLinesFail.throwItFile(file)
    }
}

// CallChain[size=12] = Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qReader(
        charset: Charset = Charsets.UTF_8,
        buffSize: Int = qBUFFER_SIZE,
        opts: QFlag<QOpenOpt> = QFlag.none(),
): LineNumberReader {
    return LineNumberReader(reader(charset, *opts.toOptEnums()), buffSize)
}

// CallChain[size=11] = Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLi ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qFetchLinesAround(
        lineNumber: Int,
        fetchRule: QFetchRule = QFetchRule.SMART_FETCH,
        charset: Charset = Charsets.UTF_8,
        lineSeparator: QLineSeparator = this.qLineSeparator(charset),
): String {
    val reader = qReader(charset)

    try {
        // TODO optimization
        val targetLine = qLineAt(lineNumber, charset)

        if (fetchRule == QFetchRule.SINGLE_LINE) return targetLine

        val fetchedLines = reader.use {
            it.qFetchLinesAround(this, lineNumber, targetLine, fetchRule, lineSeparator)
        }

        return fetchedLines
    } catch (e: Exception) {
        QE.FetchLinesFail.throwItBrackets("File", this, "LineNumber", lineNumber, e = e)
    }
}

// CallChain[size=12] = Path.qLineAt() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qLineAt(
        lineNumber: Int,
        charset: Charset = Charsets.UTF_8,
): String {
    bufferedReader(charset).use { reader ->
        var n = 0
        var line: String? = reader.readLine()

        while (line != null) {
            n++

            if (n == lineNumber) {
                return line
            }

            line = reader.readLine()
        }

        QE.LineNumberExceedsMaximum.throwItBrackets("File", this.absolutePathString(), "TargetLineNumber", lineNumber)
    }
}

// CallChain[size=13] = QFType <-[Ref]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QFType {
    // CallChain[size=17] = QFType.Any <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Any,
    // CallChain[size=13] = QFType.File <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    File,
    // CallChain[size=17] = QFType.Dir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Dir,
    // CallChain[size=17] = QFType.SymLink <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Pa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    SymLink,
    // CallChain[size=17] = QFType.FileOrDir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    FileOrDir;

    // CallChain[size=16] = QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun matches(path: Path?, followSymLink: Boolean = true): Boolean {
        if (path == null) return false

        return when (this) {
            Any -> true
            File -> if (followSymLink) path.isRegularFile() else path.isRegularFile(LinkOption.NOFOLLOW_LINKS)
            Dir -> if (followSymLink) path.isDirectory() else path.isDirectory(LinkOption.NOFOLLOW_LINKS)
            FileOrDir -> return if (followSymLink) {
                path.isRegularFile() || path.isDirectory()
            } else {
                path.isRegularFile(LinkOption.NOFOLLOW_LINKS) || path.isDirectory(LinkOption.NOFOLLOW_LINKS)
            }

            SymLink -> return path.isSymbolicLink()
        }
    }
}

// CallChain[size=12] = Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLines ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Collection<Path>.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    for (path in this) {
        val found = path.qFind(nameMatcher, type, maxDepth)
        if (found != null) return found
    }

    return null
}

// CallChain[size=13] = Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    return try {
        qList(type, maxDepth = maxDepth) {
            it.name.qMatches(nameMatcher)
        }.firstOrNull()
    } catch (e: NoSuchElementException) {
        null
    }
}

// CallChain[size=10] = Path.qListByMatch() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qListByMatch(
        nameMatch: QM,
        type: QFType = QFType.File,
        maxDepth: Int = 1,
        followSymLink: Boolean = false,
): List<Path> {
    return qList(
            type, maxDepth = maxDepth, followSymLink = followSymLink
    ) {
        it.name.qMatches(nameMatch)
    }
}

// CallChain[size=14] = Path.qList() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qList(
        type: QFType = QFType.File,
        maxDepth: Int = 1,
        followSymLink: Boolean = false,
        sortWith: ((Path, Path) -> Int)? = Path::compareTo,
        filter: (Path) -> Boolean = { true },
        // TODO https://stackoverflow.com/a/66996768/5570400
        // errorContinue: Boolean = true
): List<Path> {
    return qSeq(
            type = type,
            maxDepth = maxDepth,
            followSymLink = followSymLink,
            sortWith = sortWith,
            filter = filter
    ).toList()
}

// CallChain[size=15] = Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path.qFind() <-[Call]- Collecti ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Path.qSeq(
        type: QFType = QFType.File,
        maxDepth: Int = 1,
        followSymLink: Boolean = false,
        sortWith: ((Path, Path) -> Int)? = Path::compareTo,
        filter: (Path) -> Boolean = { true },
        // TODO https://stackoverflow.com/a/66996768/5570400
        // errorContinue: Boolean = true
): Sequence<Path> {
    if (!this.isDirectory())
        return emptySequence()

    val fvOpt = if (followSymLink) arrayOf(FileVisitOption.FOLLOW_LINKS) else arrayOf()

    val seq = Files.walk(this, maxDepth, *fvOpt).asSequence().filter {
        if (it == this) return@filter false

        type.matches(it, followSymLink) && filter(it)
    }

    return if (sortWith != null) {
        seq.sortedWith(sortWith)
    } else {
        seq
    }
}

// CallChain[size=13] = QFlag <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * Only Enum or QFlag can implement this interface.
 */
private sealed interface QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=15] = QFlag.bits <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val bits: Int

    // CallChain[size=15] = QFlag.contains() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun contains(flags: QFlag<T>): Boolean {
        return (bits and flags.bits) == flags.bits
    }

    // CallChain[size=14] = QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun toEnumValues(): List<T>

    // CallChain[size=15] = QFlag.str() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOpt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun str(): String {
        return toEnumValues().joinToString(", ") { it.name }
    }

    companion object {
        // https://discuss.kotlinlang.org/t/reified-generics-on-class-level/16711/2
        // But, can't make constructor private ...

        // CallChain[size=13] = QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        inline fun <reified T>
        none(): QFlag<T> where T : QFlag<T>, T : Enum<T> {
            return QFlagSet<T>(T::class, 0)
        }
    }
}

// CallChain[size=14] = QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLin ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private interface QFlagEnum<T> : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=15] = QFlagEnum.bits <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val bits: Int
        get() = 1 shl (this as T).ordinal
    // CallChain[size=15] = QFlagEnum.toEnumValues() <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Pa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toEnumValues(): List<T> = listOf(this) as List<T>
}

// CallChain[size=14] = QFlagSet <-[Call]- QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFet ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * Mutable bit flag
 */
private class QFlagSet<T>(val enumClass: KClass<T>, override var bits: Int) : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=16] = QFlagSet.enumValues <-[Call]- QFlagSet.toEnumValues() <-[Propag]- QFlagSet < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val enumValues: Array<T> by lazy { enumClass.qEnumValues() }

    // CallChain[size=15] = QFlagSet.toEnumValues() <-[Propag]- QFlagSet <-[Call]- QFlag.none() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toEnumValues(): List<T> =
        enumValues.filter { contains(it) }
}

// CallChain[size=2] = Int.qFormat() <-[Call]- QBlock.toString()[Root]
private fun Int.qFormat(digits: Int = 4): String {
    return String.format("%${digits}d", this)
}

// CallChain[size=3] = QUnit <-[Ref]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private enum class QUnit {
    // CallChain[size=3] = QUnit.Nano <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Nano,
    // CallChain[size=3] = QUnit.Micro <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Micro,
    // CallChain[size=3] = QUnit.Milli <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Milli,
    // CallChain[size=3] = QUnit.Second <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Second,
    // CallChain[size=3] = QUnit.Minute <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Minute,
    // CallChain[size=3] = QUnit.Hour <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Hour,
    // CallChain[size=3] = QUnit.Day <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
    Day
}

// CallChain[size=2] = Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private fun Long.qFormatDuration(unit: QUnit = QUnit.Nano): String {
    return when (unit) {
        QUnit.Milli ->
            Duration.ofMillis(this).qFormat()
        QUnit.Micro ->
            Duration.ofNanos(this).qFormat()
        QUnit.Nano ->
            Duration.ofNanos(this).qFormat()
        QUnit.Second ->
            Duration.ofSeconds(this).qFormat()
        QUnit.Minute ->
            Duration.ofMinutes(this).qFormat()
        QUnit.Hour ->
            Duration.ofHours(this).qFormat()
        QUnit.Day ->
            Duration.ofDays(this).qFormat()
    }
}

// CallChain[size=4] = Duration.qToMicrosOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private fun Duration.qToMicrosOnlyPart(): Int {
    return (toNanosPart() % 1_000_000) / 1_000
}

// CallChain[size=4] = Duration.qToNanoOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private fun Duration.qToNanoOnlyPart(): Int {
    return toNanosPart() % 1_000
}

// CallChain[size=2] = Double.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private fun Double.qFormatDuration(): String =
    toLong().qFormatDuration()

// CallChain[size=3] = Duration.qFormat() <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString()[Root]
private fun Duration.qFormat(detail: Boolean = false): String {
    val du = abs()

    val maxUnit: QUnit = du.let {
        when {
            it < Duration.ofNanos(1000) -> QUnit.Nano
            it < Duration.ofMillis(1) -> QUnit.Micro
            it < Duration.ofSeconds(1) -> QUnit.Milli
            it < Duration.ofMinutes(1) -> QUnit.Second
            it < Duration.ofHours(1) -> QUnit.Minute
            it < Duration.ofDays(1) -> QUnit.Hour
            else -> QUnit.Day
        }
    }

    val parts = mutableListOf<String>()
    when (maxUnit) {
        QUnit.Nano -> {
            parts.add(String.format("%3d ns", du.toNanosPart()))
        }
        QUnit.Micro -> {
            parts.add(String.format("%3d μs", du.qToMicrosOnlyPart()))

            if (du.qToMicrosOnlyPart() <= 3 || detail)
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
        }
        QUnit.Milli -> {
            parts.add(String.format("%3d ms", du.toMillisPart()))

            if (du.toMillisPart() <= 3 || detail)
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
        }
        QUnit.Second -> {
            parts.add(String.format("%2d sec", du.toSecondsPart()))
            parts.add(String.format("%03d ms", du.toMillisPart()))

            if (detail) {
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Minute -> {
            parts.add(String.format("%2d min", du.toMinutesPart()))
            parts.add(String.format("%02d sec", du.toSecondsPart()))
            if (detail) {
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Hour -> {
            parts.add(String.format("%2d hour", du.toHoursPart()))
            parts.add(String.format("%02d min", du.toMinutesPart()))
            if (detail) {
                parts.add(String.format("%02d sec", du.toSecondsPart()))
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Day -> {
            parts.add(String.format("%2d day", du.toDaysPart()))
            parts.add(String.format("%02d hour", du.toHoursPart()))
            if (detail) {
                parts.add(String.format("%02d min", du.toMinutesPart()))
                parts.add(String.format("%02d sec", du.toSecondsPart()))
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
    }

    return parts.joinToString(" ")
}

// CallChain[size=12] = qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyl ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qARROW = "===>".light_cyan

// CallChain[size=6] = QSrcCut <-[Ref]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QSrcCut(
        val fetchRule: QFetchRule = QFetchRule.SINGLE_LINE,
        val cut: (srcLines: String) -> String,
) {
    companion object {
        // CallChain[size=10] = QSrcCut.CUT_PARAM_qLog <-[Call]- QSrcCut.SINGLE_qLog_PARAM <-[Call]- qLogSta ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        private val CUT_PARAM_qLog = { mySrc: String ->
            mySrc.replaceFirst("""(?s)^\s*(\S.+)\.qLog[a-zA-Z]{0,10}.*$""".re, "$1")
        }

        // CallChain[size=6] = QSrcCut.CUT_UNTIL_qLog <-[Call]- QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
        private val CUT_UNTIL_qLog = { mySrc: String ->
            mySrc.replaceFirst("""(?s)(?m)^(\s*)(\S.+)\.qLog[a-zA-Z]{0,10}.*$""".re, "$1$2")
        }

        // CallChain[size=9] = QSrcCut.SINGLE_qLog_PARAM <-[Call]- qLogStackFrames() <-[Call]- QException.my ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val SINGLE_qLog_PARAM = QSrcCut(QFetchRule.SINGLE_LINE, CUT_PARAM_qLog)
        // CallChain[size=5] = QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
        val UNTIL_qLog = QSrcCut(QFetchRule.SMART_FETCH, CUT_UNTIL_qLog)
        // CallChain[size=6] = QSrcCut.MULTILINE_NOCUT <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val MULTILINE_NOCUT = QSrcCut(QFetchRule.SMART_FETCH) { it }
        // CallChain[size=10] = QSrcCut.NOCUT_JUST_SINGLE_LINE <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLog ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val NOCUT_JUST_SINGLE_LINE = QSrcCut(QFetchRule.SINGLE_LINE) { it }
        
    }
}

// CallChain[size=9] = QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QException.mySrcAndStack ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QLogStyle(
        val stackSize: Int,
        val out: QOut = QMyLog.out,
        val start: String = "----".cyan + "\n",
        val end: String = "\n\n",
        val stackReverseOrder: Boolean = false,
        val template: (msg: String, mySrc: String, now: Long, stackTrace: String) -> String,
) {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        // CallChain[size=9] = QLogStyle.String.clarifySrcRegion() <-[Call]- QLogStyle.SRC_AND_STACK <-[Call ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun String.clarifySrcRegion(onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
            if (!onlyIf.matches(this))
                return this

            return """${"SRC START ―――――――――――".qColor(QShColor.CYAN)}
${this.trim()}
${"SRC END   ―――――――――――".qColor(QShColor.CYAN)}"""
        }

        // CallChain[size=10] = QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]- qLogStackFrames() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun qLogArrow(mySrc: String, msg: String): String {
            return if (mySrc.startsWith("\"") && mySrc.endsWith("\"") && mySrc.substring(1, mySrc.length - 1)
                            .matches("""[\w\s]+""".re)
            ) {
                // src code is simple string
                "\"".light_green + msg + "\"".light_green
//                ("\"" + msg + "\"").light_green
            } else {
                qArrow(mySrc.clarifySrcRegion(QOnlyIfStr.Multiline), msg)
            }
        }

        // CallChain[size=8] = QLogStyle.SRC_AND_STACK <-[Call]- QException.mySrcAndStack <-[Call]- QExcepti ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val SRC_AND_STACK: QLogStyle
            get() = QLogStyle(1) { _, mySrc, _, stackTrace ->
                """
${mySrc.clarifySrcRegion(QOnlyIfStr.Always)}
$stackTrace""".trim()
            }

        // CallChain[size=9] = QLogStyle.S <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val S: QLogStyle
            get() = QLogStyle(1) { msg, mySrc, _, stackTrace ->
                """
${qLogArrow(mySrc, msg)}
$stackTrace
""".trim()
            }

        
    }
}

// CallChain[size=4] = T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
private fun <T : Any?> T.qLog(stackDepth: Int = 0, quiet: Boolean = false): T {
    qLog(qToLogString(), stackDepth = stackDepth + 1, srcCut = QSrcCut.UNTIL_qLog, quiet = quiet)
    return this
}

// CallChain[size=9] = qMySrcLinesAtFrame() <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qMySrcLinesAtFrame(
        frame: StackFrame,
        srcCut: QSrcCut = QSrcCut.NOCUT_JUST_SINGLE_LINE,
        srcRoots: List<Path> = QMyPath.src_root,
        srcCharset: Charset = Charsets.UTF_8,
): String {
    return try {
        val src = qCacheItOneSec("${frame.fileName} - ${frame.lineNumber} - ${srcCut.fetchRule.hashCode()}") {
            qSrcFileLinesAtFrame(
                    srcRoots = srcRoots, charset = srcCharset, fetchRule = srcCut.fetchRule, frame = frame
            )
        }

        val src2 = srcCut.cut(src).trimIndent()
        src2
    } catch (e: Exception) {
//        e.message
        "${QMyMark.WARN} Couldn't cut src lines : ${qBrackets("FileName", frame.fileName, "LineNo", frame.lineNumber, "SrcRoots", srcRoots)}"
    }
}

// CallChain[size=8] = qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Call]- QException.pri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qLogStackFrames(
        frames: List<StackFrame>,
        msg: Any? = "",
        style: QLogStyle = QLogStyle.S,
        srcRoots: List<Path> = QMyPath.src_root,
        srcCharset: Charset = Charsets.UTF_8,
        srcCut: QSrcCut = QSrcCut.SINGLE_qLog_PARAM,
        quiet: Boolean = false,
        noColor: Boolean = false,
): String {

    var mySrc = qMySrcLinesAtFrame(frame = frames[0], srcCut = srcCut, srcRoots = srcRoots, srcCharset = srcCharset)

    if (mySrc.trimStart().startsWith("}.") || mySrc.trimStart().startsWith(").") || mySrc.trimStart()
                    .startsWith("\"\"\"")
    ) {
        // Maybe you want to check multiple lines

        val multilineCut = QSrcCut(QFetchRule.SMART_FETCH, srcCut.cut)

        mySrc = qMySrcLinesAtFrame(
                frame = frames[0], srcCut = multilineCut, srcRoots = srcRoots, srcCharset = srcCharset
        )
    }

    val stackTrace = if (style.stackReverseOrder) {
        frames.reversed().joinToString("\n") { it.toString() }
    } else {
        frames.joinToString("\n") { it.toString() }
    }

    val output = style.template(
            msg.qToLogString(), mySrc, qNow, stackTrace
    )

    val text = style.start + output + style.end

    val finalTxt = if (noColor) text.noColor else text

    if (!quiet)
        style.out.print(finalTxt)

    return if (noColor) output.noColor else output
}

// CallChain[size=5] = qLog() <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
private fun qLog(
        msg: Any? = "",
        style: QLogStyle = QLogStyle.S,
        srcRoots: List<Path> = QMyPath.src_root,
        srcCharset: Charset = Charsets.UTF_8,
        stackDepth: Int = 0,
        srcCut: QSrcCut = QSrcCut.SINGLE_qLog_PARAM,
        quiet: Boolean = false,
        noColor: Boolean = false,
): String {

    val frames: List<StackFrame> = qStackFrames(stackDepth + 1, style.stackSize)

    return qLogStackFrames(
            frames = frames,
            msg = msg,
            style = style,
            srcRoots = srcRoots,
            srcCharset = srcCharset,
            srcCut = srcCut,
            quiet = quiet,
            noColor = noColor
    )
}

// CallChain[size=10] = qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFra ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qSrcFileLinesAtFrame(
        srcRoots: List<Path> = QMyPath.src_root,
        pkgDirHint: String? = null,
        charset: Charset = Charsets.UTF_8,
        lineSeparator: QLineSeparator = QLineSeparator.LF,
        fetchRule: QFetchRule = QFetchRule.SMART_FETCH,
        frame: StackFrame = qStackFrame(2),
): String {
    val srcFile: Path = qSrcFileAtFrame(frame = frame, srcRoots = srcRoots, pkgDirHint = pkgDirHint)

    return srcFile.qFetchLinesAround(frame.lineNumber, fetchRule, charset, lineSeparator)
}

// CallChain[size=11] = qArrow() <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]- qLo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qArrow(key: Any?, value: Any?): String {
    val keyStr = key.qToLogString()
            .qWithNewLinePrefix(onlyIf = QOnlyIfStr.Multiline)
            .qWithNewLineSuffix(onlyIf = QOnlyIfStr.Always)

    val valStr = value.qToLogString().qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)
            .qWithSpacePrefix(numSpace = 2, onlyIf = QOnlyIfStr.SingleLine)

    return "$keyStr$qARROW$valStr"
}

// CallChain[size=11] = String.qBracketEnd() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAtFrame() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * ```
 * [key1]  value1   [key2]  value2
 * ```
 */
private fun String.qBracketEnd(value: Any?): String {
    val valStr =
            value.qToLogString().qWithSpacePrefix(2, onlyIf = QOnlyIfStr.SingleLine)
                    .qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)

    return "[$this]$valStr"
}

// CallChain[size=11] = String.qBracketStartOrMiddle() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * ```
 * [key1]  value1   [key2]  value2
 * ```
 */
private fun String.qBracketStartOrMiddle(value: Any?): String {
    val valStr = value.qToLogString().qWithSpacePrefix(2, onlyIf = QOnlyIfStr.SingleLine).qWithSpaceSuffix(3)
            .qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)

    return "[$this]$valStr"
}

// CallChain[size=10] = qBrackets() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFrames() <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qBrackets(vararg keysAndValues: Any?): String {
    if (keysAndValues.size % 2 != 0) {
        QE.ShouldBeEvenNumber.throwItBrackets("KeysAndValues.size", keysAndValues.size)
    }

    return keysAndValues.asSequence().withIndex().chunked(2) { (key, value) ->
        if (value.index != keysAndValues.size) { // last
            key.value.toString().qBracketStartOrMiddle(value.value)
        } else {
            key.value.toString().qBracketEnd(value.value)
        }
    }.joinToString("")
}

// CallChain[size=2] = QOnePassStat <-[Ref]- QBlock.stat[Root]
/**
 * Basically, stats are calculated by one-pass algorithm,
 * but some data must be kept in memory to calculate the median.
 */
@Suppress("NonAsciiCharacters", "PropertyName")
private class QOnePassStat(val maxSamplesInHeap: Int = 10_000) {
    // CallChain[size=3] = QOnePassStat.n <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var n: Long = 0
    // CallChain[size=3] = QOnePassStat.μ <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var μ: Double = .0
    // CallChain[size=3] = QOnePassStat.m2 <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var m2: Double = .0
    // CallChain[size=3] = QOnePassStat.σ2 <-[Call]- QOnePassStat.σ <-[Call]- QBlock.timeStandardDeviation[Root]
    val σ2: Double
        get() = m2 / n
    // CallChain[size=3] = QOnePassStat.max <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var max: Double = .0
    // CallChain[size=3] = QOnePassStat.min <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var min: Double = .0
    // CallChain[size=2] = QOnePassStat.σ <-[Call]- QBlock.timeStandardDeviation[Root]
    val σ: Double
        get() = sqrt(σ2)
    // CallChain[size=3] = QOnePassStat.dataHead <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    // We should keep data in memory to calculate median
    private val dataHead = QFixedSizeList<Double>(maxSamplesInHeap / 2)
    // CallChain[size=3] = QOnePassStat.dataTail <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    private val dataTail = QFixedSizeQueue<Double>(maxSamplesInHeap / 2)

    // CallChain[size=3] = QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
    private val data: DoubleArray
        get() {
            val size = min(maxSamplesInHeap.toLong(), n).toInt()

            return DoubleArray(size) { idx ->
                if (idx < dataHead.maxSize) { // idx = 0 ~ 49 ( if dataHead.size = 100 )
                    dataHead[idx]
                } else { // idx = 50 ~ 99 ( if dataHead.size = 100 )
                    idx.qLog()
                    dataTail[idx - dataHead.maxSize]
                }
            }
        }

    // CallChain[size=2] = QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
    val median: Double
        get() = data.qMedian()

    // CallChain[size=2] = QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    fun add(xNew: Double) {
        if (!dataHead.add(xNew)) {
            dataTail.add(xNew)
        }

        if (xNew > max) {
            max = xNew
        }

        if (xNew < min) {
            min = xNew
        }

        n++
        val delta = xNew - μ
        μ += delta / n
        val delta2 = xNew - μ
        m2 += delta * delta2
    }
}

// CallChain[size=4] = QFixedSizeList <-[Call]- QOnePassStat.dataHead <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
private class QFixedSizeList<T>(val maxSize: Int, val backend: MutableList<T> = ArrayList(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=3] = QFixedSizeList.add() <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    override fun add(element: T): Boolean {
        return if (this.size < maxSize) {
            backend.add(element)
        } else {
            false
        }
    }
}

// CallChain[size=4] = QFixedSizeQueue <-[Call]- QOnePassStat.dataTail <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
private class QFixedSizeQueue<T>(val maxSize: Int, val backend: ArrayDeque<T> = ArrayDeque(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=3] = QFixedSizeQueue.add() <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    override fun add(element: T): Boolean {
        return if (this.size < maxSize) {
            backend.add(element)
        } else {
            backend.removeFirst()
            backend.addLast(element)
            true
        }
    }
}

// CallChain[size=3] = DoubleArray.qMedian() <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
private fun DoubleArray.qMedian(): Double =
    sorted().let {
        if (it.size % 2 == 0)
            (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
        else
            it[it.size / 2]
    }

// CallChain[size=10] = QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QExceptio ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
interface QOut {
    // CallChain[size=12] = QOut.isAcceptColoredText <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val isAcceptColoredText: Boolean

    // CallChain[size=12] = QOut.print() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogSty ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun print(msg: Any? = "")

    // CallChain[size=12] = QOut.println() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun println(msg: Any? = "")

    // CallChain[size=12] = QOut.close() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogSty ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun close()

    companion object {
        // CallChain[size=11] = QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val CONSOLE: QOut = QConsole(true)

        
    }
}

// CallChain[size=12] = QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogStyle <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QConsole(override val isAcceptColoredText: Boolean) : QOut {
    // CallChain[size=13] = QConsole.print() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun print(msg: Any?) {
        if (isAcceptColoredText) {
            kotlin.io.print(msg.toString())
        } else {
            kotlin.io.print(msg.toString().noColor)
        }
    }

    // CallChain[size=13] = QConsole.println() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMy ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun println(msg: Any?) {
        kotlin.io.println(msg.toString())
    }

    // CallChain[size=13] = QConsole.close() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun close() {
        // Do nothing
    }
}

// CallChain[size=8] = KClass<*>.qFunctions() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun KClass<*>.qFunctions(matcher: QMFunc = QMFunc.DeclaredOnly and QMFunc.IncludeExtensionsInClass): List<KFunction<*>> {
    val list = mutableListOf<KFunction<*>>()

    var functions = if (matcher.declaredOnly) {
        this.declaredFunctions
    } else {
        this.memberFunctions
    }

    list += functions.filter { matcher.matches(it) }

    if (matcher.includeExtensionsInClass) {
        functions = if (matcher.declaredOnly) {
            this.declaredMemberExtensionFunctions
        } else {
            this.memberExtensionFunctions
        }

        list += functions.filter { matcher.matches(it) }
    }

    return list
}

// CallChain[size=17] = KClass<E>.qEnumValues() <-[Call]- QFlagSet.enumValues <-[Call]- QFlagSet.toE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun <E : Enum<E>> KClass<E>.qEnumValues(): Array<E> {
    return java.enumConstants as Array<E>
}

// CallChain[size=11] = qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qSrcFileAtFrame(frame: StackFrame, srcRoots: List<Path> = QMyPath.src_root, pkgDirHint: String? = null): Path = qCacheItOneSec(
        frame.fileName + frame.lineNumber + srcRoots.map { it }.joinToString() + pkgDirHint
) {
    val pkgDir = pkgDirHint ?: frame.declaringClass.packageName.replace(".", "/")

    var srcFile: Path? = null

    for (dir in srcRoots) {
        val root = dir.toAbsolutePath()
        val fileInPkgDir = root.resolve(pkgDir).resolve(frame.fileName)
        if (fileInPkgDir.exists()) {
            srcFile = fileInPkgDir
            break
        } else {
            val fileNoPkgDir = root.resolve(frame.fileName)
            if (fileNoPkgDir.exists()) {
                srcFile = fileNoPkgDir
            }
        }
    }

    if (srcFile != null)
        return@qCacheItOneSec srcFile

    return@qCacheItOneSec srcRoots.qFind(QM.exact(frame.fileName), maxDepth = 100)
            .qaNotNull(QE.FileNotFound, qBrackets("FileName", frame.fileName, "SrcRoots", srcRoots))
}

// CallChain[size=8] = qStackFrames() <-[Call]- QException.stackFrames <-[Call]- QException.getStack ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private inline fun qStackFrames(
        stackDepth: Int = 0,
        size: Int = 1,
        noinline filter: (StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
): List<StackFrame> {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { s: Stream<StackFrame> ->
        s.asSequence().filter(filter).drop(stackDepth).take(size).toList()
    }
}

// CallChain[size=11] = qStackFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFrame( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private inline fun qStackFrame(
        stackDepth: Int = 0,
        noinline filter: (StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
): StackFrame {
    return qStackFrames(stackDepth, 1, filter)[0]
}

// CallChain[size=9] = KType.qToClass() <-[Call]- KType.qIsSuperclassOf() <-[Call]- qToStringRegistr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun KType.qToClass(): KClass<*>? {
    return if (this.classifier != null && this.classifier is KClass<*>) {
        this.classifier as KClass<*>
    } else {
        null
    }
}

// CallChain[size=8] = KType.qIsSuperclassOf() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun KType.qIsSuperclassOf(cls: KClass<*>): Boolean {
    return try {
        val thisClass = qToClass()

        if (thisClass?.qualifiedName == "kotlin.Array" && cls.qualifiedName == "kotlin.Array") {
            true
        } else {
            thisClass?.isSuperclassOf(cls) ?: false
        }
    } catch (e: Throwable) {
        // Exception in thread "main" kotlin.reflect.jvm.internal.KotlinReflectionInternalError: Unresolved class: ~
        false
    }
}

// CallChain[size=9] = RO <-[Ref]- qRe() <-[Call]- QException.mySrcAndStack <-[Call]- QException.pri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private typealias RO = RegexOption

// CallChain[size=8] = qRe() <-[Call]- QException.mySrcAndStack <-[Call]- QException.printStackTrace ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qRe(@Language("RegExp") regex: String, vararg opts: RO): Regex {
    return qCacheItOneSecThreadLocal(regex + opts.contentToString()) {
        Regex(regex, setOf(*opts))
    }
}

// CallChain[size=12] = re <-[Call]- String.qApplyColorNestable() <-[Call]- String.qColorLine() <-[C ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
// https://youtrack.jetbrains.com/issue/KTIJ-5643
private val @receiver:Language("RegExp") String.re: Regex
    get() = qRe(this)

// CallChain[size=12] = String.qReplaceFirstIfNonEmptyStringGroup() <-[Call]- String.qApplyColorNest ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qReplaceFirstIfNonEmptyStringGroup(@Language("RegExp") regex: String, nonEmptyGroupIdx: Int, replace: String = "$1", vararg opts: RO): String {
    val re = qRe(regex, *opts)

    return if (re.find(this)?.groups?.get(nonEmptyGroupIdx)?.value?.isNotEmpty() == true) {
        re.replaceFirst(this, replace)
    } else {
        this
    }
}

// CallChain[size=12] = qBG_JUMP <-[Call]- QShColor.bg <-[Call]- String.qColorLine() <-[Call]- Strin ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qBG_JUMP = 10

// CallChain[size=10] = qSTART <-[Call]- String.qColor() <-[Call]- String.qColorTarget() <-[Call]- Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qSTART = "\u001B["

// CallChain[size=11] = qEND <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call]- Strin ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qEND = "${qSTART}0m"

// CallChain[size=12] = qMASK_COLORED <-[Call]- String.qApplyColorNestable() <-[Call]- String.qColor ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qMASK_COLORED by lazy {
    QMaskBetween(
        qSTART,
        qEND,
        qSTART,
        escapeChar = '\\',
        targetNestDepth = 1,
        maskIncludeStartAndEndSequence = false
    )
}

// CallChain[size=11] = String.qApplyColorNestable() <-[Call]- String.qColorLine() <-[Call]- String. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qApplyColorNestable(colorStart: String): String {
    val re = "(?s)(\\Q$qEND\\E)(.+?)(\\Q$qSTART\\E|$)".re
    val replace = "$1$colorStart$2$qEND$3"
    val re2 = "^(?s)(.*?)(\\Q$qSTART\\E)"
    val replace2 = "$colorStart$1$qEND$2"

    return this.qMaskAndReplace(
        qMASK_COLORED,
        re,
        replace
    ).qReplaceFirstIfNonEmptyStringGroup(re2, 1, replace2)
}

// CallChain[size=9] = String.qColor() <-[Call]- String.qColorTarget() <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qColor(fg: QShColor? = null, bg: QShColor? = null, nestable: Boolean = this.contains(qSTART)): String {
    return if (this.qIsSingleLine()) {
        this.qColorLine(fg, bg, nestable)
    } else {
        lineSequence().map { line ->
            line.qColorLine(fg, bg, nestable)
        }.joinToString("\n")
    }
}

// CallChain[size=10] = String.qColorLine() <-[Call]- String.qColor() <-[Call]- String.qColorTarget( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qColorLine(
    fg: QShColor? = null,
    bg: QShColor? = null,
    nestable: Boolean = true,
): String {
    val nest = nestable && this.contains(qEND)

    val fgApplied = if (fg != null) {
        val fgStart = fg.fg

        if (nest) {
            this.qApplyColorNestable(fgStart)
        } else {
            "$fgStart$this$qEND"
        }
    } else {
        this
    }

    val bgApplied = if (bg != null) {
        val bgStart = bg.bg

        if (nest) {
            fgApplied.qApplyColorNestable(bgStart)
        } else {
            "$bgStart$fgApplied$qEND"
        }
    } else {
        fgApplied
    }

    return bgApplied
}

// CallChain[size=14] = noColor <-[Call]- QConsole.print() <-[Propag]- QConsole <-[Call]- QOut.CONSO ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String.noColor: String
    get() {
        return this.replace("""\Q$qSTART\E\d{1,2}m""".re, "")
    }

// CallChain[size=8] = QShColor <-[Ref]- QException.mySrcAndStack <-[Call]- QException.printStackTra ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QShColor(val code: Int) {
    // CallChain[size=9] = QShColor.BLACK <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    BLACK(30),
    // CallChain[size=9] = QShColor.RED <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAndS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    RED(31),
    // CallChain[size=9] = QShColor.GREEN <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    GREEN(32),
    // CallChain[size=9] = QShColor.YELLOW <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcA ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    YELLOW(33),
    // CallChain[size=9] = QShColor.BLUE <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    BLUE(34),
    // CallChain[size=9] = QShColor.MAGENTA <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    MAGENTA(35),
    // CallChain[size=9] = QShColor.CYAN <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    CYAN(36),
    // CallChain[size=9] = QShColor.LIGHT_GRAY <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.my ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_GRAY(37),

    // CallChain[size=9] = QShColor.DARK_GRAY <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.myS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DARK_GRAY(90),
    // CallChain[size=9] = QShColor.LIGHT_RED <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.myS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_RED(91),
    // CallChain[size=9] = QShColor.LIGHT_GREEN <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.m ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_GREEN(92),
    // CallChain[size=8] = QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAndStack <-[Call]- QException ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_YELLOW(93),
    // CallChain[size=9] = QShColor.LIGHT_BLUE <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.my ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_BLUE(94),
    // CallChain[size=9] = QShColor.LIGHT_MAGENTA <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_MAGENTA(95),
    // CallChain[size=9] = QShColor.LIGHT_CYAN <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.my ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LIGHT_CYAN(96),
    // CallChain[size=9] = QShColor.WHITE <-[Propag]- QShColor.LIGHT_YELLOW <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    WHITE(97);

    // CallChain[size=11] = QShColor.fg <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /** ANSI modifier string to apply the color to the text itself */
    val fg: String = "$qSTART${code}m"

    // CallChain[size=11] = QShColor.bg <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /** ANSI modifier string to apply the color the text's background */
    val bg: String = "$qSTART${code + qBG_JUMP}m"

    companion object {
        
    }
}

// CallChain[size=8] = String.qColorTarget() <-[Call]- QException.mySrcAndStack <-[Call]- QException ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qColorTarget(ptn: Regex, color: QShColor = QShColor.LIGHT_YELLOW): String {
    return ptn.replace(this, "$0".qColor(color))
}

// CallChain[size=2] = red <-[Call]- QBlock.toString()[Root]
private val String?.red: String
    get() = this?.qColor(QShColor.RED) ?: "null".qColor(QShColor.RED)

// CallChain[size=2] = green <-[Call]- QBlock.toString()[Root]
private val String?.green: String
    get() = this?.qColor(QShColor.GREEN) ?: "null".qColor(QShColor.GREEN)

// CallChain[size=2] = yellow <-[Call]- QBlock.toString()[Root]
private val String?.yellow: String
    get() = this?.qColor(QShColor.YELLOW) ?: "null".qColor(QShColor.YELLOW)

// CallChain[size=2] = blue <-[Call]- QBlock.toString()[Root]
private val String?.blue: String
    get() = this?.qColor(QShColor.BLUE) ?: "null".qColor(QShColor.BLUE)

// CallChain[size=17] = cyan <-[Call]- QMaskResult.toString() <-[Propag]- QMaskResult <-[Ref]- QMask ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String?.cyan: String
    get() = this?.qColor(QShColor.CYAN) ?: "null".qColor(QShColor.CYAN)

// CallChain[size=5] = light_gray <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String?.light_gray: String
    get() = this?.qColor(QShColor.LIGHT_GRAY) ?: "null".qColor(QShColor.LIGHT_GRAY)

// CallChain[size=2] = dark_gray <-[Call]- QBlock.toString()[Root]
private val String?.dark_gray: String
    get() = this?.qColor(QShColor.DARK_GRAY) ?: "null".qColor(QShColor.DARK_GRAY)

// CallChain[size=11] = light_green <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String?.light_green: String
    get() = this?.qColor(QShColor.LIGHT_GREEN) ?: "null".qColor(QShColor.LIGHT_GREEN)

// CallChain[size=13] = light_cyan <-[Call]- qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String?.light_cyan: String
    get() = this?.qColor(QShColor.LIGHT_CYAN) ?: "null".qColor(QShColor.LIGHT_CYAN)

// CallChain[size=10] = path <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QExcep ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val String.path: Path
    get() = Paths.get(this.trim()).toAbsolutePath().normalize()

// CallChain[size=4] = QLR <-[Ref]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private enum class QLR {
    // CallChain[size=4] = QLR.LEFT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    LEFT,
    // CallChain[size=4] = QLR.RIGHT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    RIGHT
}

// CallChain[size=3] = String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithMaxLength(maxLength: Int, endDots: String = " ..."): String {
    if (maxLength - endDots.length < 0)
        QE.IllegalArgument.throwIt(maxLength - endDots.length)

    if (length < maxLength)
        return this

    if (endDots.isNotEmpty() && length < endDots.length + 1)
        return this

    return substring(0, length.coerceAtMost(maxLength - endDots.length)) + endDots
}

// CallChain[size=8] = QOnlyIfStr <-[Ref]- QException.qToString() <-[Call]- QException.toString() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QOnlyIfStr(val matches: (String) -> Boolean) {
    // CallChain[size=8] = QOnlyIfStr.Multiline <-[Call]- QException.qToString() <-[Call]- QException.to ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Multiline({ it.qIsMultiLine() }),
    // CallChain[size=8] = QOnlyIfStr.SingleLine <-[Call]- QException.qToString() <-[Call]- QException.t ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    SingleLine({ it.qIsSingleLine() }),
    // CallChain[size=9] = QOnlyIfStr.Empty <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToStr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Empty({ it.isEmpty() }),
    // CallChain[size=9] = QOnlyIfStr.Blank <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToStr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Blank({ it.isBlank() }),
    // CallChain[size=9] = QOnlyIfStr.NotEmpty <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qTo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    NotEmpty({ it.isNotEmpty() }),
    // CallChain[size=9] = QOnlyIfStr.NotBlank <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qTo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    NotBlank({ it.isNotBlank() }),
    // CallChain[size=9] = QOnlyIfStr.Always <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToSt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Always({ true })
}

// CallChain[size=8] = String.qWithNewLinePrefix() <-[Call]- QException.qToString() <-[Call]- QExcep ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithNewLinePrefix(
        numNewLine: Int = 1,
        onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline,
        lineSeparator: QLineSeparator = QLineSeparator.LF,
): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeWhile { it == '\n' || it == '\r' }.count()

    return lineSeparator.value.repeat(numNewLine) + substring(nCount)
}

// CallChain[size=18] = String.qWithNewLineSuffix() <-[Call]- String.qWithNewLineSurround() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithNewLineSuffix(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeLastWhile { it == '\n' || it == '\r' }.count()

    return substring(0, length - nCount) + "\n".repeat(numNewLine)
}

// CallChain[size=17] = String.qWithNewLineSurround() <-[Call]- QMaskResult.toString() <-[Propag]- Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithNewLineSurround(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    return qWithNewLinePrefix(numNewLine, QOnlyIfStr.Always).qWithNewLineSuffix(numNewLine, QOnlyIfStr.Always)
}

// CallChain[size=8] = String.qWithSpacePrefix() <-[Call]- QException.qToString() <-[Call]- QExcepti ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithSpacePrefix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return " ".repeat(numSpace) + this.trimStart()
}

// CallChain[size=12] = String.qWithSpaceSuffix() <-[Call]- String.qBracketStartOrMiddle() <-[Call]- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qWithSpaceSuffix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return this.trimEnd() + " ".repeat(numSpace)
}

// CallChain[size=10] = CharSequence.qEndsWith() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- qLogStac ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun CharSequence.qEndsWith(suffix: Regex, length: Int = 100): Boolean {
    return takeLast(min(length, this.length)).matches(suffix)
}

// CallChain[size=9] = String.qIsMultiLine() <-[Call]- QOnlyIfStr.Multiline <-[Call]- QException.qTo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qIsMultiLine(): Boolean {
    return this.contains("\n") || this.contains("\r")
}

// CallChain[size=9] = String.qIsSingleLine() <-[Call]- QOnlyIfStr.SingleLine <-[Call]- QException.q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qIsSingleLine(): Boolean {
    return !this.qIsMultiLine()
}

// CallChain[size=9] = QLineSeparator <-[Ref]- String.qWithNewLinePrefix() <-[Call]- QException.qToS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private enum class QLineSeparator(val value: String) {
    // CallChain[size=9] = QLineSeparator.LF <-[Call]- String.qWithNewLinePrefix() <-[Call]- QException. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LF("\n"),
    // CallChain[size=10] = QLineSeparator.CRLF <-[Propag]- QLineSeparator.QLineSeparator() <-[Call]- St ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    CRLF("\r\n"),
    // CallChain[size=10] = QLineSeparator.CR <-[Propag]- QLineSeparator.QLineSeparator() <-[Call]- Stri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    CR("\r");

    companion object {
        // CallChain[size=13] = QLineSeparator.DEFAULT <-[Call]- Path.qLineSeparator() <-[Call]- Path.qFetch ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val DEFAULT = QLineSeparator.LF
    }
}

// CallChain[size=6] = String.qSubstring() <-[Call]- String.qMoveLeft() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qSubstring(rangeBothInclusive: IntRange): String =
        substring(rangeBothInclusive.first, rangeBothInclusive.last + 1)

// CallChain[size=10] = String.qCountLeftSpace() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- qLogStac ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qCountLeftSpace(): Int = takeWhile { it == ' ' }.count()

// CallChain[size=6] = String.qCountRightSpace() <-[Call]- String.qMoveCenter() <-[Call]- QLineMatch ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qCountRightSpace(): Int = takeLastWhile { it == ' ' }.count()

// CallChain[size=6] = qMASK_LENGTH_LIMIT <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qMASK_LENGTH_LIMIT: Int = 100_000

// CallChain[size=8] = QToString <-[Ref]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QToString(val okToApply: (Any) -> Boolean, val toString: (Any) -> String)

// CallChain[size=7] = qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any?.qToLogString() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qToStringRegistry: MutableList<QToString> by lazy {
    val toStrings =
            QMyToString::class.qFunctions(
                    QMFunc.nameExact("qToString") and
//                            QMFunc.returnType(String::class, false) and
//                            QMFunc.NoParams and
                            QMFunc.DeclaredOnly and
                            QMFunc.IncludeExtensionsInClass
            )

    toStrings.map { func ->
        QToString(
                okToApply = { value ->
                    func.extensionReceiverParameter?.type?.qIsSuperclassOf(value::class) ?: false
                },
                toString = { value ->
                    func.call(QMyToString, value) as String
                }
        )
    }.toMutableList()
}

// CallChain[size=6] = Any?.qToString() <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Any?.qToString(): String {
    if (this == null)
        return "null".light_gray

    for (r in qToStringRegistry) {
        if (r.okToApply(this)) {
            return r.toString(this)
        }
    }

    return toString()
}

// CallChain[size=5] = Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun Any?.qToLogString(maxLineLength: Int = 80): String {
    if (QMyLog.no_format) {
        return this.toString()
    }

    if (this == null)
        return "null".light_gray
    if (this == "")
        return "".qClarifyEmptyOrBlank()

    val str = this.qToString()

    val isListOrArray =
            this !is CharSequence && (str.startsWith("[") && str.endsWith("]")) || (str.startsWith("{") && str.endsWith("}"))
    val isMultiline = isListOrArray && str.qIsMultiLine()
    val isNestedListOrArray = isListOrArray && str.startsWith("[[")

    val comma = ",".light_gray
    val separator = "----".light_gray

    return if (isNestedListOrArray) { // Nested list always add line breaks for consistent formatting.
        val str2 = str.replaceRange(1, 1, "\n")

        val masked = str2.replaceRange(str.length, str.length, "\n").qMask(
                QMask.INNER_BRACKETS
        )

        masked.replaceAndUnmask(", ".re, "$comma\n").trim()
    } else if (isListOrArray && (maxLineLength < str.length || isMultiline) && str.length < qMASK_LENGTH_LIMIT) { // qMask is slow, needs limit length
        val str2 = str.replaceRange(1, 1, "\n")

        val masked = str2.replaceRange(str.length, str.length, "\n").qMask(
                QMask.PARENS,
                QMask.KOTLIN_STRING
        )

        masked.replaceAndUnmask(", ".re, if (isMultiline) "\n$separator\n" else "$comma\n").trim()
    } else {
        str.trim()
    }.qClarifyEmptyOrBlank()
}

// CallChain[size=6] = String.qClarifyEmptyOrBlank() <-[Call]- Any?.qToLogString() <-[Call]- QE.thro ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qClarifyEmptyOrBlank(): String {
    return if (this.isEmpty()) {
        "(EMPTY STRING)".qColor(QShColor.LIGHT_GRAY)
    } else if (this.isBlank()) {
        "$this(BLANK STRING)".qColor(QShColor.LIGHT_GRAY)
    } else {
        this
    }
}

// CallChain[size=2] = QAlign <-[Ref]- QBlock.toString()[Root]
private enum class QAlign {
    // CallChain[size=4] = QAlign.LEFT <-[Propag]- QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    LEFT,
    // CallChain[size=3] = QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    RIGHT,
    // CallChain[size=2] = QAlign.CENTER <-[Call]- QBlock.toString()[Root]
    CENTER
}

// CallChain[size=4] = QLineMatchResult <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private class QLineMatchResult(
    val regex: Regex,
    val text: String,
    val onlyFirstMatch: Boolean = false,
    val groupIdx: QGroupIdx
) {
    // CallChain[size=5] = QLineMatchResult.curText <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    var curText: String = text

    // CallChain[size=5] = QLineMatchResult.updateResult() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun updateResult(align: QAlign) {
        updateRowResult()
        updateColResult()
        updateColDestPos(align)
    }

    // CallChain[size=6] = QLineMatchResult.rowResults <-[Call]- QLineMatchResult.matchedRange() <-[Call ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    // line index -> match index
    lateinit var rowResults: List<List<MatchResult>>

    // CallChain[size=7] = QLineMatchResult.colResults <-[Call]- QLineMatchResult.updateColDestPos() <-[ ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    // col index -> match index
    lateinit var colResults: List<List<MatchResult>>

    // CallChain[size=5] = QLineMatchResult.colDestPos <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    // col index -> max left index / right index of matched region
    lateinit var colDestPos: List<QLeftRight>

    // CallChain[size=6] = QLineMatchResult.updateRowResult() <-[Call]- QLineMatchResult.updateResult()  ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun updateRowResult() {
        rowResults = if (onlyFirstMatch) {
            curText.lineSequence().map { regex.find(it, 0) }.map {
                if (it == null) {
                    emptyList()
                } else {
                    listOf(it)
                }
            }.toList()
        } else {
            curText.lineSequence().map { regex.findAll(it, 0).toList() }.toList()
        }
    }

    // CallChain[size=6] = QLineMatchResult.updateColResult() <-[Call]- QLineMatchResult.updateResult()  ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun updateColResult() {
        colResults = mutableListOf<List<MatchResult>>().also { list ->
            val maximumRowMatchCount = rowResults.maxOfOrNull { it.size } ?: -1

            for (iColumn in 0 until maximumRowMatchCount) {
                list += rowResults.mapNotNull { result ->
                    result.getOrNull(iColumn)
                }
            }
        }
    }

    // CallChain[size=6] = QLineMatchResult.updateColDestPos() <-[Call]- QLineMatchResult.updateResult() ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun updateColDestPos(align: QAlign) {
        colDestPos = if (align == QAlign.RIGHT) {
            colResults.map { it.qMinOrMaxIndexLR(groupIdx, QMinOrMax.MAX) }
        } else {
            colResults.map { it.qMinOrMaxIndexLR(groupIdx, QMinOrMax.MIN) }
        }
    }

    // CallChain[size=5] = QLineMatchResult.matchedRange() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun matchedRange(rowIdx: Int, colIdx: Int): IntRange? {
        val groups = rowResults.getOrNull(rowIdx)?.getOrNull(colIdx)?.groups ?: return null
        return if (groupIdx.idx < groups.size) {
            groups[groupIdx.idx]?.range
        } else {
            null
        }
    }

    // CallChain[size=7] = QLineMatchResult.QMinOrMax <-[Ref]- QLineMatchResult.updateColDestPos() <-[Ca ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    enum class QMinOrMax {
        MIN, MAX
    }

    // CallChain[size=7] = QLineMatchResult.List<MatchResult>.qMinOrMaxIndexLR() <-[Call]- QLineMatchRes ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    private fun List<MatchResult>.qMinOrMaxIndexLR(
        groupIdx: QGroupIdx,
        minOrMax: QMinOrMax
    ): QLeftRight {
        val leftList = mapNotNull {
            if (groupIdx.idx < it.groups.size) {
                it.groups[groupIdx.idx]?.range?.first
            } else {
                -1
            }
        }

        val left =
            if (minOrMax == QMinOrMax.MIN) {
                leftList.minOrNull() ?: -1
            } else {
                leftList.maxOrNull() ?: -1
            }

        val rightList = mapNotNull {
            if (groupIdx.idx < it.groups.size) {
                it.groups[groupIdx.idx]?.range?.last
            } else {
                -1
            }
        }

        val right =
            if (minOrMax == QMinOrMax.MIN) {
                rightList.minOrNull() ?: -1
            } else {
                rightList.maxOrNull() ?: -1
            }

        return QLeftRight(left, right)
    }

    // CallChain[size=3] = QLineMatchResult.makeEqualWidth() <-[Call]- String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
    fun makeEqualWidth(alignInRegion: QAlign): String {
        updateResult(alignInRegion)

        val maxWidth = rowResults.flatten().maxOf { result ->
            if (groupIdx.idx < result.groups.size) {
                result.groups[groupIdx.idx]?.range?.qSize ?: -1
            } else {
                -1
            }
        }

        // TODO Optimization
        var colIdx = 0
        while (colIdx < colDestPos.size) {
            val lines = curText.lineSequence().mapIndexed { rowIdx, line ->
                val range = matchedRange(rowIdx, colIdx) ?: return@mapIndexed line

                line.qRangeWithWidth(
                    range,
                    maxWidth,
                    alignInRegion
                )
            }

            curText = lines.joinToString("\n")

            updateResult(alignInRegion)

            colIdx++
        }

        return curText
    }

    // CallChain[size=4] = QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    fun align(align: QAlign = QAlign.RIGHT, keepLength: Boolean = false, oddLengthTuning: QLR): String {
        updateResult(align)

        var colIdx = 0

        while (colIdx < colDestPos.size) {
            val lines = curText.lineSequence().mapIndexed { rowIdx, line ->
                val range = matchedRange(rowIdx, colIdx) ?: return@mapIndexed line

                if (align == QAlign.CENTER) {
                    line.qMoveCenter(range, oddLengthTuning)
                } else {
                    val maxLR = colDestPos[colIdx]

                    val destLeft = when (align) {
                        QAlign.RIGHT -> maxLR.right - range.qSize + 1
                        QAlign.LEFT -> maxLR.left
                        else -> qUnreachable()
                    }

                    if (range.first < destLeft) {
                        line.qMoveRight(range, destLeft, keepLength)
                    } else {
                        line.qMoveLeft(range, destLeft, keepLength)
                    }
                }
            }

            curText = lines.joinToString("\n")

            if (!keepLength && align != QAlign.CENTER) {
                updateResult(align)
            }

            colIdx++
        }

        return curText
    }
}

// CallChain[size=5] = qSize <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private val IntRange.qSize: Int
    get() = abs(last - first) + 1

// CallChain[size=2] = String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
private fun String.qMakeEqualWidth(
    place: Regex,
    align: QAlign = QAlign.CENTER,
    groupIdx: QGroupIdx = QGroupIdx.FIRST
): String {
    return QLineMatchResult(place, this, false, groupIdx).makeEqualWidth(align)
}

// CallChain[size=2] = String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qAlignRightAll(
    vararg places: Regex = arrayOf("(.*)".re),
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = false, keepLength = keepLength, groupIdx = groupIdx)
}

// CallChain[size=2] = String.qAlignRight() <-[Call]- QBenchmark.start()[Root]
private fun String.qAlignRight(
    vararg places: Regex = arrayOf("(.*)".re),
    onlyFirstMatch: Boolean = true,
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = onlyFirstMatch, keepLength = keepLength, groupIdx = groupIdx)
}

// CallChain[size=4] = String.qIsNumber() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qIsNumber(): Boolean {
    return this.trim().matches("""[\d./eE+-]+""".re)
}

// CallChain[size=3] = String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qAlign(
    align: QAlign = QAlign.RIGHT,
    vararg places: Regex,
    onlyFirstMatch: Boolean = true,
    keepLength: Boolean = false,
    oddLengthTuning: QLR = if (qIsNumber()) QLR.RIGHT else QLR.LEFT,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    var text = this
    for (p in places) {
        text = QLineMatchResult(p, text, onlyFirstMatch, groupIdx).align(align, keepLength, oddLengthTuning)
    }

    return text
}

// CallChain[size=6] = QLeftRight <-[Ref]- QLineMatchResult.colDestPos <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private data class QLeftRight(val left: Int, val right: Int)

// CallChain[size=3] = QGroupIdx <-[Ref]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private enum class QGroupIdx(val idx: Int) {
    // CallChain[size=3] = QGroupIdx.ENTIRE_MATCH <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    ENTIRE_MATCH(0),
    // CallChain[size=7] = QGroupIdx.FIRST <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    FIRST(1),
    // CallChain[size=7] = QGroupIdx.SECOND <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    SECOND(2),
    // CallChain[size=7] = QGroupIdx.THIRD <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    THIRD(3),
    // CallChain[size=7] = QGroupIdx.FOURTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    FOURTH(4),
    // CallChain[size=7] = QGroupIdx.FIFTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    FIFTH(5),
    // CallChain[size=7] = QGroupIdx.SIXTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    SIXTH(6),
    // CallChain[size=7] = QGroupIdx.SEVENTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResul ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    SEVENTH(7),
    // CallChain[size=7] = QGroupIdx.EIGHTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    EIGHTH(8),
    // CallChain[size=7] = QGroupIdx.NINTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    NINTH(9),
    // CallChain[size=7] = QGroupIdx.TENTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult. ... gn() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    TENTH(10);
}

// CallChain[size=5] = String.qMoveCenter() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
// always keep length
private fun String.qMoveCenter(range: IntRange, oddLengthTuning: QLR): String {
    val regionText = qSubstring(range) // includes spaces

    val nLeftSpace = regionText.qCountLeftSpace()
    val nRightSpace = regionText.qCountRightSpace()

    val nonSpaceChars = regionText.substring(nLeftSpace, regionText.length - nRightSpace)

    val nLeftSpaceTarget =
        if (oddLengthTuning == QLR.LEFT || (nLeftSpace + nRightSpace) % 2 == 0) {
            abs(nLeftSpace + nRightSpace) / 2
        } else {
            abs(nLeftSpace + nRightSpace) / 2 + 1
        }
    val nRightSpaceTarget = (nLeftSpace + nRightSpace) - nLeftSpaceTarget

    return replaceRange(range, " ".repeat(nLeftSpaceTarget) + nonSpaceChars + " ".repeat(nRightSpaceTarget))
//    return substring(
//        0, range.first
//    ) + " ".repeat(nLeftSpaceTarget) + nonSpaceChars + " ".repeat(nRightSpaceTarget) + substring(range.last + 1, length)
}

// CallChain[size=5] = qSpaces() <-[Call]- String.qRangeWithWidth() <-[Call]- QLineMatchResult.makeEqualWidth() <-[Call]- String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
private fun qSpaces(n: Int): String = " ".repeat(n)

// CallChain[size=4] = String.qRangeWithWidth() <-[Call]- QLineMatchResult.makeEqualWidth() <-[Call]- String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
private fun String.qRangeWithWidth(range: IntRange, destWidth: Int, alignInRegion: QAlign): String {
    val regionText = qSubstring(range)

    val nLeftSpace = regionText.qCountLeftSpace()
    val nRightSpace = regionText.qCountRightSpace()

    val nonSpaceChars = regionText.substring(nLeftSpace, regionText.length - nRightSpace)

    val nSpaces = destWidth - nonSpaceChars.length

    if (nSpaces <= 0)
        return this

    return when (alignInRegion) {
        QAlign.CENTER -> {
            val nLeftSpaceTarget = nSpaces / 2
            val nRightSpaceTarget = nSpaces - nLeftSpaceTarget

            replaceRange(range, qSpaces(nLeftSpaceTarget) + nonSpaceChars + qSpaces(nRightSpaceTarget))
//            substring(
//                0, range.first
//            ) + " ".repeat(nLeftSpaceTarget) + nonSpaceChars + " ".repeat(nRightSpaceTarget) + substring(
//                range.last + 1,
//                length
//            )
        }
        QAlign.LEFT -> {
            replaceRange(range, qSpaces(nSpaces) + nonSpaceChars)
//            substring(
//                0, range.first
//            ) + " ".repeat(nSpaces) + nonSpaceChars + substring(
//                range.last + 1,
//                length
//            )
        }
        QAlign.RIGHT -> {
            replaceRange(range, nonSpaceChars + qSpaces(nSpaces))
//            substring(
//                0, range.first
//            ) + nonSpaceChars + " ".repeat(nSpaces) + substring(
//                range.last + 1,
//                length
//            )
        }
    }
}

// CallChain[size=5] = String.qMoveLeft() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qMoveLeft(range: IntRange, destRangeLeft: Int, keepLength: Boolean): String {
    if (destRangeLeft >= range.first) {
        return this
    }

    if (substring(destRangeLeft, range.first).isNotBlank()) {
        // can't move. already has some contents.
        return this
    }

    val regionText = qSubstring(range)

    val nSpaces = range.first - destRangeLeft
    if (nSpaces <= 0) return this

    // when keepLength is true, add as many spaces to the right as removed
    val rightSpaces = if (keepLength) " ".repeat(nSpaces) else ""

    // cut left spaces
    val first = substring(0, range.first - nSpaces) + regionText

    // add spaces to the right
    return first + rightSpaces + substring(range.last + 1, length)
}

// CallChain[size=5] = String.qMoveRight() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private fun String.qMoveRight(range: IntRange, destRangeLeft: Int, keepLength: Boolean): String {
    val regionText = qSubstring(range)

    val nSpaces = destRangeLeft - range.first

    if (nSpaces <= 0) return this

    val spaces = " ".repeat(nSpaces)

    return if (keepLength) {
        if (range.last + 1 > length) return this

        if (range.last + 1 + nSpaces > length) return this

        if (substring(range.last + 1, range.last + 1 + nSpaces).isNotBlank()) return this

        replaceRange(IntRange(range.first, range.last - nSpaces), spaces + regionText)
    } else {
        replaceRange(range, spaces + regionText)
    }
}

// CallChain[size=10] = String.qCountOccurrence() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- qLogSta ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qCountOccurrence(word: String): Int {
    return windowed(word.length) {
        if (it == word)
            1
        else
            0
    }.sum()
}

// CallChain[size=14] = QMask <-[Ref]- QMaskBetween <-[Call]- qMASK_COLORED <-[Call]- String.qApplyC ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private interface QMask {
    // CallChain[size=15] = QMask.apply() <-[Propag]- QMask <-[Ref]- QMaskBetween <-[Call]- qMASK_COLORE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun apply(text: String): QMaskResult

    companion object {
        // CallChain[size=7] = QMask.THREE_DOUBLE_QUOTES <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any?.qToLog ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val THREE_DOUBLE_QUOTES by lazy {
            QMaskBetween(
                "\"\"\"", "\"\"\"",
                nestStartSequence = null,
                escapeChar = '\\',
                maskIncludeStartAndEndSequence = false,
            )
        }
        // CallChain[size=7] = QMask.DOUBLE_QUOTE <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any?.qToLogString( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val DOUBLE_QUOTE by lazy {
            QMaskBetween(
                "\"", "\"",
                nestStartSequence = null,
                escapeChar = '\\',
                maskIncludeStartAndEndSequence = false,
            )
        }
        // CallChain[size=6] = QMask.KOTLIN_STRING <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val KOTLIN_STRING by lazy {
            QMultiMask(
                THREE_DOUBLE_QUOTES,
                DOUBLE_QUOTE
            )
        }
        // CallChain[size=6] = QMask.PARENS <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val PARENS by lazy {
            QMaskBetween(
                "(", ")",
                nestStartSequence = "(", escapeChar = '\\'
            )
        }
        // CallChain[size=6] = QMask.INNER_BRACKETS <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val INNER_BRACKETS by lazy {
            QMaskBetween(
                "[", "]",
                nestStartSequence = "[", escapeChar = '', // shell color
                targetNestDepth = 2,
                maskIncludeStartAndEndSequence = true
            )
        }

        
    }
}

// CallChain[size=7] = QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any?.qToLogString() <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMultiMask(vararg mask: QMaskBetween) : QMask {
    // CallChain[size=9] = QMultiMask.masks <-[Call]- QMultiMask.apply() <-[Propag]- QMultiMask <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val masks: Array<QMaskBetween>

    // CallChain[size=8] = QMultiMask.init { <-[Propag]- QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    init {
        masks = arrayOf(*mask)
    }

    // CallChain[size=8] = QMultiMask.apply() <-[Propag]- QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun apply(text: String): QMaskResult {
        var result: QMaskResult? = null
        for (mask in masks) {
            result = result?.applyMoreMask(mask) ?: mask.apply(text)
        }

        return result!!
    }
}

// CallChain[size=13] = QMaskBetween <-[Call]- qMASK_COLORED <-[Call]- String.qApplyColorNestable()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMaskBetween(
    val startSequence: String,
    val endSequence: String,
    val nestStartSequence: String? = if (startSequence != endSequence) {
        startSequence // can have nested structure
    } else {
        null // no nested structure
    },
    val escapeChar: Char? = null,
    val allowEOFEnd: Boolean = false,
    val targetNestDepth: Int = 1,
    val maskIncludeStartAndEndSequence: Boolean = false,
    val invert: Boolean = false,
    val noMaskChars: CharArray? = null, // charArrayOf('\u0020', '\t', '\n', '\r'),
    // U+E000..U+F8FF BMP (0) Private Use Area
    val maskChar: Char = '\uee31',
) : QMask {

    // CallChain[size=14] = QMaskBetween.apply() <-[Propag]- QMaskBetween.QMaskBetween() <-[Ref]- qMASK_ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun apply(text: String): QMaskResult {
        return applyMore(text, null)
    }

    // CallChain[size=15] = QMaskBetween.applyMore() <-[Call]- QMaskBetween.apply() <-[Propag]- QMaskBet ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun applyMore(text: String, orgText: String? = null): QMaskResult {
        val regions = text.qFindBetween(
            startSequence,
            endSequence,
            nestStartSequence,
            escapeChar,
            allowEOFEnd,
            targetNestDepth,
            maskIncludeStartAndEndSequence
        )

        val sb = StringBuilder(text.length)

        val iter = text.iterator()

        var idx = -1

        while (iter.hasNext()) {
            idx++

            var masked = false

            val ch = iter.nextChar()

            if (noMaskChars?.contains(ch) == true) {
                sb.append(ch)
                continue
            }

            for (region in regions) {
                if (idx < region.start) {
                    break
                }

                if (region.contains(idx)) {
                    sb.append(if (!invert) maskChar else ch)
                    masked = true
                    break
                }
            }

            if (!masked) {
                sb.append(if (!invert) ch else maskChar)
            }
        }

        val maskedStr = sb.toString()

        return QMaskResult(maskedStr, orgText ?: text, maskChar)
    }
}

// CallChain[size=18] = QMutRegion <-[Ref]- QRegion.toMutRegion() <-[Propag]- QRegion.contains() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private open class QMutRegion(override var start: Int, override var end: Int) : QRegion(start, end) {
    // CallChain[size=19] = QMutRegion.intersectMut() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegio ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun intersectMut(region: QRegion) {
        val start = max(this.start, region.start)
        val end = min(this.end, region.end)

        if (start <= end) {
            this.start = start
            this.end = end
        }
    }

    // CallChain[size=19] = QMutRegion.addOffset() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegion() ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun addOffset(offset: Int) {
        start += offset
        end += offset
    }

    // CallChain[size=19] = QMutRegion.shift() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegion() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun shift(length: Int) {
        this.start += length
        this.end += length
    }
}

// CallChain[size=18] = QRegion <-[Ref]- QRegion.intersect() <-[Propag]- QRegion.contains() <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * [start] inclusive, [end] exclusive
 */
private open class QRegion(open val start: Int, open val end: Int) {
    // CallChain[size=17] = QRegion.toMutRegion() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun toMutRegion(): QMutRegion {
        return QMutRegion(start, end)
    }

    // CallChain[size=17] = QRegion.toRange() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.appl ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun toRange(): IntRange {
        return IntRange(start, end + 1)
    }

    // CallChain[size=17] = QRegion.length <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val length: Int
        get() = end - start

    // CallChain[size=17] = QRegion.intersect() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.ap ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun intersect(region: QRegion): QRegion? {
        val start = max(this.start, region.start)
        val end = min(this.end, region.end)

        return if (start <= end) {
            QRegion(end, start)
        } else {
            null
        }
    }

    // CallChain[size=16] = QRegion.contains() <-[Call]- QMaskBetween.applyMore() <-[Call]- QMaskBetween ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun contains(idx: Int): Boolean {
        return idx in start until end
    }

    // CallChain[size=17] = QRegion.cut() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMor ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun cut(text: String): String {
        return text.substring(start, end)
    }

    // CallChain[size=17] = QRegion.remove() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.apply ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun remove(text: String): String {
        return text.removeRange(start, end)
    }

    // CallChain[size=17] = QRegion.replace() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.appl ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun replace(text: String, replacement: String): String {
        return text.replaceRange(start, end, replacement)
    }

    // CallChain[size=17] = QRegion.mask() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun mask(text: String, maskChar: Char = '*'): String {
        return text.replaceRange(this.toRange(), maskChar.toString().repeat(end - start))
    }
}

// CallChain[size=14] = QReplacer <-[Ref]- String.qMaskAndReplace() <-[Call]- String.qMaskAndReplace ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QReplacer(start: Int, end: Int, val replacement: String) : QMutRegion(start, end)

// CallChain[size=15] = QMaskResult <-[Ref]- QMaskBetween.apply() <-[Propag]- QMaskBetween.QMaskBetw ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMaskResult(val maskedStr: String, val orgText: String, val maskChar: Char) {
    // CallChain[size=6] = QMaskResult.replaceAndUnmask() <-[Call]- Any?.qToLogString() <-[Call]- QE.thr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /**
     * Apply regex to masked string.
     * Apply replacement to original text.
     */
    fun replaceAndUnmask(ptn: Regex, replacement: String, findAll: Boolean = true): String {
        return orgText.qMaskAndReplace(maskedStr, ptn, replacement, findAll)
    }

    // CallChain[size=9] = QMaskResult.applyMoreMask() <-[Call]- QMultiMask.apply() <-[Propag]- QMultiMa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun applyMoreMask(mask: QMaskBetween): QMaskResult {
        return mask.applyMore(maskedStr, orgText)
    }

    // CallChain[size=16] = QMaskResult.toString() <-[Propag]- QMaskResult <-[Ref]- QMaskBetween.apply() ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        val original = orgText.qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)
        val masked = maskedStr.replace(maskChar, '*').qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)

        return "${QMaskResult::class.simpleName} : $original ${"->".cyan} $masked"
    }
}

// CallChain[size=6] = CharSequence.qMask() <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun CharSequence.qMask(vararg mask: QMask): QMaskResult {
    mask.size.qaNotZero()

    return if (mask.size == 1) {
        mask[0].apply(this.toString())
    } else {
        val masks = mutableListOf<QMaskBetween>()
        for (m in mask) {
            if (m is QMaskBetween) {
                masks += m
            } else if (m is QMultiMask) {
                masks += m.masks
            }
        }

        QMultiMask(*masks.toTypedArray()).apply(this.toString())
    }
}

// CallChain[size=16] = String.qFindBetween() <-[Call]- QMaskBetween.applyMore() <-[Call]- QMaskBetw ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qFindBetween(
    startSequence: String,
    endSequence: String,
    nestStartSequence: String? = if (startSequence != endSequence) {
        startSequence // can have nested structure
    } else {
        null // no nested structure
    },
    escapeChar: Char? = null,
    allowEOFEnd: Boolean = false,
    nestingDepth: Int = 1,
    regionIncludesStartAndEndSequence: Boolean = false,
): List<QRegion> {
    val finder = QBetween(
        startSequence,
        endSequence,
        nestStartSequence,
        escapeChar,
        allowEOFEnd,
        nestingDepth,
        regionIncludesStartAndEndSequence
    )

    return finder.find(this)
}

// CallChain[size=13] = String.qMaskAndReplace() <-[Call]- String.qMaskAndReplace() <-[Call]- String ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qMaskAndReplace(
    maskedStr: String,
    ptn: Regex,
    replacement: String = "$1",
    replaceAll: Boolean = true,
): String {
    // Apply Regex pattern to maskedStr
    val findResults: Sequence<MatchResult> = if (replaceAll) {
        ptn.findAll(maskedStr)
    } else {
        val result = ptn.find(maskedStr)
        if (result == null) {
            emptySequence()
        } else {
            sequenceOf(result)
        }
    }

    val replacers: MutableList<QReplacer> = mutableListOf()

    for (r in findResults) {
        val g = r.qResolveReplacementGroup(replacement, this)
        replacers += QReplacer(
            r.range.first,
            r.range.last + 1,
            g
        )
    }

    // Apply replacements to this String instead of maskedStr
    return qMultiReplace(replacers)
}

// CallChain[size=12] = String.qMaskAndReplace() <-[Call]- String.qApplyColorNestable() <-[Call]- St ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qMaskAndReplace(
    mask: QMask,
    ptn: Regex,
    replacement: String = "$1",
    replaceAll: Boolean = true,
): String {
    val maskResult = mask.apply(this)

    return qMaskAndReplace(maskResult.maskedStr, ptn, replacement, replaceAll)
}

// CallChain[size=14] = CharSequence.qMultiReplace() <-[Call]- String.qMaskAndReplace() <-[Call]- St ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
/**
 * currently does not support region overlap
 */
private fun CharSequence.qMultiReplace(replacers: List<QReplacer>): String {
    // TODO Use StringBuilder
    val sb = StringBuilder(this)
    var offset = 0
    for (r in replacers) {
        sb.replace(r.start + offset, r.end + offset, r.replacement)
        offset += r.replacement.length - (r.end - r.start)
    }

    return sb.toString()
}

// CallChain[size=14] = MatchResult.qResolveReplacementGroup() <-[Call]- String.qMaskAndReplace() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun MatchResult.qResolveReplacementGroup(replacement: String, orgText: String): String {
    var resolveGroup = replacement

    for ((i, g) in groups.withIndex()) {
        if (g == null) continue

        val gValue = if (g.range.last - g.range.first == 0 || !resolveGroup.contains("$")) {
            ""
        } else {
            orgText.substring(g.range)
        }

        resolveGroup = resolveGroup.qReplace("$$i", gValue, '\\')
    }

    return resolveGroup
}

// CallChain[size=15] = CharSequence.qReplace() <-[Call]- MatchResult.qResolveReplacementGroup() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun CharSequence.qReplace(oldValue: String, newValue: String, escapeChar: Char): String {
    return replace(Regex("""(?<!\Q$escapeChar\E)\Q$oldValue\E"""), newValue)
}

// CallChain[size=18] = QSequenceReader <-[Call]- QBetween.find() <-[Call]- String.qFindBetween() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QSequenceReader(text: CharSequence) : QCharReader(text) {
    // CallChain[size=20] = QSequenceReader.sequenceOffset <-[Call]- QSequenceReader.offsetInSequence()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var sequenceOffset = 0

    // CallChain[size=20] = QSequenceReader.sequence <-[Call]- QSequenceReader.peekCurrentCharInSequence ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var sequence: CharArray? = null

    // CallChain[size=19] = QSequenceReader.startReadingSequence() <-[Call]- QSequenceReader.detectSeque ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    private fun startReadingSequence(sequence: CharArray): Boolean {
        return if (!hasNextChar(sequence.size)) {
            false
        } else {
            this.sequence = sequence
            sequenceOffset = offset
            true
        }
    }

    // CallChain[size=19] = QSequenceReader.endReadingSequence() <-[Call]- QSequenceReader.detectSequenc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    private fun endReadingSequence(success: Boolean): Boolean {

        if (!success) {
            offset = sequenceOffset
        }

        sequenceOffset = -1

        return success
    }

    // CallChain[size=19] = QSequenceReader.hasNextCharInSequence() <-[Call]- QSequenceReader.detectSequ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun hasNextCharInSequence(): Boolean {
        return if (sequence == null) {
            false
        } else {
            (offsetInSequence() < sequence!!.size) &&
                hasNextChar()
        }
    }

//    inline fun peekNextCharInSequence(): Char {
//        return sequence!![offset - sequenceOffset]
//    }

    // CallChain[size=19] = QSequenceReader.peekCurrentCharInSequence() <-[Call]- QSequenceReader.detect ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun peekCurrentCharInSequence(): Char {
        return sequence!![offsetInSequence()]
    }

    // CallChain[size=19] = QSequenceReader.offsetInSequence() <-[Call]- QSequenceReader.detectSequence( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /**
     * 0 to sequence.size - 1
     */
    fun offsetInSequence(): Int {
        return offset - sequenceOffset
    }

    // CallChain[size=18] = QSequenceReader.detectSequence() <-[Call]- QBetween.find() <-[Call]- String. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /**
     * If sequence is detected, move offset by the length of the sequence.
     * If no sequence is found, offset remains unchanged.
     */
    fun detectSequence(sequence: CharArray, eofAllowed: Boolean = false): Boolean {
        if (!startReadingSequence(sequence)) return false

        while (hasNextCharInSequence()) {
            val seqChar = peekCurrentCharInSequence()
            val ch = nextChar()

            if (ch != seqChar) {
                endReadingSequence(false)
                return eofAllowed && isOffsetEOF()
            }
        }

        return if (offsetInSequence() == sequence.size) {
            endReadingSequence(true)
            true
        } else {
            val success = eofAllowed && isOffsetEOF()
            endReadingSequence(success)
            success
        }
    }
}

// CallChain[size=19] = QCharReader <-[Call]- QSequenceReader <-[Call]- QBetween.find() <-[Call]- St ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private open class QCharReader(val text: CharSequence) {
    // CallChain[size=20] = QCharReader.offset <-[Propag]- QCharReader <-[Call]- QSequenceReader <-[Call ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var offset = 0

    // CallChain[size=20] = QCharReader.lineNumber() <-[Propag]- QCharReader <-[Call]- QSequenceReader < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun lineNumber(): Int {
        // Consider caret to be between the character on the offset and the character preceding it
        //
        // ex. ( [ ] indicate offsets )
        // [\n]abc\n --> 1
        // \n[\n] --> 2

        var lineBreakCount = 0

        var tmpOffset = offset

        while (tmpOffset >= 0) {
            if (tmpOffset != offset && text[tmpOffset] == '\n') {
                lineBreakCount++
            }

            tmpOffset--
        }

        return lineBreakCount + 1
    }

    // CallChain[size=20] = QCharReader.countIndentSpaces() <-[Propag]- QCharReader <-[Call]- QSequenceR ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun countIndentSpaces(space: Char = ' '): Int {
        var count = 0

        var tmpOffset = offset

        // read backward until previous line break
        while (tmpOffset >= 0) {
            if (text[tmpOffset] == '\n') {
                tmpOffset++
                break
            }

            tmpOffset--
        }

        var ch: Char

        while (true) {
            ch = text[tmpOffset]
            if (ch == space) {
                count++
            } else if (ch == '\n') {
                break
            } else {
                continue
            }

            tmpOffset--

            if (tmpOffset == -1)
                break
        }

        return count
    }

    // CallChain[size=20] = QCharReader.hasNextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun hasNextChar(len: Int = 1): Boolean {
        return offset + len - 1 < text.length
    }

    // CallChain[size=20] = QCharReader.isOffsetEOF() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun isOffsetEOF(): Boolean {
        return offset == text.length - 1
    }

    // CallChain[size=20] = QCharReader.isValidOffset() <-[Propag]- QCharReader <-[Call]- QSequenceReade ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun isValidOffset(): Boolean {
        return 0 <= offset && offset < text.length
    }

    // CallChain[size=20] = QCharReader.hasPreviousChar() <-[Propag]- QCharReader <-[Call]- QSequenceRea ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun hasPreviousChar(len: Int = 1): Boolean {
        return 0 < offset - len + 1
    }

    // CallChain[size=20] = QCharReader.previousChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun previousChar(len: Int = 1) {
        offset -= len
    }

    // CallChain[size=20] = QCharReader.currentChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun currentChar(): Char {
        return text[offset]
    }

    // CallChain[size=20] = QCharReader.peekNextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun peekNextChar(): Char {
        return text[offset]
    }

    // CallChain[size=20] = QCharReader.moveOffset() <-[Propag]- QCharReader <-[Call]- QSequenceReader < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    inline fun moveOffset(plus: Int = 1) {
        offset += plus
    }

    // CallChain[size=20] = QCharReader.nextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    /**
     * Read current offset char and add offset by 1.
     */
    inline fun nextChar(): Char {
        return text[offset++]
    }
}

// CallChain[size=17] = QBetween <-[Call]- String.qFindBetween() <-[Call]- QMaskBetween.applyMore()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QBetween(
    val startSequence: String,
    val endSequence: String,
    val nestStartSequence: String? = if (startSequence != endSequence) {
        startSequence // can have nested structure
    } else {
        null // no nested structure
    },
    val escapeChar: Char? = null,
    val allowEOFEnd: Boolean = false,
    val nestingDepth: Int = 1,
    val regionIncludeStartAndEndSequence: Boolean = false,
) {

    // CallChain[size=17] = QBetween.find() <-[Call]- String.qFindBetween() <-[Call]- QMaskBetween.apply ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun find(text: CharSequence): List<QRegion> {
        val reader = QSequenceReader(text)

        val ranges: MutableList<QRegion> = mutableListOf()

        val startChArr = startSequence.toCharArray()
        val nestStartChArr = nestStartSequence?.toCharArray()
        val endChArr = endSequence.toCharArray()

        var nNest = 0

        var startSeqOffset = -1

        while (reader.hasNextChar()) {
            val ch = reader.peekNextChar()

            if (ch == escapeChar) {
                reader.moveOffset(2)
                continue
            } else {

                val startSequenceDetected = if (nNest == 0) {
                    reader.detectSequence(startChArr, allowEOFEnd)
                } else if (nestStartChArr != null) {
                    reader.detectSequence(nestStartChArr, allowEOFEnd)
                } else {
                    false
                }

                if (startSequenceDetected) {
                    nNest++

                    if (nestingDepth == nNest) {
                        startSeqOffset = reader.offset
                    }
                } else if (nNest > 0 && reader.detectSequence(endChArr, allowEOFEnd)) {
                    if (nestingDepth == nNest) {
                        val endSeqOffset = reader.offset - endChArr.size // exclusive

                        ranges += if (!regionIncludeStartAndEndSequence) {
                            QRegion(startSeqOffset, endSeqOffset)
                        } else {
                            val end = min(endSeqOffset + endChArr.size, text.length)
                            QRegion(startSeqOffset - startChArr.size, end)
                        }
                    }

                    nNest--
                } else {
                    reader.moveOffset()
                }
            }
        }

        return ranges
    }
}

// CallChain[size=11] = qNow <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLoca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qNow: Long
    get() = System.currentTimeMillis()

// CallChain[size=2] = QTimeAndResult <-[Call]- QBlock.timeItWarmup()[Root]
private class QTimeAndResult(val index: Int, val time: Long, val nTry: Int = 1, val result: Any? = null) {
    // CallChain[size=2] = QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun str(printResult: Boolean, resultMaxLength: Int = 15): String {
        return if (result == Unit || !printResult) {
            "[${index + 1}]  ${(time / nTry.toDouble()).qFormatDuration().trim()}"
        } else {
            "[${index + 1}]  ${(time / nTry.toDouble()).qFormatDuration().trim()}   ${result?.toString()?.qWithMaxLength(resultMaxLength) ?: "null"}"
        }
    }
}


// ================================================================================
// endregion Src from Non-Root API Files -- Auto Generated by nyab.conf.QCompactLib.kt