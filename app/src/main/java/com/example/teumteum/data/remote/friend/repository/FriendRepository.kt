package com.example.teumteum.data.remote.friend.repository

import android.util.Log
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.utils.ApiResponse
import javax.inject.Inject

class FriendRepository @Inject constructor(
    private val api: FriendService
) {
    // 1) 닉네임 검색
    suspend fun searchUser(keyword: String): Result<List<FriendSearchResult>> = runCatching {
        val response = api.searchUserByNickname(keyword)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 2) 친구 프로필 조회
    suspend fun getFriendProfile(userId: Int): Result<FriendProfileResult> = runCatching {
        val response = api.getFriendProfile(userId)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: throw Exception("프로필 결과 없음")
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 3) 받은 틈 요청 목록
    suspend fun getReceivedTeums(): Result<List<TeumReceivedItem>> = runCatching {
        val response = api.getReceivedTeumRequests()
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.content ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 4) 틈 요청 보내기
    suspend fun sendTeumRequest(request: TeumRequest): Result<Int> = runCatching {
        val response = api.sendTeumRequest(request)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.id ?: throw Exception("teumId 없음")
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 5) 틈 응답(수락/거절)
    suspend fun respondToTeum(responseId: Int, status: String): Result<TeumStatusResult> = runCatching {
        val request = TeumStatusRequest(status)
        val response = api.patchTeumStatus(responseId, request)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: "오류"}")
        }
    }

    // 6) 팔로우
    suspend fun followUser(userId: Int): Result<ApiResponse<Unit>> = runCatching {
        val response = api.followUser(userId)
        val body = response.body()
        if (response.isSuccessful && body != null) {
            body
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 7) 팔로잉 목록
    suspend fun getFollowings(page: Int, size: Int): Result<List<FollowingResult>> = runCatching {
        val response = api.getFollowings(page, size)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.content ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 8) 약속된 틈 달력(점 표시용 날짜 리스트)
    suspend fun getScheduledTeumCalendar(month: String): Result<List<String>> = runCatching {
        val response = api.getScheduledTeumCalendar(month)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 9) 특정 날짜의 약속된 틈 리스트
    suspend fun getScheduledTeums(date: String): Result<List<TeumScheduledResult>> = runCatching {
        val response = api.getScheduledTeums(date)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 10) 약속된 틈 상세
    suspend fun getTeumScheduleDetail(teumId: Int): Result<TeumScheduleDetailResult> = runCatching {
        val response = api.getScheduleDetail(teumId)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 11) 약속된 틈 취소
    suspend fun cancelTeumSchedule(teumId: Int): Result<CancelTeumResult> = runCatching {
        val response = api.cancelTeumSchedule(teumId)
        val body = response.body()
        Log.d("TEUM_CANCEL_TEST", "취소 요청한 teumId: $teumId")
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }
}
