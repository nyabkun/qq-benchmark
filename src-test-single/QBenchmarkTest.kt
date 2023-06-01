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
import java.lang.NumberFormatException
import java.lang.StackWalker.StackFrame
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.name
import kotlin.io.path.reader
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random
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
// For this test, the source file is also a single-file, self-contained, and contains a runnable main function.
//  - It can be added to your codebase with a simple copy and paste.
//  - You can modify and redistribute it under the MIT License.
//  - Please add a package declaration if necessary.

// << Root of the CallChain >>
fun main() {
    qTestHumanCheck()
}

// << Root of the CallChain >>
class QBenchmarkTest {
    // << Root of the CallChain >>
    @QTestHumanCheckRequired(true)
    fun tryCatch() {
        qBenchmark {
            val rand = Random.Default
            val arrNumStr = (1..10000).map {
                rand.nextInt().toString()
            }

            block("String.toInt() with try ~ catch") {
                arrNumStr.map {
                    try {
                        it.toInt()
                    } catch(e: NumberFormatException) {
                        -1
                    }
                }
            }

            block("String.toInt() without try ~ catch") {
                arrNumStr.map {
                    it.toInt()
                }
            }
        }
    }

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
                    sb.append(i.toString())
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


// region Src from Non-Root API Files -- Auto Generated by nyab.conf.QCompactLib.kt
// ================================================================================


// CallChain[size=13] = QMyException <-[Ref]- QE <-[Ref]- qUnreachable() <-[Call]- QFetchRule.SINGLE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QMyException {
    // CallChain[size=14] = QMyException.Other <-[Call]- QException.QException() <-[Ref]- QE.throwIt() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Other,

    // CallChain[size=12] = QMyException.Unreachable <-[Call]- qUnreachable() <-[Call]- QFetchRule.SINGL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Unreachable,
    // CallChain[size=8] = QMyException.ShouldBeTrue <-[Call]- Boolean.qaTrue() <-[Call]- String.qWithMi ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    ShouldBeTrue,
    // CallChain[size=21] = QMyException.ShouldNotBeNull <-[Call]- T.qaNotNull() <-[Call]- qSrcFileAtFra ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldNotBeNull,
    // CallChain[size=11] = QMyException.ShouldNotBeZero <-[Call]- Int.qaNotZero() <-[Call]- CharSequenc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldNotBeZero,
    // CallChain[size=19] = QMyException.ShouldBeEvenNumber <-[Call]- qBrackets() <-[Call]- qMySrcLinesA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldBeEvenNumber,
    // CallChain[size=20] = QMyException.FileNotFound <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLine ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FileNotFound,
    // CallChain[size=20] = QMyException.FetchLinesFail <-[Call]- Path.qFetchLinesAround() <-[Call]- qSr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FetchLinesFail,
    // CallChain[size=21] = QMyException.LineNumberExceedsMaximum <-[Call]- Path.qLineAt() <-[Call]- Pat ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LineNumberExceedsMaximum,
    // CallChain[size=6] = QMyException.TrySetAccessibleFail <-[Call]- AccessibleObject.qTrySetAccessible() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    TrySetAccessibleFail,
    // CallChain[size=7] = QMyException.IllegalArgument <-[Call]- String.qWithMaxLength() <-[Call]- QTim ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    IllegalArgument,
    // CallChain[size=7] = QMyException.ConstructorNotFound <-[Call]- Class<T>.qConstructor() <-[Call]-  ... ce() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    ConstructorNotFound,
    // CallChain[size=4] = QMyException.TestFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    TestFail;

    
}

// CallChain[size=18] = QMyLog <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMyLog {
    // CallChain[size=18] = QMyLog.out <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * Default output stream
     */
    val out: QOut = QOut.CONSOLE

    // CallChain[size=9] = QMyLog.no_format <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call]- QO ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var no_format: Boolean = false
}

// CallChain[size=14] = QMyMark <-[Ref]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- qUn ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
@Suppress("MayBeConstant")
private object QMyMark {
    // CallChain[size=5] = QMyMark.test_method <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val test_method = "☕".yellow
    // CallChain[size=14] = QMyMark.warn <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val warn = "⚠".yellow
    // CallChain[size=4] = QMyMark.test_start <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val test_start = "☘".yellow
    
}

// CallChain[size=17] = QMyPath <-[Ref]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMyPath {
    // -- dirs

    // CallChain[size=18] = QMyPath.src <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src = "src".path
    // CallChain[size=18] = QMyPath.src_java <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_java = "src-java".path
    // CallChain[size=18] = QMyPath.src_build <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_build = "src-build".path
    // CallChain[size=18] = QMyPath.src_experiment <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_experiment = "src-experiment".path
    // CallChain[size=18] = QMyPath.src_plugin <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_plugin = "src-plugin".path
    // CallChain[size=18] = QMyPath.src_config <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_config = "src-config".path
    // CallChain[size=18] = QMyPath.src_test <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_test = "src-test".path
    // --- dir list
    // CallChain[size=17] = QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=11] = QMyToString <-[Call]- qToStringRegistry <-[Call]- Any.qToString() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMyToString {
    
}

// CallChain[size=12] = QE <-[Ref]- qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCu ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private typealias QE = QMyException

// CallChain[size=14] = qSTACK_FRAME_FILTER <-[Call]- QException.QException() <-[Ref]- QE.throwIt()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qSTACK_FRAME_FILTER: (StackWalker.StackFrame) -> Boolean = {
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

// CallChain[size=22] = String.qMatches() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qMatches(matcher: QM): Boolean = matcher.matches(this)

// CallChain[size=5] = QM.not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val QM.not: QM
    get() = QMatchNot(this)

// CallChain[size=6] = QMatchNot <-[Call]- QM.not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchNot(val matcher: QM) : QM {
    // CallChain[size=7] = QMatchNot.matches() <-[Propag]- QMatchNot <-[Call]- QM.not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(text: String): Boolean = !matcher.matches(text)

    // CallChain[size=7] = QMatchNot.toString() <-[Propag]- QMatchNot <-[Call]- QM.not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString() + "(matcher=$matcher)"
    }
}

// CallChain[size=22] = QMatchAny <-[Call]- QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchAny : QM {
    // CallChain[size=23] = QMatchAny.matches() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(text: String): Boolean {
        return true
    }

    // CallChain[size=23] = QMatchAny.toString() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=22] = QMatchNone <-[Call]- QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchNone : QM {
    // CallChain[size=23] = QMatchNone.matches() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(text: String): Boolean {
        return false
    }

    // CallChain[size=23] = QMatchNone.toString() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=21] = QM <-[Ref]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private interface QM {
    // CallChain[size=21] = QM.matches() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun matches(text: String): Boolean

    // CallChain[size=21] = QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isAny(): Boolean = this == QMatchAny

    // CallChain[size=21] = QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isNone(): Boolean = this == QMatchNone

    companion object {
        // CallChain[size=20] = QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun exact(text: String, ignoreCase: Boolean = false): QM = QExactMatch(text, ignoreCase)

        // CallChain[size=4] = QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        fun notExact(text: String, ignoreCase: Boolean = false): QM = QExactMatch(text, ignoreCase).not

        // CallChain[size=18] = QM.startsWith() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun startsWith(text: String, ignoreCase: Boolean = false): QM = QStartsWithMatch(text, ignoreCase)

        
    }
}

// CallChain[size=21] = QExactMatch <-[Call]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QExactMatch(val textExact: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=22] = QExactMatch.matches() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(text: String): Boolean {
        return text.equals(textExact, ignoreCase)
    }

    // CallChain[size=22] = QExactMatch.toString() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textExact=$textExact, ignoreCase=$ignoreCase)"
    }
}

// CallChain[size=19] = QStartsWithMatch <-[Call]- QM.startsWith() <-[Call]- QMyPath.src_root <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QStartsWithMatch(val textStartsWith: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=20] = QStartsWithMatch.matches() <-[Propag]- QStartsWithMatch <-[Call]- QM.startsW ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(text: String): Boolean {
        return text.startsWith(textStartsWith, ignoreCase)
    }

    // CallChain[size=20] = QStartsWithMatch.toString() <-[Propag]- QStartsWithMatch <-[Call]- QM.starts ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textStartsWith=$textStartsWith, ignoreCase=$ignoreCase)"
    }
}

// CallChain[size=12] = qAnd() <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any.qToS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qAnd(vararg matches: QMFunc): QMFunc = QMatchFuncAnd(*matches)

// CallChain[size=11] = QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any.qToString() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private infix fun QMFunc.and(match: QMFunc): QMFunc {
    return if (this is QMatchFuncAnd) {
        QMatchFuncAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=13] = QMatchFuncAny <-[Call]- QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsI ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncAny : QMFuncA() {
    // CallChain[size=14] = QMatchFuncAny.matches() <-[Propag]- QMatchFuncAny <-[Call]- QMFunc.isAny() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=13] = QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtension ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncNone : QMFuncA() {
    // CallChain[size=14] = QMatchFuncNone.matches() <-[Propag]- QMatchFuncNone <-[Call]- QMFunc.isNone( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return false
    }
}

// CallChain[size=12] = QMatchFuncDeclaredOnly <-[Call]- QMFunc.DeclaredOnly <-[Call]- qToStringRegi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncDeclaredOnly : QMFuncA() {
    // CallChain[size=13] = QMatchFuncDeclaredOnly.declaredOnly <-[Propag]- QMatchFuncDeclaredOnly <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly = true

    // CallChain[size=13] = QMatchFuncDeclaredOnly.matches() <-[Propag]- QMatchFuncDeclaredOnly <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=12] = QMatchFuncIncludeExtensionsInClass <-[Call]- QMFunc.IncludeExtensionsInClass ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncIncludeExtensionsInClass : QMFuncA() {
    // CallChain[size=13] = QMatchFuncIncludeExtensionsInClass.includeExtensionsInClass <-[Propag]- QMat ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass = true

    // CallChain[size=13] = QMatchFuncIncludeExtensionsInClass.matches() <-[Propag]- QMatchFuncIncludeEx ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=12] = QMatchFuncAnd <-[Ref]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- An ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMatchFuncAnd(vararg match: QMFunc) : QMFuncA() {
    // CallChain[size=12] = QMatchFuncAnd.matchList <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val matchList = match

    // CallChain[size=13] = QMatchFuncAnd.declaredOnly <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=13] = QMatchFuncAnd.includeExtensionsInClass <-[Propag]- QMatchFuncAnd.matchList < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass: Boolean = matchList.any { it.includeExtensionsInClass }

    // CallChain[size=13] = QMatchFuncAnd.matches() <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFunc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=14] = QMFuncA <-[Call]- QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFun ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private abstract class QMFuncA : QMFunc {
    // CallChain[size=15] = QMFuncA.declaredOnly <-[Propag]- QMFuncA <-[Call]- QMatchFuncNone <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly: Boolean = false
    // CallChain[size=15] = QMFuncA.includeExtensionsInClass <-[Propag]- QMFuncA <-[Call]- QMatchFuncNon ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass: Boolean = false
}

// CallChain[size=12] = QMFunc <-[Ref]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private interface QMFunc {
    // CallChain[size=12] = QMFunc.declaredOnly <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val declaredOnly: Boolean

    // CallChain[size=12] = QMFunc.includeExtensionsInClass <-[Propag]- QMFunc.IncludeExtensionsInClass  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val includeExtensionsInClass: Boolean

    // CallChain[size=12] = QMFunc.matches() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun matches(value: KFunction<*>): Boolean

    // CallChain[size=12] = QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStri ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isAny(): Boolean = this == QMatchFuncAny

    // CallChain[size=12] = QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isNone(): Boolean = this == QMatchFuncNone

    companion object {
        // CallChain[size=11] = QMFunc.DeclaredOnly <-[Call]- qToStringRegistry <-[Call]- Any.qToString() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val DeclaredOnly: QMFunc = QMatchFuncDeclaredOnly

        // CallChain[size=11] = QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry <-[Call]- Any.qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        // TODO OnlyExtensionsInClass
        val IncludeExtensionsInClass: QMFunc = QMatchFuncIncludeExtensionsInClass

        

        // TODO vararg, nullability, param names, type parameter
        // TODO handle createType() more carefully

        // CallChain[size=11] = QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Call]- Any.qToString() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun nameExact(text: String, ignoreCase: Boolean = false): QMFunc {
            return QMatchFuncName(QM.exact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=12] = QMatchFuncName <-[Call]- QMFunc.nameExact() <-[Call]- qToStringRegistry <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMatchFuncName(val nameMatcher: QM) : QMFuncA() {
    // CallChain[size=13] = QMatchFuncName.matches() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return nameMatcher.matches(value.name)
    }

    // CallChain[size=13] = QMatchFuncName.toString() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameEx ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName + ":" + nameMatcher.toString()
    }
}

// CallChain[size=5] = QMMethod.not <-[Call]- QMMethod.notAnnotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val QMMethod.not: QMMethod
    get() = QMatchMethodNot(this)

// CallChain[size=4] = qAnd() <-[Call]- QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qAnd(vararg matches: QMMethod): QMMethod = QMatchMethodAnd(*matches)

// CallChain[size=5] = qOr() <-[Call]- QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qOr(vararg matches: QMMethod): QMMethod = QMatchMethodOr(*matches)

// CallChain[size=3] = QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private infix fun QMMethod.and(match: QMMethod): QMMethod {
    return if (this is QMatchMethodAnd) {
        QMatchMethodAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=4] = QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private infix fun QMMethod.or(match: QMMethod): QMMethod {
    return if (this is QMatchMethodOr) {
        QMatchMethodOr(*matchList, match)
    } else {
        qOr(this, match)
    }
}

// CallChain[size=6] = QMatchMethodNot <-[Call]- QMMethod.not <-[Call]- QMMethod.notAnnotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodNot(val matcher: QMMethod) : QMMethodA() {
    // CallChain[size=7] = QMatchMethodNot.matches() <-[Propag]- QMatchMethodNot <-[Call]- QMMethod.not  ... ll]- QMMethod.notAnnotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean = !matcher.matches(value)
}

// CallChain[size=5] = QMatchMethodAny <-[Call]- QMMethod.isAny() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private object QMatchMethodAny : QMMethodA() {
    // CallChain[size=6] = QMatchMethodAny.matches() <-[Propag]- QMatchMethodAny <-[Call]- QMMethod.isAny() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return true
    }
}

// CallChain[size=5] = QMatchMethodNone <-[Call]- QMMethod.isNone() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private object QMatchMethodNone : QMMethodA() {
    // CallChain[size=6] = QMatchMethodNone.matches() <-[Propag]- QMatchMethodNone <-[Call]- QMMethod.isNone() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return false
    }
}

// CallChain[size=4] = QMatchMethodDeclaredOnly <-[Call]- QMMethod.DeclaredOnly <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private object QMatchMethodDeclaredOnly : QMMethodA() {
    // CallChain[size=5] = QMatchMethodDeclaredOnly.declaredOnly <-[Propag]- QMatchMethodDeclaredOnly <-[Call]- QMMethod.DeclaredOnly <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override val declaredOnly = true

    // CallChain[size=5] = QMatchMethodDeclaredOnly.matches() <-[Propag]- QMatchMethodDeclaredOnly <-[Call]- QMMethod.DeclaredOnly <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return true
    }
}

// CallChain[size=4] = QMatchMethodAnd <-[Ref]- QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodAnd(vararg match: QMMethod) : QMMethodA() {
    // CallChain[size=4] = QMatchMethodAnd.matchList <-[Call]- QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val matchList = match

    // CallChain[size=5] = QMatchMethodAnd.declaredOnly <-[Propag]- QMatchMethodAnd.matchList <-[Call]- QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=5] = QMatchMethodAnd.matches() <-[Propag]- QMatchMethodAnd.matchList <-[Call]- QMMethod.and() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=5] = QMatchMethodOr <-[Ref]- QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodOr(vararg match: QMMethod) : QMMethodA() {
    // CallChain[size=5] = QMatchMethodOr.matchList <-[Call]- QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val matchList = match

    // CallChain[size=6] = QMatchMethodOr.declaredOnly <-[Propag]- QMatchMethodOr.matchList <-[Call]- QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=6] = QMatchMethodOr.matches() <-[Propag]- QMatchMethodOr.matchList <-[Call]- QMMethod.or() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return matchList.any { it.matches(value) }
    }
}

// CallChain[size=6] = QMMethodA <-[Call]- QMatchMethodNone <-[Call]- QMMethod.isNone() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private abstract class QMMethodA : QMMethod {
    // CallChain[size=7] = QMMethodA.declaredOnly <-[Propag]- QMMethodA <-[Call]- QMatchMethodNone <-[Ca ... od.isNone() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override val declaredOnly: Boolean = false
}

// CallChain[size=4] = QMMethod <-[Ref]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private interface QMMethod {
    // CallChain[size=4] = QMMethod.declaredOnly <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val declaredOnly: Boolean

    // CallChain[size=4] = QMMethod.matches() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    fun matches(value: Method): Boolean

    // CallChain[size=4] = QMMethod.isAny() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    fun isAny(): Boolean = this == QMatchMethodAny

    // CallChain[size=4] = QMMethod.isNone() <-[Propag]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    fun isNone(): Boolean = this == QMatchMethodNone

    companion object {
        // CallChain[size=3] = QMMethod.DeclaredOnly <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        val DeclaredOnly: QMMethod = QMatchMethodDeclaredOnly

        // CallChain[size=3] = QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        val NoParams: QMMethod = QMatchMethodParams(arrayOf())

        // TODO vararg, param names, type parameter

        // CallChain[size=3] = QMMethod.annotation() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        fun <T : Annotation> annotation(annotation: KClass<T>, matcher: (T) -> Boolean = { true }): QMMethod {
            return QMatchMethodAnnotation(annotation.java, matcher)
        }

        // CallChain[size=4] = QMMethod.notAnnotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        fun <T : Annotation> notAnnotation(annotation: KClass<T>, matcher: (T) -> Boolean = { true }): QMMethod {
            return QMatchMethodAnnotation(annotation.java, matcher).not
        }

        // CallChain[size=4] = QMMethod.annotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        fun annotation(annotationName: String): QMMethod {
            return QMatchMethodAnnotationName(QM.exact(annotationName))
        }

        // CallChain[size=3] = QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        fun nameNotExact(text: String, ignoreCase: Boolean = false): QMMethod {
            return QMatchMethodName(QM.notExact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=4] = QMatchMethodName <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodName(val nameMatcher: QM) : QMMethodA() {
    // CallChain[size=5] = QMatchMethodName.matches() <-[Propag]- QMatchMethodName <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        return nameMatcher.matches(value.name)
    }
}

// CallChain[size=4] = QMatchMethodParams <-[Call]- QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodParams(val params: Array<Class<*>?>) : QMMethodA() {
    // CallChain[size=5] = QMatchMethodParams.matches() <-[Propag]- QMatchMethodParams <-[Call]- QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        if (value.parameters.size != params.size)
            return false

        if (params.isEmpty())
            return true

        return params.withIndex().all { (idx, actualParam) ->
            // Number is assignable from Integer
            if (actualParam == null) {
                true
            } else {
                // Number is assignable from Integer
                value.parameters[idx].type.qIsAssignableFrom(actualParam)
            }
        }
    }
}

// CallChain[size=4] = QMatchMethodAnnotation <-[Call]- QMMethod.annotation() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodAnnotation<T : Annotation>(val annotation: Class<T>, val matcher: (T) -> Boolean) : QMMethodA() {
    // CallChain[size=5] = QMatchMethodAnnotation.matches() <-[Propag]- QMatchMethodAnnotation <-[Call]- QMMethod.annotation() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        val anno = value.getAnnotation(annotation) ?: return false

        return matcher(anno)
    }
}

// CallChain[size=5] = QMatchMethodAnnotationName <-[Call]- QMMethod.annotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchMethodAnnotationName(val nameMatcher: QM, val declaredAnnotationsOnly: Boolean = false) : QMMethodA() {
    // CallChain[size=6] = QMatchMethodAnnotationName.matches() <-[Propag]- QMatchMethodAnnotationName <-[Call]- QMMethod.annotation() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(value: Method): Boolean {
        val annotations = if (declaredAnnotationsOnly) {
            value.declaredAnnotations
        } else {
            value.annotations
        }

        return annotations.any { anno: Annotation ->
            nameMatcher.matches(anno.annotationClass.java.simpleName)
        }
    }
}

// CallChain[size=4] = QTest <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
private annotation class QTest(val testOnlyThis: Boolean = false)

// CallChain[size=2] = QTestHumanCheckRequired <-[Call]- QBenchmarkTest.cachedRegex()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
private annotation class QTestHumanCheckRequired(val testOnlyThis: Boolean = false)

// CallChain[size=4] = QBeforeEach <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
private annotation class QBeforeEach

// CallChain[size=4] = QAfterEach <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
private annotation class QAfterEach

// CallChain[size=6] = QTestResultElement <-[Ref]- QTestResult.QTestResult() <-[Call]- QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private data class QTestResultElement(val method: Method, val cause: Throwable?) {
    // CallChain[size=5] = QTestResultElement.success <-[Call]- QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val success: Boolean
        get() = cause == null
}

// CallChain[size=6] = List<QTestResultElement>.allTestedMethods <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val List<QTestResultElement>.allTestedMethods: String
    get() =
        "\n[${"Tested".light_blue}]\n" +
                this.joinToString("\n") {
                    if (it.success) {
                        it.method.qName().green
                    } else {
                        it.method.qName().light_red
                    }
                }

// CallChain[size=3] = QTestResult <-[Ref]- qTestHumanCheck() <-[Call]- main()[Root]
private class QTestResult(val elements: List<QTestResultElement>, val time: Long) {
    // CallChain[size=6] = QTestResult.targetClasses <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val targetClasses = elements.map { it.method.declaringClass.canonicalName }

    // CallChain[size=7] = QTestResult.numSuccess <-[Call]- QTestResult.str <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val numSuccess = elements.count { it.success }
    // CallChain[size=4] = QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val numFail = elements.count { !it.success }

    // CallChain[size=6] = QTestResult.str <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val str = qBrackets("Result".blue, "$numSuccess / ${numFail + numSuccess}", "Time".blue, time.qFormatDuration())

    // CallChain[size=5] = QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    fun printIt(out: QOut = QOut.CONSOLE) {
        out.separator(start = "")

        if (numFail > 0) {
            out.println("Fail ...".red)
            out.println(str)
            out.println(elements.allTestedMethods)

            elements.filter { !it.success }.forEach { ele ->
                out.separator()

                val cause = if (ele.cause != null && ele.cause is QException) {
                    ele.cause
                } else if (ele.cause?.cause != null && ele.cause.cause is QException) {
                    ele.cause.cause
                } else {
                    ele.cause
                }

                val causeStr = if (cause != null && cause is QException) {
                    cause.type.name
                } else if (cause != null) {
                    cause::class.simpleName ?: cause::class.java.simpleName
                } else {
                    "null"
                }

                val msg = qBrackets("Failed".light_blue, ele.method.name.red, "Cause".light_blue, causeStr.red)

                val mySrcAndMsg = if (cause != null && cause is QException) {
                    val stackColoringRegex = targetClasses.joinToString("|") { ta ->
                        """(.*($ta|${ta}Kt).*?)\("""
                    }.re

                    val stackStr = stackColoringRegex.replace(cause.mySrcAndStack, "$1".qColor(QShColor.Blue) + "(")

                    cause.message + "\n\n" + stackStr
                } else {
                    ""
                }

                if (mySrcAndMsg.isNotEmpty()) {
                    out.println(msg + "\n")
                    out.println(mySrcAndMsg)
                } else if (cause != null) {
                    out.println(msg)
                    out.println("StackTrace >>>>>")
                    out.println(cause.stackTraceToString())
                }
            }
        } else {
            out.println("${"✨".yellow} ${" Success ".green} ${"✨".yellow}".green + "\n")
            out.println(str)
            out.println(elements.allTestedMethods)
        }
    }
}

// CallChain[size=4] = qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qTestMethods(
    methodsToTest: List<Method>,
    beforeMethod: List<Method> = emptyList(),
    afterMethod: List<Method> = emptyList(),
    out: QOut = QOut.CONSOLE
): QTestResult {
    val results = mutableListOf<QTestResultElement>()

    val timeItResult = qTimeIt(quiet = true) {
        for (method in methodsToTest) {
            // "⭐"
            out.println(qSeparatorWithLabel("${QMyMark.test_method} " + method.qName(true)))

            method.qTrySetAccessible()

            val cause =
                if (method.qIsInstanceMethod()) {
                    val testInstance = method.declaringClass.qNewInstance()

                    try {
                        for (before in beforeMethod) {
                            before.invoke(testInstance)
                        }

                        method.invoke(testInstance)

                        for (after in afterMethod) {
                            after.invoke(testInstance)
                        }
                        null
                    } catch (e: Throwable) {
                        e
                    }
                } else {
                    try {
                        for (before in beforeMethod) {
                            before.invoke(null)
                        }

                        method.invoke(null)

                        for (after in afterMethod) {
                            after.invoke(null)
                        }
                        null
                    } catch (e: Throwable) {
                        e
                    }
                }

            results += if (cause?.cause != null && cause is InvocationTargetException) {
                QTestResultElement(method, cause.cause)
            } else {
                QTestResultElement(method, cause)
            }
        }
    }

    val result = QTestResult(results, timeItResult.time)
    result.printIt()

    return result
}

// CallChain[size=3] = qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qTest(
    vararg targetClasses: Class<*> = arrayOf(qThisFileMainClass),

    targetMethodFilter: QMMethod =
        (QMMethod.annotation(QTest::class) or QMMethod.annotation("Test")) and
                QMMethod.notAnnotation(QTestHumanCheckRequired::class) and
//                QMMethod.notAnnotation(QIgnore::class) and
                QMMethod.DeclaredOnly and
                QMMethod.NoParams and
                QMMethod.nameNotExact("main"),

    beforeMethodFilter: QMMethod =
        (
                QMMethod.annotation(QBeforeEach::class) or QMMethod.annotation("BeforeTest")
                        or QMMethod.annotation("BeforeEach")
                        or QMMethod.annotation("BeforeMethod")
                )
                and QMMethod.DeclaredOnly and QMMethod.NoParams and QMMethod.nameNotExact(
            "main"
        ),

    afterMethodFilter: QMMethod =
        (
                QMMethod.annotation(QAfterEach::class) or QMMethod.annotation("AfterTest")
                        or QMMethod.annotation("AfterEach")
                        or QMMethod.annotation("AfterMethod")
                ) and QMMethod.DeclaredOnly and QMMethod.NoParams and QMMethod.nameNotExact(
            "main"
        ),

    out: QOut = QOut.CONSOLE,

    throwsException: Boolean = true,
): QTestResult {
    val targets = targetClasses.joinToString(", ") { it.simpleName }

    out.separator()

    val callerFileName = qCallerFileName()

    val methodsToTestImmediately = targetClasses.flatMap { cls ->
        cls.qMethods().filter { method ->
            (QMMethod.DeclaredOnly and (
                    QMMethod.annotation(QTest::class) { it.testOnlyThis } or
                            QMMethod.annotation(QTestHumanCheckRequired::class) { it.testOnlyThis })).matches(method)
        }.sortedBy {
            it.name // TODO sort by line number
        }
    }

    val methodsToTest = methodsToTestImmediately.ifEmpty {
        targetClasses.flatMap {
            it.qMethods().filter { method ->
                targetMethodFilter.matches(method)
            }
        }.sortedBy {
            it.name // TODO sort by line number
        }
    }

    qLogStackFrames(
        // "🚀"
        msg = "${QMyMark.test_start} Test Start ${QMyMark.test_start}\n$targets".light_blue,
        style = QLogStyle.MSG_AND_STACK,
        frames = listOf(
            qStackFrameEntryMethod { frame ->
                frame.fileName == callerFileName
            }
        )
    )

    val before = targetClasses.flatMap {
        it.qMethods().filter { method ->
            beforeMethodFilter.matches(method)
        }
    }

    val after = targetClasses.flatMap {
        it.qMethods().filter { method ->
            afterMethodFilter.matches(method)
        }
    }

    val result = qTestMethods(methodsToTest, before, after)

    if (result.numFail > 0 && throwsException) {
        QE.TestFail.throwIt()
    } else {
        return result
    }
}

// CallChain[size=2] = qTestHumanCheck() <-[Call]- main()[Root]
private fun qTestHumanCheck(vararg targetClasses: Class<*> = arrayOf(qThisFileMainClass)): QTestResult {
    return qTest(
        targetClasses = targetClasses,
        targetMethodFilter =
        QMMethod.annotation(QTestHumanCheckRequired::class) and
//                QMMethod.notAnnotation(QIgnore::class) and
                QMMethod.DeclaredOnly and
                QMMethod.NoParams and
                QMMethod.nameNotExact("main")
    )
}

// CallChain[size=4] = QBenchDsl <-[Call]- QBenchmark <-[Ref]- qBenchmark() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
@DslMarker
private annotation class QBenchDsl

// CallChain[size=2] = qBenchmark() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qBenchmark(action: QBenchmark.() -> Unit) {
    val scope = QBenchmark()

    scope.action()

    scope.start()
}

// CallChain[size=3] = QBenchmark <-[Ref]- qBenchmark() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
@QBenchDsl
private class QBenchmark {
    // CallChain[size=3] = QBenchmark.blocks <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private val blocks = mutableListOf<QBlock>()

    // CallChain[size=2] = QBenchmark.nTry <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * Number of trials
     */
    var nTry: Int = 100

    // CallChain[size=2] = QBenchmark.nSingleMeasureLoop <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * If this instance has several [block]s, it will shuffle them in randomized order and measure the time.
     * [nSingleMeasureLoop] represents how many times a block is executed in one measurement.
     * Eventually, the code snippet in the [block] will be executed [nSingleMeasureLoop] * [nTry] times.
     */
    var nSingleMeasureLoop: Int = 1

    // CallChain[size=2] = QBenchmark.nWarmUpTry <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * In the early executions, the execution of the [block] takes more time,
     * so we first perform some executions which are not counted in the measurements.
     */
    var nWarmUpTry: Int = 100

    // CallChain[size=2] = QBenchmark.out <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var out: QOut = QOut.CONSOLE

    // CallChain[size=2] = QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    
}

// CallChain[size=3] = QBenchmark.start() <-[Call]- qBenchmark() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=3] = QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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
    // CallChain[size=4] = QBlockLoop.timeAverage <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val timeAverage: Double
        get() = timeTotal / (nMeasureTried * nSingleMeasureLoop).toDouble()

    // CallChain[size=4] = QBlockLoop.timeStandardDeviation <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val timeStandardDeviation: Double
        get() = stat.σ / nSingleMeasureLoop

    // CallChain[size=4] = QBlockLoop.timeMedian <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val timeMedian: Double
        get() = stat.median / nSingleMeasureLoop
}

// CallChain[size=3] = QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private open class QBlock(
    val label: String,
    val task: (callCount: Long) -> Any?,
) {
    // CallChain[size=4] = QBlock.stat <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    protected val stat: QOnePassStat = QOnePassStat()

    // CallChain[size=4] = QBlock.resultFirst <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var resultFirst: Any? = Unit

    // CallChain[size=4] = QBlock.timeTotal <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var timeTotal: Long = 0

    // CallChain[size=4] = QBlock.nWarmupCall <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var nWarmupCall: Long = 0

    // CallChain[size=4] = QBlock.nMeasureTried <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var nMeasureTried: Long = 0

    // CallChain[size=4] = QBlock.nCallTotal <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val nCallTotal: Long
        get() = nWarmupCall + nMeasureTried

    // CallChain[size=4] = QBlock.resultsWarmup <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val resultsWarmup = mutableListOf<QTimeAndResult>()

    // CallChain[size=4] = QBlock.timeAverage <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    open val timeAverage: Double
        get() = timeTotal / nMeasureTried.toDouble()

    // CallChain[size=4] = QBlock.timeStandardDeviation <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    open val timeStandardDeviation: Double
        get() = stat.σ

    // CallChain[size=4] = QBlock.timeMedian <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    open val timeMedian: Double
        get() = stat.median

    // CallChain[size=4] = QBlock.timeItWarmup() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=4] = QBlock.timeIt() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=4] = QBlock.summary() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun summary(): String = "$label    [Average]  ${timeAverage.toLong().qFormatDuration()}"

    // CallChain[size=4] = QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=6] = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL <-[Call]- QCacheMap.QCacheMap() < ... edThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL = 1000L

// CallChain[size=4] = qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qThreadLocalCache: ThreadLocal<QCacheMap> by lazy {
    ThreadLocal.withInitial {
        QCacheMap(
            qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL
        )
    }
}

// CallChain[size=4] = qCacheThreadSafe <-[Call]- qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qCacheThreadSafe: QCacheMap by lazy { QCacheMap(qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL, true) }

// CallChain[size=2] = qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <K : Any, V : Any?> qCacheItOneSec(key: K, block: () -> V): V = qCacheItTimed(key, 1000L, block)

// CallChain[size=3] = qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <K : Any, V : Any?> qCacheItTimed(key: K, duration: Long, block: () -> V): V =
    qCacheThreadSafe.getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=2] = qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <K : Any, V : Any> qCacheItOneSecThreadLocal(key: K, block: () -> V): V =
    qCacheItTimedThreadLocal(key, 1000L, block)

// CallChain[size=3] = qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <K : Any, V : Any> qCacheItTimedThreadLocal(key: K, duration: Long, block: () -> V): V =
    qThreadLocalCache.get().getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=5] = QCacheMap <-[Ref]- qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QCacheMap(
    val expirationCheckInterval: Long = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL,
    val threadSafe: Boolean = false
) {
    // CallChain[size=5] = QCacheMap.lastCheck <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var lastCheck: Long = -1
    // CallChain[size=5] = QCacheMap.lock <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val lock: ReentrantLock = ReentrantLock()
    // CallChain[size=5] = QCacheMap.map <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val map: MutableMap<Any, QCacheEntry> = mutableMapOf()

    // CallChain[size=5] = QCacheMap.clearExpired() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun clearExpired(): Int = lock.qWithLock(threadSafe) {
        val toRemove = map.filterValues { it.isExpired() }
        toRemove.forEach { map.remove(it.key) }
        return toRemove.count()
    }

    // CallChain[size=4] = QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun getOrPut(key: Any, defaultValue: () -> QCacheEntry): QCacheEntry = lock.qWithLock(threadSafe) {
        val now = qNow
        if (now - lastCheck > expirationCheckInterval) {
            lastCheck = now
            clearExpired()
        }

        map.getOrPut(key, defaultValue)
    }
}

// CallChain[size=4] = QCacheEntry <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private data class QCacheEntry(val value: Any?, val duration: Long, val creationTime: Long = qNow) {
    // CallChain[size=6] = QCacheEntry.isExpired() <-[Call]- QCacheMap.clearExpired() <-[Call]- QCacheMa ... edThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isExpired() = (qNow - creationTime) > duration
}

// CallChain[size=5] = Lock.qWithLock() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private inline fun <T> Lock.qWithLock(threadSafe: Boolean, block: () -> T): T {
    return if (threadSafe) {
        withLock(block)
    } else {
        block()
    }
}

// CallChain[size=12] = QE.throwIt() <-[Call]- qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=21] = QE.throwItFile() <-[Call]- LineNumberReader.qFetchLinesAround() <-[Call]- Pa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun QE.throwItFile(path: Path, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets("File", path.absolutePathString()), e, stackDepth = stackDepth + 1)
}

// CallChain[size=19] = QE.throwItBrackets() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAtFrame() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun QE.throwItBrackets(vararg keysAndValues: Any?, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets(*keysAndValues), e, stackDepth = stackDepth + 1)
}

// CallChain[size=11] = qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qUnreachable(msg: Any? = ""): Nothing {
    QE.Unreachable.throwIt(msg)
}

// CallChain[size=13] = QException <-[Call]- QE.throwIt() <-[Call]- qUnreachable() <-[Call]- QFetchR ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QException(
    val type: QE = QE.Other,
    msg: String = QMyMark.warn,
    e: Throwable? = null,
    val stackDepth: Int = 0,
    stackSize: Int = 20,
    stackFilter: (StackWalker.StackFrame) -> Boolean = qSTACK_FRAME_FILTER,
    private val srcCut: QSrcCut = QSrcCut.MULTILINE_NOCUT,
) : RuntimeException(msg, e) {

    // CallChain[size=14] = QException.printStackTrace() <-[Propag]- QException.QException() <-[Ref]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun printStackTrace(s: PrintStream) {
        s.println("\n" + qToString() + "\n" + mySrcAndStack)
    }

    // CallChain[size=15] = QException.stackFrames <-[Call]- QException.getStackTrace() <-[Propag]- QExc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val stackFrames = qStackFrames(stackDepth + 2, size = stackSize, filter = stackFilter)

    // CallChain[size=15] = QException.mySrcAndStack <-[Call]- QException.printStackTrace() <-[Propag]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val mySrcAndStack: String by lazy {
        qLogStackFrames(frames = stackFrames, style = QLogStyle.SRC_AND_STACK, srcCut = srcCut, quiet = true)
            .qColorTarget(qRe("""\sshould[a-zA-Z]+"""), QShColor.LightYellow)
    }

    // CallChain[size=14] = QException.getStackTrace() <-[Propag]- QException.QException() <-[Ref]- QE.t ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun getStackTrace(): Array<StackTraceElement> {
        return stackFrames.map {
            it.toStackTraceElement()
        }.toTypedArray()
    }

    // CallChain[size=15] = QException.qToString() <-[Call]- QException.toString() <-[Propag]- QExceptio ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=14] = QException.toString() <-[Propag]- QException.QException() <-[Ref]- QE.throwI ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return qToString()
//         used by @Test
//        return type.name.yellow
    }
}

// CallChain[size=7] = Boolean.qaTrue() <-[Call]- String.qWithMinAndMaxLength() <-[Call]- qSeparator ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Boolean.qaTrue(exceptionType: QE = QE.ShouldBeTrue, msg: Any? = "") {
    if (!this) {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    }
}

// CallChain[size=20] = T.qaNotNull() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <T : Any> T?.qaNotNull(exceptionType: QE = QE.ShouldNotBeNull, msg: Any? = ""): T {
    if (this != null) {
        return this
    } else {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    }
}

// CallChain[size=10] = Int.qaNotZero() <-[Call]- CharSequence.qMask() <-[Call]- Any.qToLogString()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Int?.qaNotZero(exceptionType: QE = QE.ShouldNotBeZero, msg: Any? = ""): Int {
    if (this == null) {
        QE.ShouldNotBeNull.throwIt(stackDepth = 1, msg = msg)
    } else if (this == 0) {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    } else {
        return this
    }
}

// CallChain[size=21] = qBUFFER_SIZE <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qBUFFER_SIZE = DEFAULT_BUFFER_SIZE

// CallChain[size=21] = QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
// @formatter:off
private enum class QOpenOpt(val opt: OpenOption) : QFlagEnum<QOpenOpt> {
    // CallChain[size=23] = QOpenOpt.TRUNCATE_EXISTING <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    TRUNCATE_EXISTING(StandardOpenOption.TRUNCATE_EXISTING),
    // CallChain[size=23] = QOpenOpt.CREATE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CREATE(StandardOpenOption.CREATE),
    // CallChain[size=23] = QOpenOpt.CREATE_NEW <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CREATE_NEW(StandardOpenOption.CREATE_NEW),
    // CallChain[size=23] = QOpenOpt.WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    WRITE(StandardOpenOption.WRITE),
    // CallChain[size=23] = QOpenOpt.READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    READ(StandardOpenOption.READ),
    // CallChain[size=23] = QOpenOpt.APPEND <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    APPEND(StandardOpenOption.APPEND),
    // CallChain[size=23] = QOpenOpt.DELETE_ON_CLOSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DELETE_ON_CLOSE(StandardOpenOption.DELETE_ON_CLOSE),
    // CallChain[size=23] = QOpenOpt.DSYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DSYNC(StandardOpenOption.DSYNC),
    // CallChain[size=23] = QOpenOpt.SYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SYNC(StandardOpenOption.SYNC),
    // CallChain[size=23] = QOpenOpt.SPARSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SPARSE(StandardOpenOption.SPARSE),
    // CallChain[size=23] = QOpenOpt.EX_DIRECT <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt> ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    EX_DIRECT(ExtendedOpenOption.DIRECT),
    // CallChain[size=23] = QOpenOpt.EX_NOSHARE_DELETE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    EX_NOSHARE_DELETE(ExtendedOpenOption.NOSHARE_DELETE),
    // CallChain[size=23] = QOpenOpt.EX_NOSHARE_READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    EX_NOSHARE_READ(ExtendedOpenOption.NOSHARE_READ),
    // CallChain[size=23] = QOpenOpt.EX_NOSHARE_WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QO ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    EX_NOSHARE_WRITE(ExtendedOpenOption.NOSHARE_WRITE),
    // CallChain[size=23] = QOpenOpt.LN_NOFOLLOW_LINKS <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LN_NOFOLLOW_LINKS(LinkOption.NOFOLLOW_LINKS);

    companion object {
        
    }
}

// CallChain[size=21] = QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.qReader() <-[Call]- Path.qFetchL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun QFlag<QOpenOpt>.toOptEnums(): Array<OpenOption> {
    return toEnumValues().map { it.opt }.toTypedArray()
}

// CallChain[size=20] = Path.qLineSeparator() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=10] = QFetchEnd <-[Ref]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QFetchEnd {
    // CallChain[size=10] = QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE <-[Call]- QFetchRule.SMART_FET ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE,
    // CallChain[size=10] = QFetchEnd.END_WITH_THIS_LINE <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_THIS_LINE,
    // CallChain[size=11] = QFetchEnd.END_WITH_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_NEXT_LINE,
    // CallChain[size=11] = QFetchEnd.END_WITH_PREVIOUS_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_PREVIOUS_LINE
}

// CallChain[size=10] = QFetchStart <-[Ref]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QFetchStart {
    // CallChain[size=10] = QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE <-[Call]- QFetchRule.SMA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE,
    // CallChain[size=10] = QFetchStart.START_FROM_THIS_LINE <-[Call]- QFetchRule.SMART_FETCH <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_THIS_LINE,
    // CallChain[size=11] = QFetchStart.START_FROM_NEXT_LINE <-[Propag]- QFetchStart.START_FROM_THIS_LIN ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_NEXT_LINE,
    // CallChain[size=11] = QFetchStart.START_FROM_PREVIOUS_LINE <-[Propag]- QFetchStart.START_FROM_THIS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_PREVIOUS_LINE
}

// CallChain[size=10] = QFetchRuleA <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private abstract class QFetchRuleA(
    override val numLinesBeforeTargetLine: Int = 10,
    override val numLinesAfterTargetLine: Int = 10,
) : QFetchRule

// CallChain[size=11] = QFetchRule <-[Ref]- QFetchRuleA <-[Call]- QFetchRule.SMART_FETCH <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private interface QFetchRule {
    // CallChain[size=10] = QFetchRule.numLinesBeforeTargetLine <-[Propag]- QFetchRule.SMART_FETCH <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val numLinesBeforeTargetLine: Int
    // CallChain[size=10] = QFetchRule.numLinesAfterTargetLine <-[Propag]- QFetchRule.SMART_FETCH <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val numLinesAfterTargetLine: Int

    // CallChain[size=10] = QFetchRule.fetchStartCheck() <-[Propag]- QFetchRule.SMART_FETCH <-[Call]- QS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun fetchStartCheck(
        line: String,
        currentLineNumber: Int,
        targetLine: String,
        targetLineNumber: Int,
        context: MutableSet<String>,
    ): QFetchStart

    // CallChain[size=10] = QFetchRule.fetchEndCheck() <-[Propag]- QFetchRule.SMART_FETCH <-[Call]- QSrc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun fetchEndCheck(
        line: String,
        currentLineNumber: Int,
        targetLine: String,
        targetLineNumber: Int,
        context: MutableSet<String>,
    ): QFetchEnd

    companion object {
        // CallChain[size=10] = QFetchRule.SINGLE_LINE <-[Call]- QSrcCut <-[Call]- QSrcCut.UNTIL_qLog <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

        // CallChain[size=9] = QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=22] = LineNumberReader.qFetchLinesBetween() <-[Call]- LineNumberReader.qFetchTarge ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=22] = TargetSurroundingLines <-[Ref]- LineNumberReader.qFetchTargetSurroundingLine ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class TargetSurroundingLines(
    val targetLineNumber: Int,
    val startLineNumber: Int,
    val endLineNumber: Int,
    val targetLine: String,
    val linesBeforeTargetLine: List<String>,
    val linesAfterTargetLine: List<String>,
) {
    // CallChain[size=21] = TargetSurroundingLines.linesBetween() <-[Call]- LineNumberReader.qFetchLines ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=21] = LineNumberReader.qFetchTargetSurroundingLines() <-[Call]- LineNumberReader.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=20] = LineNumberReader.qFetchLinesAround() <-[Call]- Path.qFetchLinesAround() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=20] = Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Path.qReader(
    charset: Charset = Charsets.UTF_8,
    buffSize: Int = qBUFFER_SIZE,
    opts: QFlag<QOpenOpt> = QFlag.none(),
): LineNumberReader {
    return LineNumberReader(reader(charset, *opts.toOptEnums()), buffSize)
}

// CallChain[size=19] = Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=20] = Path.qLineAt() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=21] = QFType <-[Ref]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QFType {
    // CallChain[size=25] = QFType.Any <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Any,
    // CallChain[size=21] = QFType.File <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    File,
    // CallChain[size=25] = QFType.Dir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Dir,
    // CallChain[size=25] = QFType.SymLink <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Pa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SymLink,
    // CallChain[size=25] = QFType.FileOrDir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FileOrDir;

    // CallChain[size=24] = QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=20] = Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLines ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Collection<Path>.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    for (path in this) {
        val found = path.qFind(nameMatcher, type, maxDepth)
        if (found != null) return found
    }

    return null
}

// CallChain[size=21] = Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Path.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    return try {
        qList(type, maxDepth = maxDepth) {
            it.name.qMatches(nameMatcher)
        }.firstOrNull()
    } catch (e: NoSuchElementException) {
        null
    }
}

// CallChain[size=18] = Path.qListByMatch() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=22] = Path.qList() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=23] = Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path.qFind() <-[Call]- Collecti ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=21] = QFlag <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * Only Enum or QFlag can implement this interface.
 */
private sealed interface QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=23] = QFlag.bits <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val bits: Int

    // CallChain[size=23] = QFlag.contains() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun contains(flags: QFlag<T>): Boolean {
        return (bits and flags.bits) == flags.bits
    }

    // CallChain[size=22] = QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun toEnumValues(): List<T>

    // CallChain[size=23] = QFlag.str() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOpt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun str(): String {
        return toEnumValues().joinToString(", ") { it.name }
    }

    companion object {
        // https://discuss.kotlinlang.org/t/reified-generics-on-class-level/16711/2
        // But, can't make constructor private ...

        // CallChain[size=21] = QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        inline fun <reified T>
        none(): QFlag<T> where T : QFlag<T>, T : Enum<T> {
            return QFlagSet<T>(T::class, 0)
        }
    }
}

// CallChain[size=22] = QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLin ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private interface QFlagEnum<T> : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=23] = QFlagEnum.bits <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val bits: Int
        get() = 1 shl (this as T).ordinal
    // CallChain[size=23] = QFlagEnum.toEnumValues() <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Pa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toEnumValues(): List<T> = listOf(this) as List<T>
}

// CallChain[size=22] = QFlagSet <-[Call]- QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFet ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * Mutable bit flag
 */
private class QFlagSet<T>(val enumClass: KClass<T>, override var bits: Int) : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=24] = QFlagSet.enumValues <-[Call]- QFlagSet.toEnumValues() <-[Propag]- QFlagSet < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val enumValues: Array<T> by lazy { enumClass.qEnumValues() }

    // CallChain[size=23] = QFlagSet.toEnumValues() <-[Propag]- QFlagSet <-[Call]- QFlag.none() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toEnumValues(): List<T> =
        enumValues.filter { contains(it) }
}

// CallChain[size=5] = Int.qFormat() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Int.qFormat(digits: Int = 4): String {
    return String.format("%${digits}d", this)
}

// CallChain[size=6] = QUnit <-[Ref]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QUnit {
    // CallChain[size=6] = QUnit.Nano <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Nano,
    // CallChain[size=6] = QUnit.Micro <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Micro,
    // CallChain[size=6] = QUnit.Milli <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Milli,
    // CallChain[size=6] = QUnit.Second <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Second,
    // CallChain[size=6] = QUnit.Minute <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Minute,
    // CallChain[size=6] = QUnit.Hour <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Hour,
    // CallChain[size=6] = QUnit.Day <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Day
}

// CallChain[size=5] = Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Long.qFormatDuration(unit: QUnit = QUnit.Nano): String {
    return when (unit) {
        QUnit.Milli ->
            Duration.ofMillis(this).qFormat()
        QUnit.Micro ->
            Duration.ofNanos(this * 1000).qFormat()
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

// CallChain[size=7] = Duration.qToMicrosOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qFor ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Duration.qToMicrosOnlyPart(): Int {
    return (toNanosPart() % 1_000_000) / 1_000
}

// CallChain[size=7] = Duration.qToNanoOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qForma ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Duration.qToNanoOnlyPart(): Int {
    return toNanosPart() % 1_000
}

// CallChain[size=5] = Double.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Double.qFormatDuration(): String =
    toLong().qFormatDuration()

// CallChain[size=6] = Duration.qFormat() <-[Call]- Long.qFormatDuration() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun Duration.qFormat(detail: Boolean = false): String {
    if(this.isZero) {
        return "0"
    }

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

// CallChain[size=20] = qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyl ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qARROW = "===>".light_cyan

// CallChain[size=9] = QSrcCut <-[Call]- QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Call]- QOnePassSta ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QSrcCut(
        val fetchRule: QFetchRule = QFetchRule.SINGLE_LINE,
        val cut: (srcLines: String) -> String,
) {
    companion object {
        // CallChain[size=18] = QSrcCut.CUT_PARAM_qLog <-[Call]- QSrcCut.SINGLE_qLog_PARAM <-[Call]- qLogSta ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        private val CUT_PARAM_qLog = { mySrc: String ->
            mySrc.replaceFirst("""(?s)^\s*(\S.+)\.qLog[a-zA-Z]{0,10}.*$""".re, "$1")
        }

        // CallChain[size=9] = QSrcCut.CUT_UNTIL_qLog <-[Call]- QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        private val CUT_UNTIL_qLog = { mySrc: String ->
            mySrc.replaceFirst("""(?s)(?m)^(\s*)(\S.+)\.qLog[a-zA-Z]{0,10}.*$""".re, "$1$2")
        }

        // CallChain[size=17] = QSrcCut.SINGLE_qLog_PARAM <-[Call]- qLogStackFrames() <-[Call]- QException.m ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val SINGLE_qLog_PARAM = QSrcCut(QFetchRule.SINGLE_LINE, CUT_PARAM_qLog)
        // CallChain[size=8] = QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val UNTIL_qLog = QSrcCut(QFetchRule.SMART_FETCH, CUT_UNTIL_qLog)
        // CallChain[size=14] = QSrcCut.MULTILINE_NOCUT <-[Call]- QException.QException() <-[Ref]- QE.throwI ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val MULTILINE_NOCUT = QSrcCut(QFetchRule.SMART_FETCH) { it }
        // CallChain[size=18] = QSrcCut.NOCUT_JUST_SINGLE_LINE <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLog ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val NOCUT_JUST_SINGLE_LINE = QSrcCut(QFetchRule.SINGLE_LINE) { it }
        
    }
}

// CallChain[size=17] = QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QException.mySrcAndStac ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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
        // CallChain[size=17] = QLogStyle.String.clarifySrcRegion() <-[Call]- QLogStyle.SRC_AND_STACK <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun String.clarifySrcRegion(onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
            if (!onlyIf.matches(this))
                return this

            return """${"SRC START ―――――――――――".qColor(QShColor.Cyan)}
${this.trim()}
${"SRC END   ―――――――――――".qColor(QShColor.Cyan)}"""
        }

        // CallChain[size=18] = QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]- qLogStackFrames() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

        // CallChain[size=16] = QLogStyle.SRC_AND_STACK <-[Call]- QException.mySrcAndStack <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val SRC_AND_STACK: QLogStyle
            get() = QLogStyle(1) { _, mySrc, _, stackTrace ->
                """
${mySrc.clarifySrcRegion(QOnlyIfStr.Always)}
$stackTrace""".trim()
            }

        // CallChain[size=4] = QLogStyle.MSG_AND_STACK <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
        val MSG_AND_STACK: QLogStyle
            get() = QLogStyle(1, start = "") { msg, _, _, stackTrace ->
                """
$msg
$stackTrace
""".trim()
            }

        // CallChain[size=17] = QLogStyle.S <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndStack < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val S: QLogStyle
            get() = QLogStyle(1) { msg, mySrc, _, stackTrace ->
                """
${qLogArrow(mySrc, msg)}
$stackTrace
""".trim()
            }

        
    }
}

// CallChain[size=7] = T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <T : Any?> T.qLog(stackDepth: Int = 0, quiet: Boolean = false): T {
    qLog(qToLogString(), stackDepth = stackDepth + 1, srcCut = QSrcCut.UNTIL_qLog, quiet = quiet)
    return this
}

// CallChain[size=17] = qMySrcLinesAtFrame() <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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
        "${QMyMark.warn} Couldn't cut src lines : ${qBrackets("FileName", frame.fileName, "LineNo", frame.lineNumber, "SrcRoots", srcRoots)}"
    }
}

// CallChain[size=16] = qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Call]- QException.pr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    val finalTxt = if (noColor) text.noStyle else text

    if (!quiet)
        style.out.print(finalTxt)

    return if (noColor) output.noStyle else output
}

// CallChain[size=8] = qLog() <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=18] = qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFra ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=19] = qArrow() <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]- qLo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qArrow(key: Any?, value: Any?): String {
    val keyStr = key.qToLogString()
            .qWithNewLinePrefix(onlyIf = QOnlyIfStr.Multiline)
            .qWithNewLineSuffix(onlyIf = QOnlyIfStr.Always)

    val valStr = value.qToLogString().qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)
            .qWithSpacePrefix(numSpace = 2, onlyIf = QOnlyIfStr.SingleLine)

    return "$keyStr$qARROW$valStr"
}

// CallChain[size=19] = String.qBracketEnd() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAtFrame() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=19] = String.qBracketStartOrMiddle() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=18] = qBrackets() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFrames() <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=5] = QOnePassStat <-[Ref]- QBlock.stat <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * Basically, stats are calculated by one-pass algorithm,
 * but some data must be kept in memory to calculate the median.
 */
@Suppress("NonAsciiCharacters", "PropertyName")
private class QOnePassStat(val maxSamplesInHeap: Int = 10_000) {
    // CallChain[size=7] = QOnePassStat.n <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.median <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var n: Long = 0
    // CallChain[size=6] = QOnePassStat.μ <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var μ: Double = .0
    // CallChain[size=7] = QOnePassStat.m2 <-[Call]- QOnePassStat.σ2 <-[Call]- QOnePassStat.σ <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var m2: Double = .0
    // CallChain[size=6] = QOnePassStat.σ2 <-[Call]- QOnePassStat.σ <-[Call]- QBlockLoop.timeStandardDev ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val σ2: Double
        get() = m2 / n
    // CallChain[size=6] = QOnePassStat.max <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var max: Double = .0
    // CallChain[size=6] = QOnePassStat.min <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var min: Double = .0
    // CallChain[size=5] = QOnePassStat.σ <-[Call]- QBlockLoop.timeStandardDeviation <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val σ: Double
        get() = sqrt(σ2)
    // CallChain[size=7] = QOnePassStat.dataHead <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.medi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    // We should keep data in memory to calculate median
    private val dataHead = QFixedSizeList<Double>(maxSamplesInHeap / 2)
    // CallChain[size=7] = QOnePassStat.dataTail <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat.medi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private val dataTail = QFixedSizeQueue<Double>(maxSamplesInHeap / 2)

    // CallChain[size=6] = QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlockLoop.timeMedian <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=5] = QOnePassStat.median <-[Call]- QBlockLoop.timeMedian <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val median: Double
        get() = data.qMedian()

    // CallChain[size=5] = QOnePassStat.add() <-[Call]- QBlock.timeIt() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = QFixedSizeList <-[Call]- QOnePassStat.dataHead <-[Call]- QOnePassStat.data <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QFixedSizeList<T>(val maxSize: Int, val backend: MutableList<T> = ArrayList(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=9] = QFixedSizeList.add() <-[Propag]- QFixedSizeList <-[Call]- QOnePassStat.dataHe ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun add(element: T): Boolean {
        return if (this.size < maxSize) {
            backend.add(element)
        } else {
            false
        }
    }
}

// CallChain[size=8] = QFixedSizeQueue <-[Call]- QOnePassStat.dataTail <-[Call]- QOnePassStat.data < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QFixedSizeQueue<T>(val maxSize: Int, val backend: ArrayDeque<T> = ArrayDeque(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=9] = QFixedSizeQueue.add() <-[Propag]- QFixedSizeQueue <-[Call]- QOnePassStat.data ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=6] = DoubleArray.qMedian() <-[Call]- QOnePassStat.median <-[Call]- QBlockLoop.time ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun DoubleArray.qMedian(): Double =
    sorted().let {
        if (it.size % 2 == 0)
            (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
        else
            it[it.size / 2]
    }

// CallChain[size=18] = QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QExceptio ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
interface QOut {
    // CallChain[size=19] = QOut.isAcceptColoredText <-[Propag]- QOut <-[Ref]- QLogStyle <-[Ref]- QLogSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val isAcceptColoredText: Boolean

    // CallChain[size=19] = QOut.print() <-[Propag]- QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun print(msg: Any? = "")

    // CallChain[size=19] = QOut.println() <-[Propag]- QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AN ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun println(msg: Any? = "")

    // CallChain[size=19] = QOut.close() <-[Propag]- QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun close()

    companion object {
        // CallChain[size=2] = QOut.CONSOLE <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val CONSOLE: QOut = QConsole(true)

        
    }
}

// CallChain[size=4] = QOut.separator() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun QOut.separator(start: String = "\n", end: String = "\n") {
    this.println(qSeparator(start = start, end = end))
}

// CallChain[size=3] = QConsole <-[Call]- QOut.CONSOLE <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QConsole(override val isAcceptColoredText: Boolean) : QOut {
    // CallChain[size=4] = QConsole.print() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun print(msg: Any?) {
        if (isAcceptColoredText) {
            kotlin.io.print(msg.toString())
        } else {
            kotlin.io.print(msg.toString().noStyle)
        }
    }

    // CallChain[size=4] = QConsole.println() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun println(msg: Any?) {
        kotlin.io.println(msg.toString())
    }

    // CallChain[size=4] = QConsole.close() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun close() {
        // Do nothing
    }
}

// CallChain[size=5] = Method.qName() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Method.qName(withParenthesis: Boolean = false): String {
    val clsName = declaringClass.simpleName

    val name = if (clsName.isNotEmpty()) {
        "$clsName.$name"
    } else {
        name
    }

    return if (withParenthesis) {
        "$name()"
    } else {
        name
    }
}

// CallChain[size=11] = KClass<*>.qFunctions() <-[Call]- qToStringRegistry <-[Call]- Any.qToString() ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=25] = KClass<E>.qEnumValues() <-[Call]- QFlagSet.enumValues <-[Call]- QFlagSet.toE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun <E : Enum<E>> KClass<E>.qEnumValues(): Array<E> {
    return java.enumConstants as Array<E>
}

// CallChain[size=6] = qThisSrcLineSignature <-[Call]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val qThisSrcLineSignature: String
    get() = qCallerSrcLineSignature()

// CallChain[size=19] = qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=4] = qCallerFileName() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qCallerFileName(stackDepth: Int = 0): String {
    return qStackFrame(stackDepth + 2).fileName
}

// CallChain[size=7] = qCallerSrcLineSignature() <-[Call]- qThisSrcLineSignature <-[Call]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qCallerSrcLineSignature(stackDepth: Int = 0): String {
    val frame = qStackFrame(stackDepth + 2)

    return if (frame.declaringClass?.canonicalName == null) {
        if (frame.fileName != null) {
            if (frame.methodName != "invokeSuspend") {
                frame.fileName + " - " + frame.methodName + " - " + frame.lineNumber
            } else {
                frame.fileName + " - " + frame.lineNumber
            }
        } else {
            frame.methodName + " - " + frame.lineNumber
        }
    } else {
        frame.declaringClass.canonicalName + "." + frame.methodName + " - " + frame.lineNumber
    }
}

// CallChain[size=16] = qStackFrames() <-[Call]- QException.stackFrames <-[Call]- QException.getStac ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private inline fun qStackFrames(
        stackDepth: Int = 0,
        size: Int = 1,
        noinline filter: (StackFrame) -> Boolean = qSTACK_FRAME_FILTER,
): List<StackFrame> {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { s: Stream<StackFrame> ->
        s.asSequence().filter(filter).drop(stackDepth).take(size).toList()
    }
}

// CallChain[size=19] = qStackFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFrame( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private inline fun qStackFrame(
        stackDepth: Int = 0,
        noinline filter: (StackFrame) -> Boolean = qSTACK_FRAME_FILTER,
): StackFrame {
    return qStackFrames(stackDepth, 1, filter)[0]
}

// CallChain[size=4] = qStackFrameEntryMethod() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qStackFrameEntryMethod(filter: (StackFrame) -> Boolean): StackFrame {
    return qStackFrames(0, Int.MAX_VALUE)
            .filter(filter)
            .findLast {
                it.lineNumber > 0
            }.qaNotNull()
}

// CallChain[size=12] = KType.qToClass() <-[Call]- KType.qIsSuperclassOf() <-[Call]- qToStringRegist ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun KType.qToClass(): KClass<*>? {
    return if (this.classifier != null && this.classifier is KClass<*>) {
        this.classifier as KClass<*>
    } else {
        null
    }
}

// CallChain[size=11] = KType.qIsSuperclassOf() <-[Call]- qToStringRegistry <-[Call]- Any.qToString( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=5] = AccessibleObject.qTrySetAccessible() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun AccessibleObject.qTrySetAccessible() {
    try {
        if (!this.trySetAccessible()) {
            QE.TrySetAccessibleFail.throwIt(this)
        }
    } catch (e: SecurityException) {
        QE.TrySetAccessibleFail.throwIt(this, e = e)
    }
}

// CallChain[size=3] = qThisFileMainClass <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val qThisFileMainClass: Class<*>
    get() = qCallerFileMainClass()

// CallChain[size=4] = Class<*>.qMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Class<*>.qMethods(matcher: QMMethod = QMMethod.DeclaredOnly): List<Method> {
    val allMethods = if (matcher.declaredOnly) declaredMethods else methods
    return allMethods.filter { matcher.matches(it) }
}

// CallChain[size=5] = Class<T>.qNewInstance() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun <T : Any> Class<T>.qNewInstance(vararg params: Any): T {
    val constructor = qConstructor(*params, declaredOnly = false)
    return constructor.newInstance()
}

// CallChain[size=6] = Class<T>.qConstructor() <-[Call]- Class<T>.qNewInstance() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun <T : Any> Class<T>.qConstructor(vararg params: Any, declaredOnly: Boolean = false): Constructor<T> {
    return if (declaredOnly) {
        this.getDeclaredConstructor(*params.map { it::class.java }.toTypedArray())
            .qaNotNull(QE.ConstructorNotFound)
    } else {
        this.getConstructor(*params.map { it::class.java }.toTypedArray())
            .qaNotNull(QE.ConstructorNotFound)
    }
}

// CallChain[size=7] = Class<*>.qPrimitiveToWrapper() <-[Call]- Class<*>.qIsAssignableFrom() <-[Call ...  QMatchMethodParams <-[Call]- QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Class<*>.qPrimitiveToWrapper(): Class<*> = qJVMPrimitiveToWrapperMap[this] ?: this

// CallChain[size=8] = qJVMPrimitiveToWrapperMap <-[Call]- Class<*>.qPrimitiveToWrapper() <-[Call]-  ...  QMatchMethodParams <-[Call]- QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val qJVMPrimitiveToWrapperMap by lazy {
    val map = HashMap<Class<*>, Class<*>>()

    map[java.lang.Boolean.TYPE] = java.lang.Boolean::class.java
    map[java.lang.Byte.TYPE] = java.lang.Byte::class.java
    map[java.lang.Character.TYPE] = java.lang.Character::class.java
    map[java.lang.Short.TYPE] = java.lang.Short::class.java
    map[java.lang.Integer.TYPE] = java.lang.Integer::class.java
    map[java.lang.Long.TYPE] = java.lang.Long::class.java
    map[java.lang.Double.TYPE] = java.lang.Double::class.java
    map[java.lang.Float.TYPE] = java.lang.Float::class.java
    map
}

// CallChain[size=6] = Class<*>.qIsAssignableFrom() <-[Call]- QMatchMethodParams.matches() <-[Propag]- QMatchMethodParams <-[Call]- QMMethod.NoParams <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Class<*>.qIsAssignableFrom(subclass: Class<*>, autoboxing: Boolean = true): Boolean {
    return if (autoboxing) {
        this.qPrimitiveToWrapper().isAssignableFrom(subclass.qPrimitiveToWrapper())
    } else {
        this.isAssignableFrom(subclass)
    }
}

// CallChain[size=5] = Method.qIsInstanceMethod() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun Method.qIsInstanceMethod(): Boolean {
    return !Modifier.isStatic(this.modifiers)
}

// CallChain[size=4] = qCallerFileMainClass() <-[Call]- qThisFileMainClass <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qCallerFileMainClass(stackDepth: Int = 0): Class<*> {
    val frame = qStackFrame(stackDepth + 2)
    val fileName = frame.fileName.substring(0, frame.fileName.lastIndexOf("."))
    val pkgName = frame.declaringClass.packageName

    val clsName = if (fileName.endsWith(".kt", true)) {
        if (pkgName.isNotEmpty())
            "$pkgName.${fileName}Kt"
        else
            "${fileName}Kt"
    } else {
        if (pkgName.isNotEmpty())
            "$pkgName.$fileName"
        else
            fileName
    }

    return Class.forName(clsName)
}

// CallChain[size=12] = RO <-[Ref]- qRe() <-[Call]- @receiver:Language("RegExp") String.re <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private typealias RO = RegexOption

// CallChain[size=11] = qRe() <-[Call]- @receiver:Language("RegExp") String.re <-[Call]- QSrcCut.CUT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qRe(@Language("RegExp") regex: String, vararg opts: RO): Regex {
    return qCacheItOneSecThreadLocal(regex + opts.contentToString()) {
        Regex(regex, setOf(*opts))
    }
}

// CallChain[size=10] = @receiver:Language("RegExp") String.re <-[Call]- QSrcCut.CUT_UNTIL_qLog <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
// https://youtrack.jetbrains.com/issue/KTIJ-5643
private val @receiver:Language("RegExp") String.re: Regex
    get() = qRe(this)

// CallChain[size=19] = qBG_JUMP <-[Call]- QShColor.bg <-[Propag]- QShColor.Yellow <-[Call]- String. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qBG_JUMP = 10

// CallChain[size=19] = qSTART <-[Call]- QShColor.bg <-[Propag]- QShColor.Yellow <-[Call]- String.ye ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qSTART = "\u001B["

// CallChain[size=20] = qEND <-[Call]- String.qApplyEscapeLine() <-[Call]- String.qApplyEscapeLine() ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qEND = "${qSTART}0m"

// CallChain[size=20] = String.qApplyEscapeNestable() <-[Call]- String.qApplyEscapeLine() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qApplyEscapeNestable(start: String): String {
    val lastEnd = this.endsWith(qEND)

    return if( lastEnd ) {
            start + this.substring(0, this.length - 1).replace(qEND, qEND + start) + this[this.length - 1]
        } else {
            start + this.replace(qEND, qEND + start) + qEND
        }
}

// CallChain[size=17] = String.qColor() <-[Call]- String.yellow <-[Call]- QException.qToString() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qColor(fg: QShColor? = null, bg: QShColor? = null, nestable: Boolean = this.contains(qSTART)): String {
    return if (this.qIsSingleLine()) {
        this.qApplyEscapeLine(fg, bg, nestable)
    } else {
        lineSequence().map { line ->
            line.qApplyEscapeLine(fg, bg, nestable)
        }.joinToString("\n")
    }
}

// CallChain[size=18] = String.qApplyEscapeLine() <-[Call]- String.qColor() <-[Call]- String.yellow  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qApplyEscapeLine(fg: QShColor?, bg: QShColor?, nestable: Boolean): String {
    return this.qApplyEscapeLine(
        listOfNotNull(fg?.fg, bg?.bg).toTypedArray(),
        nestable
    )
}

// CallChain[size=19] = String.qApplyEscapeLine() <-[Call]- String.qApplyEscapeLine() <-[Call]- Stri ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qApplyEscapeLine(
    startSequences: Array<String>,
    nestable: Boolean
): String {
    val nest = nestable && this.contains(qEND)

    var text = this

    for (start in startSequences) {
        text = if (nest) {
            text.qApplyEscapeNestable(start)
        } else {
            "$start$text$qEND"
        }
    }

    return text
}

// CallChain[size=17] = QShColor <-[Ref]- String.yellow <-[Call]- QException.qToString() <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QShColor(val code: Int) {
    // CallChain[size=18] = QShColor.Black <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Black(30),
    // CallChain[size=18] = QShColor.Red <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Red(31),
    // CallChain[size=18] = QShColor.Green <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Green(32),
    // CallChain[size=17] = QShColor.Yellow <-[Call]- String.yellow <-[Call]- QException.qToString() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Yellow(33),
    // CallChain[size=18] = QShColor.Blue <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Blue(34),
    // CallChain[size=18] = QShColor.Purple <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Purple(35),
    // CallChain[size=18] = QShColor.Cyan <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Cyan(36),
    // CallChain[size=18] = QShColor.LightGray <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightGray(37),

    // CallChain[size=18] = QShColor.DefaultFG <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DefaultFG(39),
    // CallChain[size=18] = QShColor.DefaultBG <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DefaultBG(49),

    // CallChain[size=18] = QShColor.DarkGray <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DarkGray(90),
    // CallChain[size=18] = QShColor.LightRed <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightRed(91),
    // CallChain[size=18] = QShColor.LightGreen <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightGreen(92),
    // CallChain[size=18] = QShColor.LightYellow <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightYellow(93),
    // CallChain[size=18] = QShColor.LightBlue <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightBlue(94),
    // CallChain[size=18] = QShColor.LightPurple <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightPurple(95),
    // CallChain[size=18] = QShColor.LightCyan <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LightCyan(96),
    // CallChain[size=18] = QShColor.White <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    White(97);

    // CallChain[size=18] = QShColor.fg <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val fg: String = "$qSTART${code}m"

    // CallChain[size=18] = QShColor.bg <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val bg: String = "$qSTART${code + qBG_JUMP}m"

    companion object {
        // CallChain[size=18] = QShColor.random() <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun random(seed: String, colors: Array<QShColor> = arrayOf(Yellow, Green, Blue, Purple, Cyan)): QShColor {
            val idx = seed.hashCode().rem(colors.size).absoluteValue
            return colors[idx]
        }

        // CallChain[size=18] = QShColor.get() <-[Propag]- QShColor.Yellow <-[Call]- String.yellow <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun get(ansiEscapeCode: Int): QShColor {
            return QShColor.values().find {
                it.code == ansiEscapeCode
            }!!
        }
    }
}

// CallChain[size=16] = String.qColorTarget() <-[Call]- QException.mySrcAndStack <-[Call]- QExceptio ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qColorTarget(ptn: Regex, fg: QShColor? = null, bg: QShColor? = null): String {
    return ptn.replace(this, "$0".qColor(fg, bg))
}

// CallChain[size=5] = String.red <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.red: String
    get() = this?.qColor(QShColor.Red) ?: "null".qColor(QShColor.Red)

// CallChain[size=5] = String.green <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.green: String
    get() = this?.qColor(QShColor.Green) ?: "null".qColor(QShColor.Green)

// CallChain[size=16] = String.yellow <-[Call]- QException.qToString() <-[Call]- QException.toString ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.yellow: String
    get() = this?.qColor(QShColor.Yellow) ?: "null".qColor(QShColor.Yellow)

// CallChain[size=5] = String.blue <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.blue: String
    get() = this?.qColor(QShColor.Blue) ?: "null".qColor(QShColor.Blue)

// CallChain[size=18] = String.cyan <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.cyan: String
    get() = this?.qColor(QShColor.Cyan) ?: "null".qColor(QShColor.Cyan)

// CallChain[size=13] = String.light_gray <-[Call]- QE.throwIt() <-[Call]- qUnreachable() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.light_gray: String
    get() = this?.qColor(QShColor.LightGray) ?: "null".qColor(QShColor.LightGray)

// CallChain[size=5] = String.dark_gray <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.dark_gray: String
    get() = this?.qColor(QShColor.DarkGray) ?: "null".qColor(QShColor.DarkGray)

// CallChain[size=7] = String.light_red <-[Call]- List<QTestResultElement>.allTestedMethods <-[Call] ... It() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val String?.light_red: String
    get() = this?.qColor(QShColor.LightRed) ?: "null".qColor(QShColor.LightRed)

// CallChain[size=19] = String.light_green <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.light_green: String
    get() = this?.qColor(QShColor.LightGreen) ?: "null".qColor(QShColor.LightGreen)

// CallChain[size=4] = String.light_blue <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val String?.light_blue: String
    get() = this?.qColor(QShColor.LightBlue) ?: "null".qColor(QShColor.LightBlue)

// CallChain[size=21] = String.light_cyan <-[Call]- qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String?.light_cyan: String
    get() = this?.qColor(QShColor.LightCyan) ?: "null".qColor(QShColor.LightCyan)

// CallChain[size=17] = String.noStyle <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndStac ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String.noStyle: String
    get() {
        return this.replace("""\Q$qSTART\E\d{1,2}m""".re, "")
    }

// CallChain[size=18] = String.path <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val String.path: Path
    get() = Paths.get(this.trim()).toAbsolutePath().normalize()

// CallChain[size=7] = QLR <-[Ref]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlo ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QLR {
    // CallChain[size=7] = QLR.LEFT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call] ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LEFT,
    // CallChain[size=7] = QLR.RIGHT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    RIGHT
}

// CallChain[size=5] = qSeparator() <-[Call]- QOut.separator() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qSeparator(
    fg: QShColor? = QShColor.LightGray,
    bg: QShColor? = null,
    char: Char = '⎯',
    length: Int = 80,
    start: String = "\n",
    end: String = "\n",
): String {
    return start + char.toString().repeat(length).qColor(fg, bg) + end
}

// CallChain[size=5] = qSeparatorWithLabel() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qSeparatorWithLabel(
    label: String,
    fg: QShColor? = QShColor.LightGray,
    bg: QShColor? = null,
    char: Char = '⎯',
    length: Int = 70,
    start: String = "\n",
    end: String = "\n",
): String {
    return start + label + "  " + char.toString().repeat((length - label.length - 2).coerceAtLeast(0)).qColor(fg, bg)
            .qWithMinAndMaxLength(length, length, alignment = QAlign.LEFT, endDots = "") + end
}

// CallChain[size=6] = String.qWithMinAndMaxLength() <-[Call]- qSeparatorWithLabel() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun String.qWithMinAndMaxLength(
        minLength: Int,
        maxLength: Int,
        alignment: QAlign = QAlign.RIGHT,
        endDots: String = "...",
): String {
    (minLength <= maxLength).qaTrue()

    return if (this.length > maxLength) {
        qWithMaxLength(maxLength, endDots = endDots)
    } else {
        qWithMinLength(minLength, alignment)
    }
}

// CallChain[size=7] = String.qWithMinLength() <-[Call]- String.qWithMinAndMaxLength() <-[Call]- qSe ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun String.qWithMinLength(minLength: Int, alignment: QAlign = QAlign.RIGHT): String = when (alignment) {
    QAlign.LEFT -> String.format("%-${minLength}s", this)
    QAlign.RIGHT -> String.format("%${minLength}s", this)
    QAlign.CENTER -> String.format("%${minLength}s", this).qAlignCenter()
}

// CallChain[size=6] = String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithMaxLength(maxLength: Int, endDots: String = " ..."): String {
    if (maxLength - endDots.length < 0)
        QE.IllegalArgument.throwIt(maxLength - endDots.length)

    if (length < maxLength)
        return this

    if (endDots.isNotEmpty() && length < endDots.length + 1)
        return this

    return substring(0, length.coerceAtMost(maxLength - endDots.length)) + endDots
}

// CallChain[size=16] = QOnlyIfStr <-[Ref]- QException.qToString() <-[Call]- QException.toString() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QOnlyIfStr(val matches: (String) -> Boolean) {
    // CallChain[size=16] = QOnlyIfStr.Multiline <-[Call]- QException.qToString() <-[Call]- QException.t ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Multiline({ it.qIsMultiLine() }),
    // CallChain[size=16] = QOnlyIfStr.SingleLine <-[Call]- QException.qToString() <-[Call]- QException. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SingleLine({ it.qIsSingleLine() }),
    // CallChain[size=17] = QOnlyIfStr.Empty <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Empty({ it.isEmpty() }),
    // CallChain[size=17] = QOnlyIfStr.Blank <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Blank({ it.isBlank() }),
    // CallChain[size=17] = QOnlyIfStr.NotEmpty <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    NotEmpty({ it.isNotEmpty() }),
    // CallChain[size=17] = QOnlyIfStr.NotBlank <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    NotBlank({ it.isNotBlank() }),
    // CallChain[size=17] = QOnlyIfStr.Always <-[Propag]- QOnlyIfStr.Multiline <-[Call]- QException.qToS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Always({ true })
}

// CallChain[size=16] = String.qWithNewLinePrefix() <-[Call]- QException.qToString() <-[Call]- QExce ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithNewLinePrefix(
        numNewLine: Int = 1,
        onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline,
        lineSeparator: QLineSeparator = QLineSeparator.LF,
): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeWhile { it == '\n' || it == '\r' }.count()

    return lineSeparator.value.repeat(numNewLine) + substring(nCount)
}

// CallChain[size=21] = String.qWithNewLineSuffix() <-[Call]- String.qWithNewLineSurround() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithNewLineSuffix(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeLastWhile { it == '\n' || it == '\r' }.count()

    return substring(0, length - nCount) + "\n".repeat(numNewLine)
}

// CallChain[size=20] = String.qWithNewLineSurround() <-[Call]- String.qBracketEnd() <-[Call]- qBrac ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithNewLineSurround(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    return qWithNewLinePrefix(numNewLine, QOnlyIfStr.Always).qWithNewLineSuffix(numNewLine, QOnlyIfStr.Always)
}

// CallChain[size=16] = String.qWithSpacePrefix() <-[Call]- QException.qToString() <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithSpacePrefix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return " ".repeat(numSpace) + this.trimStart()
}

// CallChain[size=20] = String.qWithSpaceSuffix() <-[Call]- String.qBracketStartOrMiddle() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qWithSpaceSuffix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return this.trimEnd() + " ".repeat(numSpace)
}

// CallChain[size=10] = CharSequence.qEndsWith() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun CharSequence.qEndsWith(suffix: Regex, length: Int = 100): Boolean {
    return takeLast(min(length, this.length)).matches(suffix)
}

// CallChain[size=17] = String.qIsMultiLine() <-[Call]- QOnlyIfStr.Multiline <-[Call]- QException.qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qIsMultiLine(): Boolean {
    return this.contains("\n") || this.contains("\r")
}

// CallChain[size=17] = String.qIsSingleLine() <-[Call]- QOnlyIfStr.SingleLine <-[Call]- QException. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qIsSingleLine(): Boolean {
    return !this.qIsMultiLine()
}

// CallChain[size=17] = QLineSeparator <-[Ref]- String.qWithNewLinePrefix() <-[Call]- QException.qTo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QLineSeparator(val value: String) {
    // CallChain[size=17] = QLineSeparator.LF <-[Call]- String.qWithNewLinePrefix() <-[Call]- QException ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LF("\n"),
    // CallChain[size=18] = QLineSeparator.CRLF <-[Propag]- QLineSeparator.QLineSeparator() <-[Call]- St ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CRLF("\r\n"),
    // CallChain[size=18] = QLineSeparator.CR <-[Propag]- QLineSeparator.QLineSeparator() <-[Call]- Stri ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CR("\r");

    companion object {
        // CallChain[size=21] = QLineSeparator.DEFAULT <-[Call]- Path.qLineSeparator() <-[Call]- Path.qFetch ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val DEFAULT = QLineSeparator.LF
    }
}

// CallChain[size=9] = String.qSubstring() <-[Call]- String.qMoveLeft() <-[Call]- QLineMatchResult.a ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qSubstring(rangeBothInclusive: IntRange): String =
        substring(rangeBothInclusive.first, rangeBothInclusive.last + 1)

// CallChain[size=10] = String.qCountLeftSpace() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qCountLeftSpace(): Int = takeWhile { it == ' ' }.count()

// CallChain[size=9] = String.qCountRightSpace() <-[Call]- String.qMoveCenter() <-[Call]- QLineMatch ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qCountRightSpace(): Int = takeLastWhile { it == ' ' }.count()

// CallChain[size=9] = qMASK_LENGTH_LIMIT <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qMASK_LENGTH_LIMIT: Int = 100_000

// CallChain[size=11] = QToString <-[Ref]- qToStringRegistry <-[Call]- Any.qToString() <-[Call]- Any ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QToString(val okToApply: (Any) -> Boolean, val toString: (Any) -> String)

// CallChain[size=10] = qToStringRegistry <-[Call]- Any.qToString() <-[Call]- Any.qToLogString() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=9] = Any.qToString() <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call]- QOn ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = Any.qToLogString() <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=9] = String.qClarifyEmptyOrBlank() <-[Call]- Any.qToLogString() <-[Call]- T.qLog() ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qClarifyEmptyOrBlank(): String {
    return if (this.isEmpty()) {
        "(EMPTY STRING)".qColor(QShColor.LightGray)
    } else if (this.isBlank()) {
        "$this(BLANK STRING)".qColor(QShColor.LightGray)
    } else {
        this
    }
}

// CallChain[size=5] = QAlign <-[Ref]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QAlign {
    // CallChain[size=7] = QAlign.LEFT <-[Propag]- QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Cal ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LEFT,
    // CallChain[size=6] = QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    RIGHT,
    // CallChain[size=5] = QAlign.CENTER <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CENTER
}

// CallChain[size=7] = QLineMatchResult <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll()  ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QLineMatchResult(
    val regex: Regex,
    val text: String,
    val onlyFirstMatch: Boolean = false,
    val groupIdx: QGroupIdx
) {
    // CallChain[size=8] = QLineMatchResult.curText <-[Call]- QLineMatchResult.align() <-[Call]- String. ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var curText: String = text

    // CallChain[size=8] = QLineMatchResult.updateResult() <-[Call]- QLineMatchResult.align() <-[Call]-  ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun updateResult(align: QAlign) {
        updateRowResult()
        updateColResult()
        updateColDestPos(align)
    }

    // CallChain[size=9] = QLineMatchResult.rowResults <-[Call]- QLineMatchResult.matchedRange() <-[Call ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    // line index -> match index
    lateinit var rowResults: List<List<MatchResult>>

    // CallChain[size=10] = QLineMatchResult.colResults <-[Call]- QLineMatchResult.updateColDestPos() <- ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    // col index -> match index
    lateinit var colResults: List<List<MatchResult>>

    // CallChain[size=8] = QLineMatchResult.colDestPos <-[Call]- QLineMatchResult.align() <-[Call]- Stri ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    // col index -> max left index / right index of matched region
    lateinit var colDestPos: List<QLeftRight>

    // CallChain[size=9] = QLineMatchResult.updateRowResult() <-[Call]- QLineMatchResult.updateResult()  ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=9] = QLineMatchResult.updateColResult() <-[Call]- QLineMatchResult.updateResult()  ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=9] = QLineMatchResult.updateColDestPos() <-[Call]- QLineMatchResult.updateResult() ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun updateColDestPos(align: QAlign) {
        colDestPos = if (align == QAlign.RIGHT) {
            colResults.map { it.qMinOrMaxIndexLR(groupIdx, QMinOrMax.MAX) }
        } else {
            colResults.map { it.qMinOrMaxIndexLR(groupIdx, QMinOrMax.MIN) }
        }
    }

    // CallChain[size=8] = QLineMatchResult.matchedRange() <-[Call]- QLineMatchResult.align() <-[Call]-  ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun matchedRange(rowIdx: Int, colIdx: Int): IntRange? {
        val groups = rowResults.getOrNull(rowIdx)?.getOrNull(colIdx)?.groups ?: return null
        return if (groupIdx.idx < groups.size) {
            groups[groupIdx.idx]?.range
        } else {
            null
        }
    }

    // CallChain[size=10] = QLineMatchResult.QMinOrMax <-[Ref]- QLineMatchResult.updateColDestPos() <-[C ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    enum class QMinOrMax {
        MIN, MAX
    }

    // CallChain[size=10] = QLineMatchResult.List<MatchResult>.qMinOrMaxIndexLR() <-[Call]- QLineMatchRe ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=6] = QLineMatchResult.makeEqualWidth() <-[Call]- String.qMakeEqualWidth() <-[Call] ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=7] = QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRig ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = IntRange.qSize <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() < ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val IntRange.qSize: Int
    get() = abs(last - first) + 1

// CallChain[size=5] = String.qMakeEqualWidth() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qMakeEqualWidth(
    place: Regex,
    align: QAlign = QAlign.CENTER,
    groupIdx: QGroupIdx = QGroupIdx.FIRST
): String {
    return QLineMatchResult(place, this, false, groupIdx).makeEqualWidth(align)
}

// CallChain[size=5] = String.qAlignRightAll() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qAlignRightAll(
    vararg places: Regex = arrayOf("(.*)".re),
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = false, keepLength = keepLength, groupIdx = groupIdx)
}

// CallChain[size=4] = String.qAlignRight() <-[Call]- QBenchmark.start() <-[Call]- qBenchmark() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qAlignRight(
    vararg places: Regex = arrayOf("(.*)".re),
    onlyFirstMatch: Boolean = true,
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = onlyFirstMatch, keepLength = keepLength, groupIdx = groupIdx)
}

// CallChain[size=7] = String.qIsNumber() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll( ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qIsNumber(): Boolean {
    return this.trim().matches("""[\d./eE+-]+""".re)
}

// CallChain[size=8] = String.qAlignCenter() <-[Call]- String.qWithMinLength() <-[Call]- String.qWit ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun String.qAlignCenter(
    vararg places: Regex = arrayOf("(.*)".re),
    onlyFirstMatch: Boolean = true,
    oddLengthTuning: QLR = if (qIsNumber()) QLR.RIGHT else QLR.LEFT,
    groupIdx: QGroupIdx = QGroupIdx.FIRST
): String {
    return qAlign(QAlign.CENTER, *places, onlyFirstMatch = onlyFirstMatch, oddLengthTuning = oddLengthTuning, groupIdx = groupIdx)
}

// CallChain[size=6] = String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=9] = QLeftRight <-[Ref]- QLineMatchResult.colDestPos <-[Call]- QLineMatchResult.al ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private data class QLeftRight(val left: Int, val right: Int)

// CallChain[size=6] = QGroupIdx <-[Ref]- String.qAlignRightAll() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private enum class QGroupIdx(val idx: Int) {
    // CallChain[size=6] = QGroupIdx.ENTIRE_MATCH <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ENTIRE_MATCH(0),
    // CallChain[size=10] = QGroupIdx.FIRST <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FIRST(1),
    // CallChain[size=10] = QGroupIdx.SECOND <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResul ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SECOND(2),
    // CallChain[size=10] = QGroupIdx.THIRD <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    THIRD(3),
    // CallChain[size=10] = QGroupIdx.FOURTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResul ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FOURTH(4),
    // CallChain[size=10] = QGroupIdx.FIFTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FIFTH(5),
    // CallChain[size=10] = QGroupIdx.SIXTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SIXTH(6),
    // CallChain[size=10] = QGroupIdx.SEVENTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResu ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    SEVENTH(7),
    // CallChain[size=10] = QGroupIdx.EIGHTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResul ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    EIGHTH(8),
    // CallChain[size=10] = QGroupIdx.NINTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    NINTH(9),
    // CallChain[size=10] = QGroupIdx.TENTH <-[Propag]- QGroupIdx.QGroupIdx() <-[Call]- QLineMatchResult ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    TENTH(10);
}

// CallChain[size=8] = String.qMoveCenter() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAli ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = qSpaces() <-[Call]- String.qRangeWithWidth() <-[Call]- QLineMatchResult.makeE ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qSpaces(n: Int): String = " ".repeat(n)

// CallChain[size=7] = String.qRangeWithWidth() <-[Call]- QLineMatchResult.makeEqualWidth() <-[Call] ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = String.qMoveLeft() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=8] = String.qMoveRight() <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlig ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=10] = String.qCountOccurrence() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun String.qCountOccurrence(word: String): Int {
    return windowed(word.length) {
        if (it == word)
            1
        else
            0
    }.sum()
}

// CallChain[size=12] = QMask <-[Ref]- QMaskBetween <-[Call]- QMask.DOUBLE_QUOTE <-[Call]- QMask.KOT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private interface QMask {
    // CallChain[size=10] = QMask.apply() <-[Propag]- QMask.KOTLIN_STRING <-[Call]- Any.qToLogString() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun apply(text: String): QMaskResult

    companion object {
        // CallChain[size=10] = QMask.THREE_DOUBLE_QUOTES <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any.qToLog ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val THREE_DOUBLE_QUOTES by lazy {
            QMaskBetween(
                "\"\"\"", "\"\"\"",
                nestStartSequence = null,
                escapeChar = '\\',
                maskIncludeStartAndEndSequence = false,
            )
        }
        // CallChain[size=10] = QMask.DOUBLE_QUOTE <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any.qToLogString( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val DOUBLE_QUOTE by lazy {
            QMaskBetween(
                "\"", "\"",
                nestStartSequence = null,
                escapeChar = '\\',
                maskIncludeStartAndEndSequence = false,
            )
        }
        // CallChain[size=9] = QMask.KOTLIN_STRING <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val KOTLIN_STRING by lazy {
            QMultiMask(
                THREE_DOUBLE_QUOTES,
                DOUBLE_QUOTE
            )
        }
        // CallChain[size=9] = QMask.PARENS <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call]- QOnePa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val PARENS by lazy {
            QMaskBetween(
                "(", ")",
                nestStartSequence = "(", escapeChar = '\\'
            )
        }
        // CallChain[size=9] = QMask.INNER_BRACKETS <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=10] = QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[Call]- Any.qToLogString() <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMultiMask(vararg mask: QMaskBetween) : QMask {
    // CallChain[size=12] = QMultiMask.masks <-[Call]- QMultiMask.apply() <-[Propag]- QMultiMask <-[Call ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val masks: Array<QMaskBetween>

    // CallChain[size=11] = QMultiMask.init { <-[Propag]- QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    init {
        masks = arrayOf(*mask)
    }

    // CallChain[size=11] = QMultiMask.apply() <-[Propag]- QMultiMask <-[Call]- QMask.KOTLIN_STRING <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun apply(text: String): QMaskResult {
        var result: QMaskResult? = null
        for (mask in masks) {
            result = result?.applyMoreMask(mask) ?: mask.apply(text)
        }

        return result!!
    }
}

// CallChain[size=11] = QMaskBetween <-[Call]- QMask.DOUBLE_QUOTE <-[Call]- QMask.KOTLIN_STRING <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=12] = QMaskBetween.apply() <-[Propag]- QMaskBetween.QMaskBetween() <-[Ref]- QMask. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun apply(text: String): QMaskResult {
        return applyMore(text, null)
    }

    // CallChain[size=13] = QMaskBetween.applyMore() <-[Call]- QMaskBetween.apply() <-[Propag]- QMaskBet ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=16] = QMutRegion <-[Ref]- QRegion.toMutRegion() <-[Propag]- QRegion.contains() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private open class QMutRegion(override var start: Int, override var end: Int) : QRegion(start, end) {
    // CallChain[size=17] = QMutRegion.intersectMut() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegio ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun intersectMut(region: QRegion) {
        val start = max(this.start, region.start)
        val end = min(this.end, region.end)

        if (start <= end) {
            this.start = start
            this.end = end
        }
    }

    // CallChain[size=17] = QMutRegion.addOffset() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegion() ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun addOffset(offset: Int) {
        start += offset
        end += offset
    }

    // CallChain[size=17] = QMutRegion.shift() <-[Propag]- QMutRegion <-[Ref]- QRegion.toMutRegion() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun shift(length: Int) {
        this.start += length
        this.end += length
    }
}

// CallChain[size=16] = QRegion <-[Ref]- QRegion.intersect() <-[Propag]- QRegion.contains() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * [start] inclusive, [end] exclusive
 */
private open class QRegion(open val start: Int, open val end: Int) {
    // CallChain[size=15] = QRegion.toMutRegion() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun toMutRegion(): QMutRegion {
        return QMutRegion(start, end)
    }

    // CallChain[size=15] = QRegion.toRange() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.appl ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun toRange(): IntRange {
        return IntRange(start, end + 1)
    }

    // CallChain[size=15] = QRegion.length <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val length: Int
        get() = end - start

    // CallChain[size=15] = QRegion.intersect() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.ap ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun intersect(region: QRegion): QRegion? {
        val start = max(this.start, region.start)
        val end = min(this.end, region.end)

        return if (start <= end) {
            QRegion(end, start)
        } else {
            null
        }
    }

    // CallChain[size=14] = QRegion.contains() <-[Call]- QMaskBetween.applyMore() <-[Call]- QMaskBetween ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun contains(idx: Int): Boolean {
        return idx in start until end
    }

    // CallChain[size=15] = QRegion.cut() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMor ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun cut(text: String): String {
        return text.substring(start, end)
    }

    // CallChain[size=15] = QRegion.remove() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.apply ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun remove(text: String): String {
        return text.removeRange(start, end)
    }

    // CallChain[size=15] = QRegion.replace() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.appl ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun replace(text: String, replacement: String): String {
        return text.replaceRange(start, end, replacement)
    }

    // CallChain[size=15] = QRegion.mask() <-[Propag]- QRegion.contains() <-[Call]- QMaskBetween.applyMo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun mask(text: String, maskChar: Char = '*'): String {
        return text.replaceRange(this.toRange(), maskChar.toString().repeat(end - start))
    }
}

// CallChain[size=11] = QReplacer <-[Ref]- String.qMaskAndReplace() <-[Call]- QMaskResult.replaceAnd ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QReplacer(start: Int, end: Int, val replacement: String) : QMutRegion(start, end)

// CallChain[size=11] = QMaskResult <-[Ref]- QMask.apply() <-[Propag]- QMask.KOTLIN_STRING <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMaskResult(val maskedStr: String, val orgText: String, val maskChar: Char) {
    // CallChain[size=9] = QMaskResult.replaceAndUnmask() <-[Call]- Any.qToLogString() <-[Call]- T.qLog( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * Apply regex to masked string.
     * Apply replacement to original text.
     */
    fun replaceAndUnmask(ptn: Regex, replacement: String, findAll: Boolean = true): String {
        return orgText.qMaskAndReplace(maskedStr, ptn, replacement, findAll)
    }

    // CallChain[size=12] = QMaskResult.applyMoreMask() <-[Call]- QMultiMask.apply() <-[Propag]- QMultiM ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun applyMoreMask(mask: QMaskBetween): QMaskResult {
        return mask.applyMore(maskedStr, orgText)
    }

    // CallChain[size=12] = QMaskResult.toString() <-[Propag]- QMaskResult <-[Ref]- QMask.apply() <-[Pro ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        val original = orgText.qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)
        val masked = maskedStr.replace(maskChar, '*').qWithNewLineSurround(onlyIf = QOnlyIfStr.Multiline)

        return "${QMaskResult::class.simpleName} : $original ${"->".cyan} $masked"
    }
}

// CallChain[size=9] = CharSequence.qMask() <-[Call]- Any.qToLogString() <-[Call]- T.qLog() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=14] = String.qFindBetween() <-[Call]- QMaskBetween.applyMore() <-[Call]- QMaskBetw ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=10] = String.qMaskAndReplace() <-[Call]- QMaskResult.replaceAndUnmask() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=11] = CharSequence.qMultiReplace() <-[Call]- String.qMaskAndReplace() <-[Call]- QM ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=11] = MatchResult.qResolveReplacementGroup() <-[Call]- String.qMaskAndReplace() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=12] = CharSequence.qReplace() <-[Call]- MatchResult.qResolveReplacementGroup() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun CharSequence.qReplace(oldValue: String, newValue: String, escapeChar: Char): String {
    return replace(Regex("""(?<!\Q$escapeChar\E)\Q$oldValue\E"""), newValue)
}

// CallChain[size=16] = QSequenceReader <-[Call]- QBetween.find() <-[Call]- String.qFindBetween() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QSequenceReader(text: CharSequence) : QCharReader(text) {
    // CallChain[size=18] = QSequenceReader.sequenceOffset <-[Call]- QSequenceReader.offsetInSequence()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private var sequenceOffset = 0

    // CallChain[size=18] = QSequenceReader.sequence <-[Call]- QSequenceReader.peekCurrentCharInSequence ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private var sequence: CharArray? = null

    // CallChain[size=17] = QSequenceReader.startReadingSequence() <-[Call]- QSequenceReader.detectSeque ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun startReadingSequence(sequence: CharArray): Boolean {
        return if (!hasNextChar(sequence.size)) {
            false
        } else {
            this.sequence = sequence
            sequenceOffset = offset
            true
        }
    }

    // CallChain[size=17] = QSequenceReader.endReadingSequence() <-[Call]- QSequenceReader.detectSequenc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun endReadingSequence(success: Boolean): Boolean {

        if (!success) {
            offset = sequenceOffset
        }

        sequenceOffset = -1

        return success
    }

    // CallChain[size=17] = QSequenceReader.hasNextCharInSequence() <-[Call]- QSequenceReader.detectSequ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun hasNextCharInSequence(): Boolean {
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

    // CallChain[size=17] = QSequenceReader.peekCurrentCharInSequence() <-[Call]- QSequenceReader.detect ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    private fun peekCurrentCharInSequence(): Char {
        return sequence!![offsetInSequence()]
    }

    // CallChain[size=17] = QSequenceReader.offsetInSequence() <-[Call]- QSequenceReader.detectSequence( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * 0 to sequence.size - 1
     */
    private fun offsetInSequence(): Int {
        return offset - sequenceOffset
    }

    // CallChain[size=16] = QSequenceReader.detectSequence() <-[Call]- QBetween.find() <-[Call]- String. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=17] = QCharReader <-[Call]- QSequenceReader <-[Call]- QBetween.find() <-[Call]- St ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private open class QCharReader(val text: CharSequence) {
    // CallChain[size=18] = QCharReader.offset <-[Propag]- QCharReader <-[Call]- QSequenceReader <-[Call ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var offset = 0

    // CallChain[size=18] = QCharReader.lineNumber() <-[Propag]- QCharReader <-[Call]- QSequenceReader < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun lineNumber(): Int {
        // Consider caret to be between the character on the offset and the character preceding it
        //
        // ex. ( [ ] indicate offsets )
        // [\n]abc\n --> lineNumber is 1 "First Line"
        // \n[\n] --> lineNumber is 2 "Second Line"

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

    // CallChain[size=18] = QCharReader.countIndentSpaces() <-[Propag]- QCharReader <-[Call]- QSequenceR ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=18] = QCharReader.hasNextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun hasNextChar(len: Int = 1): Boolean {
        return offset + len - 1 < text.length
    }

    // CallChain[size=18] = QCharReader.isOffsetEOF() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun isOffsetEOF(): Boolean {
        return offset == text.length - 1
    }

    // CallChain[size=18] = QCharReader.isValidOffset() <-[Propag]- QCharReader <-[Call]- QSequenceReade ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun isValidOffset(): Boolean {
        return 0 <= offset && offset < text.length
    }

    // CallChain[size=18] = QCharReader.hasPreviousChar() <-[Propag]- QCharReader <-[Call]- QSequenceRea ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun hasPreviousChar(len: Int = 1): Boolean {
        return 0 < offset - len + 1
    }

    // CallChain[size=18] = QCharReader.previousChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun previousChar(len: Int = 1): Char {
        offset -= len
        return text[offset]
    }

    // CallChain[size=18] = QCharReader.currentChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun currentChar(): Char {
        return text[offset]
    }

    // CallChain[size=18] = QCharReader.peekNextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun peekNextChar(): Char {
        return text[offset]
    }

    // CallChain[size=18] = QCharReader.moveOffset() <-[Propag]- QCharReader <-[Call]- QSequenceReader < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    inline fun moveOffset(plus: Int = 1) {
        offset += plus
    }

    // CallChain[size=18] = QCharReader.nextChar() <-[Propag]- QCharReader <-[Call]- QSequenceReader <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /**
     * Read current offset char and add offset by 1.
     */
    inline fun nextChar(): Char {
        return text[offset++]
    }

    // CallChain[size=18] = QCharReader.nextStringExcludingCurOffset() <-[Propag]- QCharReader <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun nextStringExcludingCurOffset(length: Int): String {
        val str = text.substring(offset + 1, (offset + 1 + length).coerceAtMost(text.length))
        offset += length
        return str
    }

    // CallChain[size=18] = QCharReader.peekNextStringIncludingCurOffset() <-[Propag]- QCharReader <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun peekNextStringIncludingCurOffset(length: Int): String {
        return text.substring(offset, (offset + length).coerceAtMost(text.length))
    }

    // CallChain[size=18] = QCharReader.peekPreviousStringExcludingCurOffset() <-[Propag]- QCharReader < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun peekPreviousStringExcludingCurOffset(length: Int): String {
        return text.substring(offset - length, offset)
    }
}

// CallChain[size=15] = QBetween <-[Call]- String.qFindBetween() <-[Call]- QMaskBetween.applyMore()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

    // CallChain[size=15] = QBetween.find() <-[Call]- String.qFindBetween() <-[Call]- QMaskBetween.apply ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=4] = qNow <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qNow: Long
    get() = System.currentTimeMillis()

// CallChain[size=6] = QTimeItResult <-[Ref]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QTimeItResult<T>(val label: String, val time: Long, val result: T) {
    // CallChain[size=6] = QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun toString(): String {
        return qBrackets(label, time.qFormatDuration())
    }
}

// CallChain[size=5] = QTimeAndResult <-[Call]- QBlock.timeItWarmup() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QTimeAndResult(val index: Int, val time: Long, val nTry: Int = 1, val result: Any? = null) {
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
private inline fun <T> qTimeIt(label: String = qThisSrcLineSignature, quiet: Boolean = false, out: QOut? = QOut.CONSOLE, block: () -> T): QTimeItResult<T> {
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


// ================================================================================
// endregion Src from Non-Root API Files -- Auto Generated by nyab.conf.QCompactLib.kt