/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package nyab.util

import com.sun.nio.file.ExtendedOpenOption
import java.io.LineNumberReader
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.name
import kotlin.io.path.reader
import kotlin.math.max
import kotlin.streams.asSequence
import nyab.conf.QE
import nyab.conf.QMyPath
import nyab.match.QM
import nyab.match.qMatches

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=11] = QFetchRule <-[Ref]- QFetchRuleA <-[Call]- QFetchRule.SMART_FETCH <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal interface QFetchRule {
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

// CallChain[size=10] = QFetchRuleA <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal abstract class QFetchRuleA(
        override val numLinesBeforeTargetLine: Int = 10,
        override val numLinesAfterTargetLine: Int = 10,
) : QFetchRule

// CallChain[size=10] = QFetchStart <-[Ref]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QFetchStart {
    // CallChain[size=10] = QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE <-[Call]- QFetchRule.SMA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE,
    // CallChain[size=10] = QFetchStart.START_FROM_THIS_LINE <-[Call]- QFetchRule.SMART_FETCH <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_THIS_LINE,
    // CallChain[size=11] = QFetchStart.START_FROM_NEXT_LINE <-[Propag]- QFetchStart.START_FROM_THIS_LIN ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_NEXT_LINE,
    // CallChain[size=11] = QFetchStart.START_FROM_PREVIOUS_LINE <-[Propag]- QFetchStart.START_FROM_THIS ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    START_FROM_PREVIOUS_LINE
}

// CallChain[size=10] = QFetchEnd <-[Ref]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut.UNTIL_qLog <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QFetchEnd {
    // CallChain[size=10] = QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE <-[Call]- QFetchRule.SMART_FET ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE,
    // CallChain[size=10] = QFetchEnd.END_WITH_THIS_LINE <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_THIS_LINE,
    // CallChain[size=11] = QFetchEnd.END_WITH_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_NEXT_LINE,
    // CallChain[size=11] = QFetchEnd.END_WITH_PREVIOUS_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    END_WITH_PREVIOUS_LINE
}

// CallChain[size=19] = Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qFetchLinesAround(
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

// CallChain[size=20] = Path.qLineSeparator() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qLineSeparator(charset: Charset = Charsets.UTF_8): QLineSeparator {
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

// CallChain[size=20] = Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qReader(
        charset: Charset = Charsets.UTF_8,
        buffSize: Int = qBUFFER_SIZE,
        opts: QFlag<QOpenOpt> = QFlag.none(),
): LineNumberReader {
    return LineNumberReader(reader(charset, *opts.toOptEnums()), buffSize)
}

// CallChain[size=20] = Path.qLineAt() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qLineAt(
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

// CallChain[size=22] = TargetSurroundingLines <-[Ref]- LineNumberReader.qFetchTargetSurroundingLine ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class TargetSurroundingLines(
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

// CallChain[size=21] = qBUFFER_SIZE <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal const val qBUFFER_SIZE = DEFAULT_BUFFER_SIZE

// CallChain[size=21] = QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
// @formatter:off
internal enum class QOpenOpt(val opt: OpenOption) : QFlagEnum<QOpenOpt> {
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
internal fun QFlag<QOpenOpt>.toOptEnums(): Array<OpenOption> {
    return toEnumValues().map { it.opt }.toTypedArray()
}

// CallChain[size=20] = Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLines ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Collection<Path>.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    for (path in this) {
        val found = path.qFind(nameMatcher, type, maxDepth)
        if (found != null) return found
    }

    return null
}

// CallChain[size=21] = QFType <-[Ref]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QFType {
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

// CallChain[size=21] = Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    return try {
        qList(type, maxDepth = maxDepth) {
            it.name.qMatches(nameMatcher)
        }.firstOrNull()
    } catch (e: NoSuchElementException) {
        null
    }
}

// CallChain[size=22] = Path.qList() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qList(
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
internal fun Path.qSeq(
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

// CallChain[size=18] = Path.qListByMatch() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Path.qListByMatch(
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