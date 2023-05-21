/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package nyab.util

import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import nyab.conf.QMyPath
import nyab.match.QM

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=9] = qCacheItOneSecThreadLocal() <-[Call]- qRe() <-[Call]- QException.mySrcAndStac ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun <K : Any, V : Any> qCacheItOneSecThreadLocal(key: K, block: () -> V): V =
    qCacheItTimedThreadLocal(key, 1000L, block)

// CallChain[size=10] = qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- q ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun <K : Any, V : Any> qCacheItTimedThreadLocal(key: K, duration: Long, block: () -> V): V =
    qThreadLocalCache.get().getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=11] = qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOne ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qThreadLocalCache: ThreadLocal<QCacheMap> by lazy {
    ThreadLocal.withInitial {
        QCacheMap(
            qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL
        )
    }
}

// CallChain[size=12] = QCacheMap <-[Ref]- qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal class QCacheMap(
    val expirationCheckInterval: Long = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL,
    val threadSafe: Boolean = false
) {
    // CallChain[size=12] = QCacheMap.lastCheck <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedTh ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    var lastCheck: Long = -1
    // CallChain[size=12] = QCacheMap.lock <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadL ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val lock: ReentrantLock = ReentrantLock()
    // CallChain[size=12] = QCacheMap.map <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLo ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    val map: MutableMap<Any, QCacheEntry> = mutableMapOf()

    // CallChain[size=12] = QCacheMap.clearExpired() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTi ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun clearExpired(): Int = lock.qWithLock(threadSafe) {
        val toRemove = map.filterValues { it.isExpired() }
        toRemove.forEach { map.remove(it.key) }
        return toRemove.count()
    }

    // CallChain[size=11] = QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheIt ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun getOrPut(key: Any, defaultValue: () -> QCacheEntry): QCacheEntry = lock.qWithLock(threadSafe) {
        val now = qNow
        if (now - lastCheck > expirationCheckInterval) {
            lastCheck = now
            clearExpired()
        }

        map.getOrPut(key, defaultValue)
    }
}

// CallChain[size=11] = QCacheEntry <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThr ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal data class QCacheEntry(val value: Any?, val duration: Long, val creationTime: Long = qNow) {
    // CallChain[size=13] = QCacheEntry.isExpired() <-[Call]- QCacheMap.clearExpired() <-[Call]- QCacheM ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
    fun isExpired() = (qNow - creationTime) > duration
}

// CallChain[size=13] = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL <-[Call]- QCacheMap.QCacheMap()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal const val qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL = 1000L

// CallChain[size=10] = qCacheItOneSec() <-[Call]- qMySrcLinesAtFrame() <-[Call]- qLogStackFrames()  ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun <K : Any, V : Any?> qCacheItOneSec(key: K, block: () -> V): V = qCacheItTimed(key, 1000L, block)

// CallChain[size=11] = qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- qMySrcLinesAtFrame() <- ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
internal fun <K : Any, V : Any?> qCacheItTimed(key: K, duration: Long, block: () -> V): V =
    qCacheThreadSafe.getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=12] = qCacheThreadSafe <-[Call]- qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Cal ... <-[Call]- String.qWithMaxLength() <-[Call]- QTimeAndResult.str() <-[Call]- QBlock.toString()[Root]
private val qCacheThreadSafe: QCacheMap by lazy { QCacheMap(qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL, true) }