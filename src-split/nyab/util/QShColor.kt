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

import kotlin.math.absoluteValue

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=10] = qBG_JUMP <-[Call]- QShColor.bg <-[Propag]- QShColor.LightYellow <-[Call]- QE ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qBG_JUMP = 10

// CallChain[size=10] = qSTART <-[Call]- QShColor.bg <-[Propag]- QShColor.LightYellow <-[Call]- QExc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qSTART = "\u001B["

// CallChain[size=12] = qEND <-[Call]- String.qApplyEscapeLine() <-[Call]- String.qApplyEscapeLine() ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private const val qEND = "${qSTART}0m"

// CallChain[size=12] = String.qApplyEscapeNestable() <-[Call]- String.qApplyEscapeLine() <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qApplyEscapeNestable(start: String): String {
    val lastEnd = this.endsWith(qEND)

    return if( lastEnd ) {
            start + this.substring(0, this.length - 1).replace(qEND, qEND + start) + this[this.length - 1]
        } else {
            start + this.replace(qEND, qEND + start) + qEND
        }
}

// CallChain[size=9] = String.qColor() <-[Call]- String.qColorTarget() <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun String.qColor(fg: QShColor? = null, bg: QShColor? = null, nestable: Boolean = this.contains(qSTART)): String {
    return if (this.qIsSingleLine()) {
        this.qApplyEscapeLine(fg, bg, nestable)
    } else {
        lineSequence().map { line ->
            line.qApplyEscapeLine(fg, bg, nestable)
        }.joinToString("\n")
    }
}

// CallChain[size=10] = String.qApplyEscapeLine() <-[Call]- String.qColor() <-[Call]- String.qColorT ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun String.qApplyEscapeLine(fg: QShColor?, bg: QShColor?, nestable: Boolean): String {
    return this.qApplyEscapeLine(
        listOfNotNull(fg?.fg, bg?.bg).toTypedArray(),
        nestable
    )
}

// CallChain[size=11] = String.qApplyEscapeLine() <-[Call]- String.qApplyEscapeLine() <-[Call]- Stri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
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

// CallChain[size=14] = noStyle <-[Call]- QConsole.print() <-[Propag]- QConsole <-[Call]- QOut.CONSO ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal val String.noStyle: String
    get() {
        return this.replace("""\Q$qSTART\E\d{1,2}m""".re, "")
    }

// CallChain[size=8] = QShColor <-[Ref]- QException.mySrcAndStack <-[Call]- QException.printStackTra ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal enum class QShColor(val code: Int) {
    // CallChain[size=9] = QShColor.Black <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Black(30),
    // CallChain[size=9] = QShColor.Red <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAndSt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Red(31),
    // CallChain[size=9] = QShColor.Green <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Green(32),
    // CallChain[size=9] = QShColor.Yellow <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Yellow(33),
    // CallChain[size=9] = QShColor.Blue <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAndS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Blue(34),
    // CallChain[size=9] = QShColor.Purple <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAn ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Purple(35),
    // CallChain[size=9] = QShColor.Cyan <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAndS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    Cyan(36),
    // CallChain[size=9] = QShColor.LightGray <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightGray(37),

    // CallChain[size=9] = QShColor.DefaultFG <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DefaultFG(39),
    // CallChain[size=9] = QShColor.DefaultBG <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DefaultBG(49),

    // CallChain[size=9] = QShColor.DarkGray <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    DarkGray(90),
    // CallChain[size=9] = QShColor.LightRed <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightRed(91),
    // CallChain[size=9] = QShColor.LightGreen <-[Propag]- QShColor.LightYellow <-[Call]- QException.myS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightGreen(92),
    // CallChain[size=8] = QShColor.LightYellow <-[Call]- QException.mySrcAndStack <-[Call]- QException. ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightYellow(93),
    // CallChain[size=9] = QShColor.LightBlue <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightBlue(94),
    // CallChain[size=9] = QShColor.LightPurple <-[Propag]- QShColor.LightYellow <-[Call]- QException.my ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightPurple(95),
    // CallChain[size=9] = QShColor.LightCyan <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    LightCyan(96),
    // CallChain[size=9] = QShColor.White <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    White(97);

    // CallChain[size=9] = QShColor.fg <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAndSta ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val fg: String = "$qSTART${code}m"

    // CallChain[size=9] = QShColor.bg <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAndSta ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val bg: String = "$qSTART${code + qBG_JUMP}m"

    companion object {
        // CallChain[size=9] = QShColor.random() <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun random(seed: String, colors: Array<QShColor> = arrayOf(Yellow, Green, Blue, Purple, Cyan)): QShColor {
            val idx = seed.hashCode().rem(colors.size).absoluteValue
            return colors[idx]
        }

        // CallChain[size=9] = QShColor.get() <-[Propag]- QShColor.LightYellow <-[Call]- QException.mySrcAnd ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun get(ansiEscapeCode: Int): QShColor {
            return QShColor.values().find {
                it.code == ansiEscapeCode
            }!!
        }
    }
}

// CallChain[size=8] = String.qColorTarget() <-[Call]- QException.mySrcAndStack <-[Call]- QException ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun String.qColorTarget(ptn: Regex, fg: QShColor? = null, bg: QShColor? = null): String {
    return ptn.replace(this, "$0".qColor(fg, bg))
}

// CallChain[size=2] = red <-[Call]- QBlock.toString()[Root]
internal val String?.red: String
    get() = this?.qColor(QShColor.Red) ?: "null".qColor(QShColor.Red)

// CallChain[size=2] = green <-[Call]- QBlock.toString()[Root]
internal val String?.green: String
    get() = this?.qColor(QShColor.Green) ?: "null".qColor(QShColor.Green)

// CallChain[size=2] = yellow <-[Call]- QBlock.toString()[Root]
internal val String?.yellow: String
    get() = this?.qColor(QShColor.Yellow) ?: "null".qColor(QShColor.Yellow)

// CallChain[size=2] = blue <-[Call]- QBlock.toString()[Root]
internal val String?.blue: String
    get() = this?.qColor(QShColor.Blue) ?: "null".qColor(QShColor.Blue)

// CallChain[size=10] = cyan <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QExcepti ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal val String?.cyan: String
    get() = this?.qColor(QShColor.Cyan) ?: "null".qColor(QShColor.Cyan)

// CallChain[size=5] = light_gray <-[Call]- QE.throwIt() <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal val String?.light_gray: String
    get() = this?.qColor(QShColor.LightGray) ?: "null".qColor(QShColor.LightGray)

// CallChain[size=2] = dark_gray <-[Call]- QBlock.toString()[Root]
internal val String?.dark_gray: String
    get() = this?.qColor(QShColor.DarkGray) ?: "null".qColor(QShColor.DarkGray)

// CallChain[size=11] = light_green <-[Call]- QLogStyle.qLogArrow() <-[Call]- QLogStyle.S <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal val String?.light_green: String
    get() = this?.qColor(QShColor.LightGreen) ?: "null".qColor(QShColor.LightGreen)

// CallChain[size=13] = light_cyan <-[Call]- qARROW <-[Call]- qArrow() <-[Call]- QLogStyle.qLogArrow ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal val String?.light_cyan: String
    get() = this?.qColor(QShColor.LightCyan) ?: "null".qColor(QShColor.LightCyan)