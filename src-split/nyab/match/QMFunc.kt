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

// CallChain[size=9] = QMFunc <-[Ref]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal interface QMFunc {
    // CallChain[size=9] = QMFunc.declaredOnly <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qTo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val declaredOnly: Boolean

    // CallChain[size=9] = QMFunc.includeExtensionsInClass <-[Propag]- QMFunc.IncludeExtensionsInClass < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val includeExtensionsInClass: Boolean

    // CallChain[size=9] = QMFunc.matches() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun matches(value: KFunction<*>): Boolean

    // CallChain[size=9] = QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStrin ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isAny(): Boolean = this == QMatchFuncAny

    // CallChain[size=9] = QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStri ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isNone(): Boolean = this == QMatchFuncNone

    companion object {
        // CallChain[size=8] = QMFunc.DeclaredOnly <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        val DeclaredOnly: QMFunc = QMatchFuncDeclaredOnly

        // CallChain[size=8] = QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry <-[Call]- Any?.qT ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        // TODO OnlyExtensionsInClass
        val IncludeExtensionsInClass: QMFunc = QMatchFuncIncludeExtensionsInClass

        

        // TODO vararg, nullability, param names, type parameter
        // TODO handle createType() more carefully

        // CallChain[size=8] = QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[ ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
        fun nameExact(text: String, ignoreCase: Boolean = false): QMFunc {
            return QMatchFuncName(QM.exact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=8] = QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal infix fun QMFunc.and(match: QMFunc): QMFunc {
    return if (this is QMatchFuncAnd) {
        QMatchFuncAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=9] = QMatchFuncIncludeExtensionsInClass <-[Call]- QMFunc.IncludeExtensionsInClass  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncIncludeExtensionsInClass : QMFuncA() {
    // CallChain[size=10] = QMatchFuncIncludeExtensionsInClass.includeExtensionsInClass <-[Propag]- QMat ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass = true

    // CallChain[size=10] = QMatchFuncIncludeExtensionsInClass.matches() <-[Propag]- QMatchFuncIncludeEx ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=10] = QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtension ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncNone : QMFuncA() {
    // CallChain[size=11] = QMatchFuncNone.matches() <-[Propag]- QMatchFuncNone <-[Call]- QMFunc.isNone( ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return false
    }
}

// CallChain[size=11] = QMFuncA <-[Call]- QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFun ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private abstract class QMFuncA : QMFunc {
    // CallChain[size=12] = QMFuncA.declaredOnly <-[Propag]- QMFuncA <-[Call]- QMatchFuncNone <-[Call]-  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly: Boolean = false
    // CallChain[size=12] = QMFuncA.includeExtensionsInClass <-[Propag]- QMFuncA <-[Call]- QMatchFuncNon ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass: Boolean = false
}

// CallChain[size=10] = QMatchFuncAny <-[Call]- QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsI ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncAny : QMFuncA() {
    // CallChain[size=11] = QMatchFuncAny.matches() <-[Propag]- QMatchFuncAny <-[Call]- QMFunc.isAny() < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=9] = QMatchFuncDeclaredOnly <-[Call]- QMFunc.DeclaredOnly <-[Call]- qToStringRegis ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private object QMatchFuncDeclaredOnly : QMFuncA() {
    // CallChain[size=10] = QMatchFuncDeclaredOnly.declaredOnly <-[Propag]- QMatchFuncDeclaredOnly <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly = true

    // CallChain[size=10] = QMatchFuncDeclaredOnly.matches() <-[Propag]- QMatchFuncDeclaredOnly <-[Call] ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=9] = QMatchFuncAnd <-[Ref]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMatchFuncAnd(vararg match: QMFunc) : QMFuncA() {
    // CallChain[size=9] = QMatchFuncAnd.matchList <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val matchList = match

    // CallChain[size=10] = QMatchFuncAnd.declaredOnly <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMF ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=10] = QMatchFuncAnd.includeExtensionsInClass <-[Propag]- QMatchFuncAnd.matchList < ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override val includeExtensionsInClass: Boolean = matchList.any { it.includeExtensionsInClass }

    // CallChain[size=10] = QMatchFuncAnd.matches() <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFunc ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=9] = qAnd() <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToS ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private fun qAnd(vararg matches: QMFunc): QMFunc = QMatchFuncAnd(*matches)

// CallChain[size=9] = QMatchFuncName <-[Call]- QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Ca ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private class QMatchFuncName(val nameMatcher: QM) : QMFuncA() {
    // CallChain[size=10] = QMatchFuncName.matches() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExa ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return nameMatcher.matches(value.name)
    }

    // CallChain[size=10] = QMatchFuncName.toString() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameEx ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    override fun toString(): String {
        return this::class.simpleName + ":" + nameMatcher.toString()
    }
}