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

import kotlin.math.abs

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
internal fun String.qMakeEqualWidth(
    place: Regex,
    align: QAlign = QAlign.CENTER,
    groupIdx: QGroupIdx = QGroupIdx.FIRST
): String {
    return QLineMatchResult(place, this, false, groupIdx).makeEqualWidth(align)
}

// CallChain[size=2] = QAlign <-[Ref]- QBlock.toString()[Root]
internal enum class QAlign {
    // CallChain[size=4] = QAlign.LEFT <-[Propag]- QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    LEFT,
    // CallChain[size=3] = QAlign.RIGHT <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
    RIGHT,
    // CallChain[size=2] = QAlign.CENTER <-[Call]- QBlock.toString()[Root]
    CENTER
}

// CallChain[size=2] = String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
internal fun String.qAlignRightAll(
    vararg places: Regex = arrayOf("(.*)".re),
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = false, keepLength = keepLength, groupIdx = groupIdx)
}

// CallChain[size=3] = QGroupIdx <-[Ref]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
internal enum class QGroupIdx(val idx: Int) {
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

// CallChain[size=4] = String.qIsNumber() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
internal fun String.qIsNumber(): Boolean {
    return this.trim().matches("""[\d./eE+-]+""".re)
}

// CallChain[size=4] = QLineMatchResult <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
internal class QLineMatchResult(
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

// CallChain[size=6] = QLeftRight <-[Ref]- QLineMatchResult.colDestPos <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
internal data class QLeftRight(val left: Int, val right: Int)

// CallChain[size=5] = qSize <-[Call]- QLineMatchResult.align() <-[Call]- String.qAlign() <-[Call]- String.qAlignRightAll() <-[Call]- QBlock.toString()[Root]
private val IntRange.qSize: Int
    get() = abs(last - first) + 1

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

// CallChain[size=5] = qSpaces() <-[Call]- String.qRangeWithWidth() <-[Call]- QLineMatchResult.makeEqualWidth() <-[Call]- String.qMakeEqualWidth() <-[Call]- QBlock.toString()[Root]
private fun qSpaces(n: Int): String = " ".repeat(n)

// CallChain[size=2] = String.qAlignRight() <-[Call]- QBenchmark.start()[Root]
internal fun String.qAlignRight(
    vararg places: Regex = arrayOf("(.*)".re),
    onlyFirstMatch: Boolean = true,
    keepLength: Boolean = false,
    groupIdx: QGroupIdx = QGroupIdx.ENTIRE_MATCH
): String {
    return qAlign(QAlign.RIGHT, *places, onlyFirstMatch = onlyFirstMatch, keepLength = keepLength, groupIdx = groupIdx)
}