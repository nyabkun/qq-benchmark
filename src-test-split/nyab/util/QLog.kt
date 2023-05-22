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

import java.lang.StackWalker.StackFrame
import java.nio.charset.Charset
import java.nio.file.Path
import nyab.conf.QE
import nyab.conf.QMyLog
import nyab.conf.QMyMark
import nyab.conf.QMyPath

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=20] = qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyl ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val qARROW = "===>".light_cyan

// CallChain[size=9] = QSrcCut <-[Call]- QSrcCut.UNTIL_qLog <-[Call]- T.qLog() <-[Call]- QOnePassSta ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QSrcCut(
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
internal class QLogStyle(
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

            return """${"SRC START ―――――――――――".qColor(QShColor.CYAN)}
${this.trim()}
${"SRC END   ―――――――――――".qColor(QShColor.CYAN)}"""
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
internal fun <T : Any?> T.qLog(stackDepth: Int = 0, quiet: Boolean = false): T {
    qLog(qToLogString(), stackDepth = stackDepth + 1, srcCut = QSrcCut.UNTIL_qLog, quiet = quiet)
    return this
}

// CallChain[size=17] = qMySrcLinesAtFrame() <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun qMySrcLinesAtFrame(
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

// CallChain[size=16] = qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Call]- QException.pr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun qLogStackFrames(
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

// CallChain[size=8] = qLog() <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]- QOnePassStat. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun qLog(
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
internal fun qSrcFileLinesAtFrame(
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
internal fun qArrow(key: Any?, value: Any?): String {
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
internal fun qBrackets(vararg keysAndValues: Any?): String {
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