package com.example.teumteum.utils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async

object RefreshSingleFlight {
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var inFlight: kotlinx.coroutines.Deferred<String?>? = null

    // runBlocking 환경에서도 쓸 수 있게 blocking 버전 제공
    @OptIn(DelicateCoroutinesApi::class)
    fun refreshBlocking(block: suspend () -> String?): String? = kotlinx.coroutines.runBlocking {
        mutex.lock()
        try {
            // 누군가 이미 재발급 중이면 그 결과를 기다림
            inFlight?.let { existing ->
                mutex.unlock() // 대기 중에는 잠금 풀어줌
                return@runBlocking existing.await()
            }
            // 새 재발급 시작
            val job = kotlinx.coroutines.GlobalScope.async(kotlinx.coroutines.Dispatchers.IO) {
                try { block() } finally { /* no-op */ }
            }
            inFlight = job
            mutex.unlock()
            try { job.await() } finally {
                // 완료되면 inFlight 해제
                mutex.lock()
                if (inFlight == job) inFlight = null
                mutex.unlock()
            }
        } catch (t: Throwable) {
            // 예외 시 안전하게 inFlight 초기화
            if (mutex.isLocked) mutex.unlock()
            inFlight = null
            throw t
        }
    }
}