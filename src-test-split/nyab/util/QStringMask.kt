/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package nyab.util

import kotlin.math.max
import kotlin.math.min

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=10] = String.qCountOccurrence() <-[Call]- QFetchRule.SMART_FETCH <-[Call]- QSrcCut ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qCountOccurrence(word: String): Int {
    return windowed(word.length) {
        if (it == word)
            1
        else
            0
    }.sum()
}

// CallChain[size=12] = QMask <-[Ref]- QMaskBetween <-[Call]- QMask.DOUBLE_QUOTE <-[Call]- QMask.KOT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal interface QMask {
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
internal class QMultiMask(vararg mask: QMaskBetween) : QMask {
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
internal class QMaskBetween(
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
internal open class QMutRegion(override var start: Int, override var end: Int) : QRegion(start, end) {
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
internal open class QRegion(open val start: Int, open val end: Int) {
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
internal class QReplacer(start: Int, end: Int, val replacement: String) : QMutRegion(start, end)

// CallChain[size=11] = QMaskResult <-[Ref]- QMask.apply() <-[Propag]- QMask.KOTLIN_STRING <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QMaskResult(val maskedStr: String, val orgText: String, val maskChar: Char) {
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
internal fun CharSequence.qMask(vararg mask: QMask): QMaskResult {
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
internal fun String.qFindBetween(
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
internal fun CharSequence.qMultiReplace(replacers: List<QReplacer>): String {
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
internal fun MatchResult.qResolveReplacementGroup(replacement: String, orgText: String): String {
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
internal fun CharSequence.qReplace(oldValue: String, newValue: String, escapeChar: Char): String {
    return replace(Regex("""(?<!\Q$escapeChar\E)\Q$oldValue\E"""), newValue)
}

// CallChain[size=16] = QSequenceReader <-[Call]- QBetween.find() <-[Call]- String.qFindBetween() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QSequenceReader(text: CharSequence) : QCharReader(text) {
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
internal open class QCharReader(val text: CharSequence) {
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