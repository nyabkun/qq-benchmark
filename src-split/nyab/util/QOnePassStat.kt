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

import kotlin.math.min
import kotlin.math.sqrt

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QOnePassStat <-[Ref]- QBlock.stat[Root]
/**
 * Basically, stats are calculated by one-pass algorithm,
 * but some data must be kept in memory to calculate the median.
 */
@Suppress("NonAsciiCharacters", "PropertyName")
internal class QOnePassStat(val maxSamplesInHeap: Int = 10_000) {
    // CallChain[size=3] = QOnePassStat.n <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var n: Long = 0
    // CallChain[size=3] = QOnePassStat.μ <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var μ: Double = .0
    // CallChain[size=3] = QOnePassStat.m2 <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var m2: Double = .0
    // CallChain[size=3] = QOnePassStat.σ2 <-[Call]- QOnePassStat.σ <-[Call]- QBlock.timeStandardDeviation[Root]
    val σ2: Double
        get() = m2 / n
    // CallChain[size=3] = QOnePassStat.max <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var max: Double = .0
    // CallChain[size=3] = QOnePassStat.min <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    var min: Double = .0
    // CallChain[size=2] = QOnePassStat.σ <-[Call]- QBlock.timeStandardDeviation[Root]
    val σ: Double
        get() = sqrt(σ2)
    // CallChain[size=3] = QOnePassStat.dataHead <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    // We should keep data in memory to calculate median
    private val dataHead = QFixedSizeList<Double>(maxSamplesInHeap / 2)
    // CallChain[size=3] = QOnePassStat.dataTail <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    private val dataTail = QFixedSizeQueue<Double>(maxSamplesInHeap / 2)

    // CallChain[size=3] = QOnePassStat.data <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
    private val data: DoubleArray
        get() {
            val size = min(maxSamplesInHeap.toLong(), n).toInt()

            return DoubleArray(size) { idx ->
                if (idx < dataHead.maxSize) { // idx = 0 ~ 49 ( if dataHead.size = 100 )
                    dataHead[idx]
                } else { // idx = 50 ~ 99 ( if dataHead.size = 100 )
                    idx.qLog()
                    dataTail[idx - dataHead.maxSize]
                }
            }
        }

    // CallChain[size=2] = QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
    val median: Double
        get() = data.qMedian()

    // CallChain[size=2] = QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    fun add(xNew: Double) {
        if (!dataHead.add(xNew)) {
            dataTail.add(xNew)
        }

        if (xNew > max) {
            max = xNew
        }

        if (xNew < min) {
            min = xNew
        }

        n++
        val delta = xNew - μ
        μ += delta / n
        val delta2 = xNew - μ
        m2 += delta * delta2
    }
}

// CallChain[size=4] = QFixedSizeList <-[Call]- QOnePassStat.dataHead <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
internal class QFixedSizeList<T>(val maxSize: Int, val backend: MutableList<T> = ArrayList(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=3] = QFixedSizeList.add() <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    override fun add(element: T): Boolean {
        return if (this.size < maxSize) {
            backend.add(element)
        } else {
            false
        }
    }
}

// CallChain[size=4] = QFixedSizeQueue <-[Call]- QOnePassStat.dataTail <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
internal class QFixedSizeQueue<T>(val maxSize: Int, val backend: ArrayDeque<T> = ArrayDeque(maxSize)) :
    MutableList<T> by backend {
    // CallChain[size=3] = QFixedSizeQueue.add() <-[Call]- QOnePassStat.add() <-[Call]- QBlock.timeIt()[Root]
    override fun add(element: T): Boolean {
        return if (this.size < maxSize) {
            backend.add(element)
        } else {
            backend.removeFirst()
            backend.addLast(element)
            true
        }
    }
}

// CallChain[size=3] = DoubleArray.qMedian() <-[Call]- QOnePassStat.median <-[Call]- QBlock.timeMedian[Root]
internal fun DoubleArray.qMedian(): Double =
    sorted().let {
        if (it.size % 2 == 0)
            (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
        else
            it[it.size / 2]
    }