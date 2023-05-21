/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("SpellCheckingInspection", "UNCHECKED_CAST")

package nyab.util

import kotlin.reflect.KClass

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=21] = QFlag <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * Only Enum or QFlag can implement this interface.
 */
internal sealed interface QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=23] = QFlag.bits <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptE ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val bits: Int

    // CallChain[size=23] = QFlag.contains() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>. ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun contains(flags: QFlag<T>): Boolean {
        return (bits and flags.bits) == flags.bits
    }

    // CallChain[size=22] = QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.q ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun toEnumValues(): List<T>

    // CallChain[size=23] = QFlag.str() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOpt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun str(): String {
        return toEnumValues().joinToString(", ") { it.name }
    }

    companion object {
        // https://discuss.kotlinlang.org/t/reified-generics-on-class-level/16711/2
        // But, can't make constructor private ...

        // CallChain[size=21] = QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
        inline fun <reified T>
        none(): QFlag<T> where T : QFlag<T>, T : Enum<T> {
            return QFlagSet<T>(T::class, 0)
        }
    }
}

// CallChain[size=22] = QFlagSet <-[Call]- QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFet ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
/**
 * Mutable bit flag
 */
internal class QFlagSet<T>(val enumClass: KClass<T>, override var bits: Int) : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=24] = QFlagSet.enumValues <-[Call]- QFlagSet.toEnumValues() <-[Propag]- QFlagSet < ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val enumValues: Array<T> by lazy { enumClass.qEnumValues() }

    // CallChain[size=23] = QFlagSet.toEnumValues() <-[Propag]- QFlagSet <-[Call]- QFlag.none() <-[Call] ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toEnumValues(): List<T> =
        enumValues.filter { contains(it) }
}

// CallChain[size=22] = QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLin ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal interface QFlagEnum<T> : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=23] = QFlagEnum.bits <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override val bits: Int
        get() = 1 shl (this as T).ordinal
    // CallChain[size=23] = QFlagEnum.toEnumValues() <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Pa ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    override fun toEnumValues(): List<T> = listOf(this) as List<T>
}