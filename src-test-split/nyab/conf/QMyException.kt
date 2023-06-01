/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.conf

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=12] = QE <-[Ref]- qUnreachable() <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCu ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal typealias QE = QMyException

// CallChain[size=13] = QMyException <-[Ref]- QE <-[Ref]- qUnreachable() <-[Call]- QFetchRule.SINGLE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal enum class QMyException {
    // CallChain[size=14] = QMyException.Other <-[Call]- QException.QException() <-[Ref]- QE.throwIt() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Other,

    // CallChain[size=12] = QMyException.Unreachable <-[Call]- qUnreachable() <-[Call]- QFetchRule.SINGL ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    Unreachable,
    // CallChain[size=8] = QMyException.ShouldBeTrue <-[Call]- Boolean.qaTrue() <-[Call]- String.qWithMi ... el() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    ShouldBeTrue,
    // CallChain[size=21] = QMyException.ShouldNotBeNull <-[Call]- T.qaNotNull() <-[Call]- qSrcFileAtFra ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldNotBeNull,
    // CallChain[size=11] = QMyException.ShouldNotBeZero <-[Call]- Int.qaNotZero() <-[Call]- CharSequenc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldNotBeZero,
    // CallChain[size=19] = QMyException.ShouldBeEvenNumber <-[Call]- qBrackets() <-[Call]- qMySrcLinesA ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    ShouldBeEvenNumber,
    // CallChain[size=20] = QMyException.FileNotFound <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLine ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FileNotFound,
    // CallChain[size=20] = QMyException.FetchLinesFail <-[Call]- Path.qFetchLinesAround() <-[Call]- qSr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    FetchLinesFail,
    // CallChain[size=21] = QMyException.LineNumberExceedsMaximum <-[Call]- Path.qLineAt() <-[Call]- Pat ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    LineNumberExceedsMaximum,
    // CallChain[size=6] = QMyException.TrySetAccessibleFail <-[Call]- AccessibleObject.qTrySetAccessible() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    TrySetAccessibleFail,
    // CallChain[size=7] = QMyException.IllegalArgument <-[Call]- String.qWithMaxLength() <-[Call]- QTim ... ing() <-[Propag]- QBlock <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    IllegalArgument,
    // CallChain[size=7] = QMyException.ConstructorNotFound <-[Call]- Class<T>.qConstructor() <-[Call]-  ... ce() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    ConstructorNotFound,
    // CallChain[size=4] = QMyException.TestFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    TestFail;

    
}

// CallChain[size=14] = qSTACK_FRAME_FILTER <-[Call]- QException.QException() <-[Ref]- QE.throwIt()  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal val qSTACK_FRAME_FILTER: (StackWalker.StackFrame) -> Boolean = {
    !it.className.startsWith("org.gradle") &&
            !it.className.startsWith("org.testng") &&
            !it.className.startsWith("worker.org.gradle") &&
            !it.methodName.endsWith("\$default") && it.fileName != null &&
//            && !it.className.startsWith(QException::class.qualifiedName!!)
//            && it.methodName != "invokeSuspend"
            it.declaringClass != null
//            && it.declaringClass.canonicalName != null
//            && !it.declaringClass.canonicalName.startsWith("kotlin.coroutines.jvm.internal.")
//            && !it.declaringClass.canonicalName.startsWith("kotlinx.coroutines")
}