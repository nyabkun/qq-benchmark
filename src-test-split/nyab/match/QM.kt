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

// CallChain[size=22] = String.qMatches() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun String.qMatches(matcher: QM): Boolean = matcher.matches(this)

// CallChain[size=5] = not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private val QM.not: QM
    get() = QMatchNot(this)

// CallChain[size=6] = QMatchNot <-[Call]- not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private class QMatchNot(val matcher: QM) : QM {
    // CallChain[size=7] = QMatchNot.matches() <-[Propag]- QMatchNot <-[Call]- not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun matches(text: String): Boolean = !matcher.matches(text)

    // CallChain[size=7] = QMatchNot.toString() <-[Propag]- QMatchNot <-[Call]- not <-[Call]- QM.notExact() <-[Call]- QMMethod.nameNotExact() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString() + "(matcher=$matcher)"
    }
}

// CallChain[size=22] = QMatchAny <-[Call]- QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal object QMatchAny : QM {
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
internal object QMatchNone : QM {
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
internal interface QM {
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