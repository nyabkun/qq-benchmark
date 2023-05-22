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

import java.nio.charset.Charset
import kotlin.math.min
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.isSuperclassOf
import nyab.conf.QE
import nyab.conf.QMyLog
import nyab.conf.QMyToString
import nyab.match.QMFunc
import nyab.match.and

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = QLR <-[Ref]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlo ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QLR {
    // CallChain[size=7] = QLR.LEFT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call] ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LEFT,
    // CallChain[size=7] = QLR.RIGHT <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    RIGHT
}

// CallChain[size=5] = qSeparator() <-[Call]- QOut.separator() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal fun qSeparator(
        fg: QShColor? = QShColor.LIGHT_GRAY,
        bg: QShColor? = null,
        char: Char = '⎯',
        length: Int = 80,
        start: String = "\n",
        end: String = "\n",
): String {
    return start + char.toString().repeat(length).qColor(fg, bg) + end
}

// CallChain[size=5] = qSeparatorWithLabel() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal fun qSeparatorWithLabel(
        label: String,
        fg: QShColor? = QShColor.LIGHT_GRAY,
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
internal fun String.qWithMinAndMaxLength(
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
internal fun String.qWithMinLength(minLength: Int, alignment: QAlign = QAlign.RIGHT): String = when (alignment) {
    QAlign.LEFT -> String.format("%-${minLength}s", this)
    QAlign.RIGHT -> String.format("%${minLength}s", this)
    QAlign.CENTER -> String.format("%${minLength}s", this).qAlignCenter()
}

// CallChain[size=6] = String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qWithMaxLength(maxLength: Int, endDots: String = " ..."): String {
    if (maxLength - endDots.length < 0)
        QE.IllegalArgument.throwIt(maxLength - endDots.length)

    if (length < maxLength)
        return this

    if (endDots.isNotEmpty() && length < endDots.length + 1)
        return this

    return substring(0, length.coerceAtMost(maxLength - endDots.length)) + endDots
}

// CallChain[size=16] = QOnlyIfStr <-[Ref]- QException.qToString() <-[Call]- QException.toString() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QOnlyIfStr(val matches: (String) -> Boolean) {
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
internal fun String.qWithNewLinePrefix(
        numNewLine: Int = 1,
        onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline,
        lineSeparator: QLineSeparator = QLineSeparator.LF,
): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeWhile { it == '\n' || it == '\r' }.count()

    return lineSeparator.value.repeat(numNewLine) + substring(nCount)
}

// CallChain[size=26] = String.qWithNewLineSuffix() <-[Call]- String.qWithNewLineSurround() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qWithNewLineSuffix(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    val nCount = takeLastWhile { it == '\n' || it == '\r' }.count()

    return substring(0, length - nCount) + "\n".repeat(numNewLine)
}

// CallChain[size=25] = String.qWithNewLineSurround() <-[Call]- QMaskResult.toString() <-[Propag]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qWithNewLineSurround(numNewLine: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.Multiline): String {
    if (!onlyIf.matches(this)) return this

    return qWithNewLinePrefix(numNewLine, QOnlyIfStr.Always).qWithNewLineSuffix(numNewLine, QOnlyIfStr.Always)
}

// CallChain[size=16] = String.qWithSpacePrefix() <-[Call]- QException.qToString() <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qWithSpacePrefix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return " ".repeat(numSpace) + this.trimStart()
}

// CallChain[size=20] = String.qWithSpaceSuffix() <-[Call]- String.qBracketStartOrMiddle() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qWithSpaceSuffix(numSpace: Int = 1, onlyIf: QOnlyIfStr = QOnlyIfStr.SingleLine): String {
    if (!onlyIf.matches(this)) return this

    return this.trimEnd() + " ".repeat(numSpace)
}

// CallChain[size=10] = CharSequence.qEndsWith() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun CharSequence.qEndsWith(suffix: Regex, length: Int = 100): Boolean {
    return takeLast(min(length, this.length)).matches(suffix)
}

// CallChain[size=17] = String.qIsMultiLine() <-[Call]- QOnlyIfStr.Multiline <-[Call]- QException.qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qIsMultiLine(): Boolean {
    return this.contains("\n") || this.contains("\r")
}

// CallChain[size=17] = String.qIsSingleLine() <-[Call]- QOnlyIfStr.SingleLine <-[Call]- QException. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qIsSingleLine(): Boolean {
    return !this.qIsMultiLine()
}

// CallChain[size=17] = QLineSeparator <-[Ref]- String.qWithNewLinePrefix() <-[Call]- QException.qTo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QLineSeparator(val value: String) {
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
internal fun String.qSubstring(rangeBothInclusive: IntRange): String =
        substring(rangeBothInclusive.first, rangeBothInclusive.last + 1)

// CallChain[size=10] = String.qCountLeftSpace() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qCountLeftSpace(): Int = takeWhile { it == ' ' }.count()

// CallChain[size=9] = String.qCountRightSpace() <-[Call]- String.qMoveCenter() <-[Call]- QLineMatch ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qCountRightSpace(): Int = takeLastWhile { it == ' ' }.count()

// CallChain[size=9] = qMASK_LENGTH_LIMIT <-[Call]- Any?.qToLogString() <-[Call]- T.qLog() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal const val qMASK_LENGTH_LIMIT: Int = 100_000

// CallChain[size=11] = QToString <-[Ref]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- An ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QToString(val okToApply: (Any) -> Boolean, val toString: (Any) -> String)

// CallChain[size=10] = qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any?.qToLogString() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=9] = Any?.qToString() <-[Call]- Any?.qToLogString() <-[Call]- T.qLog() <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Any?.qToString(): String {
    if (this == null)
        return "null".light_gray

    for (r in qToStringRegistry) {
        if (r.okToApply(this)) {
            return r.toString(this)
        }
    }

    return toString()
}

// CallChain[size=8] = Any?.qToLogString() <-[Call]- T.qLog() <-[Call]- QOnePassStat.data <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun Any?.qToLogString(maxLineLength: Int = 80): String {
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

// CallChain[size=9] = String.qClarifyEmptyOrBlank() <-[Call]- Any?.qToLogString() <-[Call]- T.qLog( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qClarifyEmptyOrBlank(): String {
    return if (this.isEmpty()) {
        "(EMPTY STRING)".qColor(QShColor.LIGHT_GRAY)
    } else if (this.isBlank()) {
        "$this(BLANK STRING)".qColor(QShColor.LIGHT_GRAY)
    } else {
        this
    }
}