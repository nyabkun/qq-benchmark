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

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.extensionReceiverParameter
import nyab.util.QFlagEnum

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=12] = QMFunc <-[Ref]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal interface QMFunc {
    // CallChain[size=12] = QMFunc.declaredOnly <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qT ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val declaredOnly: Boolean

    // CallChain[size=12] = QMFunc.includeExtensionsInClass <-[Propag]- QMFunc.IncludeExtensionsInClass  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val includeExtensionsInClass: Boolean

    // CallChain[size=12] = QMFunc.matches() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun matches(value: KFunction<*>): Boolean

    // CallChain[size=12] = QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStri ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isAny(): Boolean = this == QMatchFuncAny

    // CallChain[size=12] = QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStr ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isNone(): Boolean = this == QMatchFuncNone

    companion object {
        // CallChain[size=11] = QMFunc.DeclaredOnly <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        val DeclaredOnly: QMFunc = QMatchFuncDeclaredOnly

        // CallChain[size=11] = QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry <-[Call]- Any?.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        // TODO OnlyExtensionsInClass
        val IncludeExtensionsInClass: QMFunc = QMatchFuncIncludeExtensionsInClass

        

        // TODO vararg, nullability, param names, type parameter
        // TODO handle createType() more carefully

        // CallChain[size=11] = QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        fun nameExact(text: String, ignoreCase: Boolean = false): QMFunc {
            return QMatchFuncName(QM.exact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=11] = QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal infix fun QMFunc.and(match: QMFunc): QMFunc {
    return if (this is QMatchFuncAnd) {
        QMatchFuncAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=12] = QMatchFuncIncludeExtensionsInClass <-[Call]- QMFunc.IncludeExtensionsInClass ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncIncludeExtensionsInClass : QMFuncA() {
    // CallChain[size=13] = QMatchFuncIncludeExtensionsInClass.includeExtensionsInClass <-[Propag]- QMat ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass = true

    // CallChain[size=13] = QMatchFuncIncludeExtensionsInClass.matches() <-[Propag]- QMatchFuncIncludeEx ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=13] = QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtension ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncNone : QMFuncA() {
    // CallChain[size=14] = QMatchFuncNone.matches() <-[Propag]- QMatchFuncNone <-[Call]- QMFunc.isNone( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return false
    }
}

// CallChain[size=14] = QMFuncA <-[Call]- QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFun ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private abstract class QMFuncA : QMFunc {
    // CallChain[size=15] = QMFuncA.declaredOnly <-[Propag]- QMFuncA <-[Call]- QMatchFuncNone <-[Call]-  ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly: Boolean = false
    // CallChain[size=15] = QMFuncA.includeExtensionsInClass <-[Propag]- QMFuncA <-[Call]- QMatchFuncNon ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass: Boolean = false
}

// CallChain[size=13] = QMatchFuncAny <-[Call]- QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsI ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncAny : QMFuncA() {
    // CallChain[size=14] = QMatchFuncAny.matches() <-[Propag]- QMatchFuncAny <-[Call]- QMFunc.isAny() < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=12] = QMatchFuncDeclaredOnly <-[Call]- QMFunc.DeclaredOnly <-[Call]- qToStringRegi ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private object QMatchFuncDeclaredOnly : QMFuncA() {
    // CallChain[size=13] = QMatchFuncDeclaredOnly.declaredOnly <-[Propag]- QMatchFuncDeclaredOnly <-[Ca ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly = true

    // CallChain[size=13] = QMatchFuncDeclaredOnly.matches() <-[Propag]- QMatchFuncDeclaredOnly <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=12] = QMatchFuncAnd <-[Ref]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- An ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMatchFuncAnd(vararg match: QMFunc) : QMFuncA() {
    // CallChain[size=12] = QMatchFuncAnd.matchList <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val matchList = match

    // CallChain[size=13] = QMatchFuncAnd.declaredOnly <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMF ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=13] = QMatchFuncAnd.includeExtensionsInClass <-[Propag]- QMatchFuncAnd.matchList < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val includeExtensionsInClass: Boolean = matchList.any { it.includeExtensionsInClass }

    // CallChain[size=13] = QMatchFuncAnd.matches() <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFunc ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=12] = qAnd() <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qTo ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private fun qAnd(vararg matches: QMFunc): QMFunc = QMatchFuncAnd(*matches)

// CallChain[size=12] = QMatchFuncName <-[Call]- QMFunc.nameExact() <-[Call]- qToStringRegistry <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private class QMatchFuncName(val nameMatcher: QM) : QMFuncA() {
    // CallChain[size=13] = QMatchFuncName.matches() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return nameMatcher.matches(value.name)
    }

    // CallChain[size=13] = QMatchFuncName.toString() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameEx ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toString(): String {
        return this::class.simpleName + ":" + nameMatcher.toString()
    }
}