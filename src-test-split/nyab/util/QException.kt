/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("NOTHING_TO_INLINE")

package nyab.util

import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import nyab.conf.QE
import nyab.conf.QMyMark
import nyab.match.QM

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=11] = qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun qUnreachable(msg: Any? = ""): Nothing {
    QE.Unreachable.throwIt(msg)
}

// CallChain[size=12] = QE.throwIt() <-[Call]- qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun QE.throwIt(msg: Any? = "", e: Throwable? = null, stackDepth: Int = 0): Nothing {
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

// CallChain[size=13] = QException <-[Call]- QE.throwIt() <-[Call]- qUnreachable() <-[Call]- QFetchR ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QException(
    val type: QE = QE.Other,
    msg: String = QMyMark.WARN,
    e: Throwable? = null,
    val stackDepth: Int = 0,
    stackSize: Int = 20,
    stackFilter: (StackWalker.StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
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
            .qColorTarget(qRe("""\sshould[a-zA-Z]+"""), QShColor.LIGHT_YELLOW)
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

// CallChain[size=19] = QE.throwItBrackets() <-[Call]- qBrackets() <-[Call]- qMySrcLinesAtFrame() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun QE.throwItBrackets(vararg keysAndValues: Any?, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets(*keysAndValues), e, stackDepth = stackDepth + 1)
}

// CallChain[size=21] = QE.throwItFile() <-[Call]- LineNumberReader.qFetchLinesAround() <-[Call]- Pa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun QE.throwItFile(path: Path, e: Throwable? = null, stackDepth: Int = 0): Nothing {
    throw QException(this, qBrackets("File", path.absolutePathString()), e, stackDepth = stackDepth + 1)
}

// CallChain[size=20] = T?.qaNotNull() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun <T : Any> T?.qaNotNull(exceptionType: QE = QE.ShouldNotBeNull, msg: Any? = ""): T {
    if (this != null) {
        return this
    } else {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    }
}

// CallChain[size=10] = Int?.qaNotZero() <-[Call]- CharSequence.qMask() <-[Call]- Any?.qToLogString( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Int?.qaNotZero(exceptionType: QE = QE.ShouldNotBeZero, msg: Any? = ""): Int {
    if (this == null) {
        QE.ShouldNotBeNull.throwIt(stackDepth = 1, msg = msg)
    } else if (this == 0) {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    } else {
        return this
    }
}

// CallChain[size=7] = Boolean.qaTrue() <-[Call]- String.qWithMinAndMaxLength() <-[Call]- qSeparator ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal fun Boolean.qaTrue(exceptionType: QE = QE.ShouldBeTrue, msg: Any? = "") {
    if (!this) {
        exceptionType.throwIt(stackDepth = 1, msg = msg)
    }
}