package com.umc.teumteum.utils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async

object RefreshSingleFlight {
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var inFlight: kotlinx.coroutines.Deferred<String?>? = null

    /**
     * 동시 재발급 요청을 단 1회로 합쳐주는 유틸
     * - 이미 재발급이 진행 중이면 그 결과를 기다렸다가 그대로 반환
     * - 새로운 재발급이 필요하면 직접 실행
     * - runBlocking 환경에서도 호출 가능
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun refreshBlocking(block: suspend () -> String?): String? = kotlinx.coroutines.runBlocking {
        mutex.lock()
        try {
            // 이미 재발급 진행 중이라면 → 그 Job 결과를 기다리고 반환
            inFlight?.let { existing ->
                mutex.unlock() // 다른 호출자들이 대기하지 않도록 잠금 해제
                return@runBlocking existing.await()
            }
            // 아직 재발급이 없으면 → 새로운 Job 실행
            val job = kotlinx.coroutines.GlobalScope.async(kotlinx.coroutines.Dispatchers.IO) {
                try { block() } finally { /* no-op */ }
            }
            inFlight = job
            mutex.unlock()
            try {
                // 실행한 Job 결과 반환
                job.await()
            } finally {
                // 완료된 Job이면 inFlight 초기화
                mutex.lock()
                if (inFlight == job) inFlight = null
                mutex.unlock()
            }
        } catch (t: Throwable) {
            // 예외 발생 시에도 inFlight 초기화 보장
            if (mutex.isLocked) mutex.unlock()
            inFlight = null
            throw t
        }
    }
}