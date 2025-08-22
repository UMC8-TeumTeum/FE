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
){
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

            // id 내림차순으로 정렬 → 최신순 → 최대 2개만
            val trimmed = (body.result ?: emptyList())
                .sortedByDescending { it.id }
                .take(2)

            trimmed
        } else {
            Log.d("RECENT_TODO", "오늘의 투두리스트 조회에 실패했습니다.")
            throw Exception("오늘의 투두리스트 조회에 실패했습니다.")
        }
    }
}