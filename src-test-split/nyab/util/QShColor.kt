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

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=13] = light_gray <-[Call]- QE.throwIt() <-[Call]- qUnreachable() <-[Call]- QFetchR ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.light_gray: String
    get() = this?.qColor(QShColor.LIGHT_GRAY) ?: "null".qColor(QShColor.LIGHT_GRAY)

// CallChain[size=16] = yellow <-[Call]- QException.qToString() <-[Call]- QException.toString() <-[P ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.yellow: String
    get() = this?.qColor(QShColor.YELLOW) ?: "null".qColor(QShColor.YELLOW)

// CallChain[size=17] = String.qColor() <-[Call]- yellow <-[Call]- QException.qToString() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qColor(fg: QShColor? = null, bg: QShColor? = null, nestable: Boolean = this.contains(qSTART)): String {
    return if (this.qIsSingleLine()) {
        this.qColorLine(fg, bg, nestable)
    } else {
        lineSequence().map { line ->
            line.qColorLine(fg, bg, nestable)
        }.joinToString("\n")
    }
}

// CallChain[size=17] = QShColor <-[Ref]- yellow <-[Call]- QException.qToString() <-[Call]- QExcepti ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
enum class QShColor(val code: Int) {
    // CallChain[size=18] = QShColor.BLACK <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcep ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    BLACK(30),
    // CallChain[size=18] = QShColor.RED <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcepti ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    RED(31),
    // CallChain[size=18] = QShColor.GREEN <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcep ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    GREEN(32),
    // CallChain[size=17] = QShColor.YELLOW <-[Call]- yellow <-[Call]- QException.qToString() <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    YELLOW(33),
    // CallChain[size=18] = QShColor.BLUE <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    BLUE(34),
    // CallChain[size=18] = QShColor.MAGENTA <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    MAGENTA(35),
    // CallChain[size=18] = QShColor.CYAN <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcept ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    CYAN(36),
    // CallChain[size=18] = QShColor.LIGHT_GRAY <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_GRAY(37),

    // CallChain[size=18] = QShColor.DARK_GRAY <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    DARK_GRAY(90),
    // CallChain[size=18] = QShColor.LIGHT_RED <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_RED(91),
    // CallChain[size=18] = QShColor.LIGHT_GREEN <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_GREEN(92),
    // CallChain[size=18] = QShColor.LIGHT_YELLOW <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_YELLOW(93),
    // CallChain[size=18] = QShColor.LIGHT_BLUE <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_BLUE(94),
    // CallChain[size=18] = QShColor.LIGHT_MAGENTA <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_MAGENTA(95),
    // CallChain[size=18] = QShColor.LIGHT_CYAN <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- Q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LIGHT_CYAN(96),
    // CallChain[size=18] = QShColor.WHITE <-[Propag]- QShColor.YELLOW <-[Call]- yellow <-[Call]- QExcep ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    WHITE(97);

    // CallChain[size=19] = QShColor.fg <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /** ANSI modifier string to apply the color to the text itself */
    val fg: String = "$qSTART${code}m"

    // CallChain[size=19] = QShColor.bg <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    /** ANSI modifier string to apply the color the text's background */
    val bg: String = "$qSTART${code + qBG_JUMP}m"

    companion object {
        
    }
}

// CallChain[size=18] = qSTART <-[Call]- String.qColor() <-[Call]- yellow <-[Call]- QException.qToSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qSTART = "\u001B["

// CallChain[size=18] = String.qColorLine() <-[Call]- String.qColor() <-[Call]- yellow <-[Call]- QEx ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=19] = qEND <-[Call]- String.qColorLine() <-[Call]- String.qColor() <-[Call]- yello ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qEND = "${qSTART}0m"

// CallChain[size=19] = String.qApplyColorNestable() <-[Call]- String.qColorLine() <-[Call]- String. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=20] = qBG_JUMP <-[Call]- QShColor.bg <-[Call]- String.qColorLine() <-[Call]- Strin ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private const val qBG_JUMP = 10

// CallChain[size=20] = qMASK_COLORED <-[Call]- String.qApplyColorNestable() <-[Call]- String.qColor ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
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

// CallChain[size=25] = cyan <-[Call]- QMaskResult.toString() <-[Propag]- QMaskResult <-[Ref]- QMask ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.cyan: String
    get() = this?.qColor(QShColor.CYAN) ?: "null".qColor(QShColor.CYAN)

// CallChain[size=16] = String.qColorTarget() <-[Call]- QException.mySrcAndStack <-[Call]- QExceptio ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qColorTarget(ptn: Regex, color: QShColor = QShColor.LIGHT_YELLOW): String {
    return ptn.replace(this, "$0".qColor(color))
}

// CallChain[size=17] = noColor <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String.noColor: String
    get() {
        return this.replace("""\Q$qSTART\E\d{1,2}m""".re, "")
    }

// CallChain[size=19] = light_green <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.light_green: String
    get() = this?.qColor(QShColor.LIGHT_GREEN) ?: "null".qColor(QShColor.LIGHT_GREEN)

// CallChain[size=21] = light_cyan <-[Call]- qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.light_cyan: String
    get() = this?.qColor(QShColor.LIGHT_CYAN) ?: "null".qColor(QShColor.LIGHT_CYAN)

// CallChain[size=5] = red <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.red: String
    get() = this?.qColor(QShColor.RED) ?: "null".qColor(QShColor.RED)

// CallChain[size=5] = blue <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.blue: String
    get() = this?.qColor(QShColor.BLUE) ?: "null".qColor(QShColor.BLUE)

// CallChain[size=5] = green <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.green: String
    get() = this?.qColor(QShColor.GREEN) ?: "null".qColor(QShColor.GREEN)

// CallChain[size=5] = dark_gray <-[Call]- QBlock.toString() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val String?.dark_gray: String
    get() = this?.qColor(QShColor.DARK_GRAY) ?: "null".qColor(QShColor.DARK_GRAY)

// CallChain[size=4] = light_blue <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal val String?.light_blue: String
    get() = this?.qColor(QShColor.LIGHT_BLUE) ?: "null".qColor(QShColor.LIGHT_BLUE)

// CallChain[size=7] = light_red <-[Call]- allTestedMethods <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal val String?.light_red: String
    get() = this?.qColor(QShColor.LIGHT_RED) ?: "null".qColor(QShColor.LIGHT_RED)