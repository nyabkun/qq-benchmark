/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.match

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=13] = QM <-[Ref]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal interface QM {
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

// CallChain[size=14] = QMatchNone <-[Call]- QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal object QMatchNone : QM {
    // CallChain[size=15] = QMatchNone.matches() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return false
    }

    // CallChain[size=15] = QMatchNone.toString() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=14] = QMatchAny <-[Call]- QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal object QMatchAny : QM {
    // CallChain[size=15] = QMatchAny.matches() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]- Q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(text: String): Boolean {
        return true
    }

    // CallChain[size=15] = QMatchAny.toString() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=14] = String.qMatches() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun String.qMatches(matcher: QM): Boolean = matcher.matches(this)

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