package com.example.teumteum.data.remote.mypage.repository

import android.util.Log
import com.example.teumteum.data.remote.friend.model.PublicTodoResult
import com.example.teumteum.data.remote.mypage.model.MyInfoResponse
import com.example.teumteum.data.remote.mypage.service.MyPageService
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.utils.handleApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService
) {
    //마이페이지 조회
    suspend fun getMyInfo(): Result<MyInfoResponse> = runCatching {
        val response = myPageService.getMyInfo()
        Log.d("MyInfo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 최근 투두 조회 (최신 등록순 2개)
    suspend fun getRecentTodos(date: String): Result<List<TodoListResult>> = runCatching {
        val response = myPageService.getRecentTodos(date)
        val http = response.code()
        val body = response.body()

        // HTTP 실패
        if (!response.isSuccessful) {
            Log.d("RECENT_PUBLIC_TODO", "서버 오류")
            throw Exception("HTTP $http - ${response.errorBody()?.string() ?: response.message()}")
        }

        // 본문 없음
        if (body == null) {
            Log.d("RECENT_PUBLIC_TODO", "응답 본문이 비어있습니다.")
            throw Exception("응답 본문이 비어있습니다.")
        }

        // 성공/실패 분기 (로그 한 줄만)
        if (body.isSuccess && body.code == "HOME20015") {
            Log.d("RECENT_TODO", body.message)

            val zone = java.time.ZoneId.systemDefault()
            val today = java.time.LocalDate.now(zone)
            val nowTime = java.time.LocalTime.now(zone)

            val targetDate = runCatching { java.time.LocalDate.parse(date) }
                .getOrElse { throw Exception("잘못된 날짜 형식입니다. yyyy-MM-dd 형식이어야 합니다.") }

            val filtered = (body.result ?: emptyList()).asSequence()
                .filter { it.isPublic }
                .filter { item ->
                    val start = parseLocalTimeHHmm(item.startTime) ?: return@filter false
                    when {
                        targetDate.isAfter(today) -> true                // 미래 날짜: 전부 포함
                        targetDate.isEqual(today) -> start.isAfter(nowTime) || start == nowTime
                        else -> false                                     // 과거 날짜: 제외
                    }
                }
                .sortedWith(
                    compareBy<TodoListResult> { parseLocalTimeHHmm(it.startTime) }
                        .thenBy { parseLocalTimeHHmm(it.endTime) }
                        .thenByDescending { it.id }
                )
                .take(2)
                .toList()

            filtered
        } else {
            Log.d("RECENT_TODO", "오늘의 투두리스트 조회에 실패했습니다.")
            throw Exception("오늘의 투두리스트 조회에 실패했습니다.")
        }
    }

    private fun parseLocalTimeHHmm(s: String?): java.time.LocalTime? {
        if (s.isNullOrBlank()) return null
        return runCatching {
            java.time.LocalTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrNull()
    }
}