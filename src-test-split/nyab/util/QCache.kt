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

// CallChain[size=2] = qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun <K : Any, V : Any?> qCacheItOneSec(key: K, block: () -> V): V = qCacheItTimed(key, 1000L, block)

// CallChain[size=2] = qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun <K : Any, V : Any> qCacheItOneSecThreadLocal(key: K, block: () -> V): V =
    qCacheItTimedThreadLocal(key, 1000L, block)

// CallChain[size=3] = qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun <K : Any, V : Any> qCacheItTimedThreadLocal(key: K, duration: Long, block: () -> V): V =
    qThreadLocalCache.get().getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=4] = qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qThreadLocalCache: ThreadLocal<QCacheMap> by lazy {
    ThreadLocal.withInitial {
        QCacheMap(
            qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL
        )
    }
}

// CallChain[size=5] = QCacheMap <-[Ref]- qThreadLocalCache <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal class QCacheMap(
    val expirationCheckInterval: Long = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL,
    val threadSafe: Boolean = false
) {
    // CallChain[size=5] = QCacheMap.lastCheck <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    var lastCheck: Long = -1
    // CallChain[size=5] = QCacheMap.lock <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val lock: ReentrantLock = ReentrantLock()
    // CallChain[size=5] = QCacheMap.map <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val map: MutableMap<Any, QCacheEntry> = mutableMapOf()

    // CallChain[size=5] = QCacheMap.clearExpired() <-[Call]- QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun clearExpired(): Int = lock.qWithLock(threadSafe) {
        val toRemove = map.filterValues { it.isExpired() }
        toRemove.forEach { map.remove(it.key) }
        return toRemove.count()
    }

    // CallChain[size=4] = QCacheMap.getOrPut() <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun getOrPut(key: Any, defaultValue: () -> QCacheEntry): QCacheEntry = lock.qWithLock(threadSafe) {
        val now = qNow
        if (now - lastCheck > expirationCheckInterval) {
            lastCheck = now
            clearExpired()
        }

        map.getOrPut(key, defaultValue)
    }
}

// CallChain[size=4] = QCacheEntry <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal data class QCacheEntry(val value: Any?, val duration: Long, val creationTime: Long = qNow) {
    // CallChain[size=6] = QCacheEntry.isExpired() <-[Call]- QCacheMap.clearExpired() <-[Call]- QCacheMa ... edThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    fun isExpired() = (qNow - creationTime) > duration
}

// CallChain[size=6] = qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL <-[Call]- QCacheMap.QCacheMap() < ... edThreadLocal() <-[Call]- qCacheItOneSecThreadLocal() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal const val qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL = 1000L

// CallChain[size=3] = qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal fun <K : Any, V : Any?> qCacheItTimed(key: K, duration: Long, block: () -> V): V =
    qCacheThreadSafe.getOrPut(key) { QCacheEntry(block(), duration, qNow) }.value as V

// CallChain[size=4] = qCacheThreadSafe <-[Call]- qCacheItTimed() <-[Call]- qCacheItOneSec() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
private val qCacheThreadSafe: QCacheMap by lazy { QCacheMap(qDEFAULT_CACHE_IT_EXPIRATION_CHECK_INTERVAL, true) }