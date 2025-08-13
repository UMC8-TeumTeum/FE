package com.example.teumteum.data.remote.friend.repository

import android.util.Log
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.utils.ApiResponse
import com.example.teumteum.utils.handleApiResponse
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
//        val response = api.sendTeumRequest(request)
//        val body = response.body()
//        if (response.isSuccessful && body?.isSuccess == true) {
//            Log.d("SEND_TEUM_REQUEST", body.toString())
//            body.result ?: throw Exception("teumId 없음")
//        } else {
//            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
//        }

        val response = api.sendTeumRequest(request)
        Log.d("SendTeumRequest", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 5) 틈 응답(수락/거절)
    suspend fun respondToTeum(responseId: Int, status: String): Result<TeumStatusResult> = runCatching {
        val req = TeumStatusRequest(status)
        val response = api.patchTeumStatus(responseId, req)
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

    // 12) 언팔로우
    suspend fun unfollowUser(userId: Int): Result<ApiResponse<String>> = runCatching {
        val response = api.unfollow(userId.toLong())
        val body = response.body()
        Log.d("UNFOLLOW_TEST", "언팔로우 요청한 userId: $userId")
        if (response.isSuccessful && body != null) {
            body
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 13) 친구 즐겨찾기 설정/해제
    suspend fun setFavorite(userId: Int, isFavorite: Boolean): Result<FavoriteResult> = runCatching {
        val response = api.setFavorite(userId, FavoriteRequest(isFavorite))
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 14) 팔로워 목록
    suspend fun getFollowers(page: Int, size: Int): Result<List<FollowerResult>> = runCatching {
        val response = api.getFollowers(page, size)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            Log.d("FOLLOWER_FRAGMENT", "친구 목록 조회에 성공하였습니다. message=${body.message}")
            body.result?.content ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 14-1) 팔로워 목록
    suspend fun getFollowersPage(page: Int, size: Int): Result<FollowerPageResult> = runCatching {
        val response = api.getFollowers(page, size)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            Log.d("FOLLOWER_FRAGMENT", "친구 목록 조회에 성공하였습니다. message=${body.message}")
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 15) 틈 요청 읽음 처리
    suspend fun readTeumRequest(responseId: Int): Result<Int> = runCatching {
        val response = api.readTeumRequest(responseId)
        Log.d("ReadTeumRequest", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 16) 맞팔로우 목록 조회 (특정 유저 제외 가능)
    suspend fun getMutualFriends(
        excludeUserId: Int? = null,
        page: Int = 1,
        size: Int = 50
    ): Result<List<MutualFriendItem>> = runCatching {
        val response = api.getMutualFriends(page, size, excludeUserId)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.content ?: emptyList()
        } else {
            val errText = response.errorBody()?.string()
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: errText ?: response.message()}")
        }
    }

    // 17) 가능한 시간 조회
    suspend fun getPossibleTime(request: PossibleTimeRequest): Result<PossibleTimeResult> = runCatching {
        val response = api.getPossibleTime(request)
        Log.d("GetPossibleTime", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 18) 틈 요청 재전송
    suspend fun resendTeumRequest(
        parentRequestId: Int,
        request: ResendTeumRequest
    ): Result<ResendTeumResult> = runCatching {
        val response = api.resendTeumRequest(parentRequestId, request)
        Log.d("ResendTeumRequest", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 19) 친구 빈틈 시간 조회
    suspend fun getFriendTeumTime(userId: Int): Result<TeumTimeResult> = runCatching {
        val response = api.getFriendTeumTime(userId)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 20) 서로의 빈틈을 함께한 시간 조회
    suspend fun getSharedTeumTime(userId: Int): Result<TeumTimeResult> = runCatching {
        val response = api.getSharedTeumTime(userId)
        val body = response.body()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
        }
        if (body == null) throw Exception("응답 본문이 비어있습니다.")

        if (body.isSuccess && body.code == "TEUM2012" && body.result != null) {
            body.result
        } else {
            val msg = when (body.code) {
                "TEUM4002" -> "자기 자신은 조회할 수 없습니다."
                "USER4040" -> "존재하지 않는 사용자입니다."
                else -> body.message
            }
            throw Exception(msg)
        }
    }

    // 21) 함께한 틈 목록 조회
    suspend fun getSharedTeumList(
        userId: Int,
        page: Int = 1,
        size: Int = 10
    ): Result<List<SharedTeumItem>> = runCatching {
        val response = api.getSharedTeumList(userId, page, size)
        val body = response.body()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
        }
        if (body == null) throw Exception("응답 본문이 비어있습니다.")

        if (body.isSuccess && body.code == "TEUM2013") {
            body.result?.content ?: emptyList()
        } else {
            val msg = when (body.code) {
                "USER4040" -> "존재하지 않는 사용자입니다."
                "TEUM4002" -> "자기 자신은 조회할 수 없습니다."
                else -> body.message
            }
            throw Exception("${body.code} - $msg")
        }
    }

    // 22) 최근 공개 투두 조회 (최신 등록순 2개)
    suspend fun getRecentPublicTodos(userId: Int): Result<List<PublicTodoResult>> = runCatching {
        val response = api.getRecentPublicTodos(userId)
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

        // 결과 2개로 제한
        val trimmed = body.result?.take(2) ?: emptyList()

        // 성공/실패 분기 (로그 한 줄만)
        if (body.isSuccess && body.code == "FRIEND2005") {
            Log.d("RECENT_PUBLIC_TODO", body.message)
            trimmed
        } else {
            val msg = when (body.code) {
                "FRIEND4040" -> "존재하지 않는 유저입니다."
                "FRIEND4002" -> "자기 자신은 조회할 수 없습니다."
                else -> body.message
            }
            Log.d("RECENT_PUBLIC_TODO", msg)
            throw Exception("${body.code} - $msg")
        }
    }

    // 공개 투두 날짜 리스트 조회
    suspend fun getFriendPublicTodoDates(userId: Int, month: String): Result<List<String>> = runCatching {
        val response = api.getFriendPublicTodoCalendar(userId, month)
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw Exception("HTTP ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
        }

        Log.d("PUBLIC_TODO_CALENDAR",
            "isSuccess=${body.isSuccess}, code=${body.code}, message=${body.message}"
        )

        if (body.isSuccess && body.code == "FRIEND2005") {
            body.result ?: emptyList()
        } else {
            val msg = when (body.code) {
                "FRIEND4002" -> "자기 자신은 조회할 수 없습니다."
                "FRIEND4040" -> "존재하지 않는 유저입니다."
                else -> body.message
            }
            throw Exception("${body.code} - $msg")
        }
    }

    // 특정 날짜 공개 투두 조회
    suspend fun getFriendPublicTodosByDate(
        userId: Int,
        date: String
    ): Result<List<PublicTodoResult>> = runCatching {
        val response = api.getFriendPublicTodosByDate(userId, date)
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw Exception("HTTP ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
        }

        Log.d(
            "PUBLIC_TODO_DAY",
            "isSuccess=${body.isSuccess}, code=${body.code}, message=${body.message}"
        )

        if (body.isSuccess && body.code == "FRIEND2005") {
            body.result ?: emptyList()
        } else {
            val msg = when (body.code) {
                "FRIEND4002" -> "자기 자신에 대한 요청은 처리할 수 없습니다."
                "FRIEND4040" -> "존재하지 않는 유저입니다."
                else -> body.message
            }
            throw Exception("${body.code} - $msg")
        }
    }

    // 틈 요청 날짜 리스트 조회
    suspend fun getTeumRequestCalendar(month: String): Result<List<String>> = runCatching {
        val response = api.getTeumRequestCalendar(month)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    // 특정 날짜의 틈 요청 조회
    suspend fun getTeumRequestsByDate(date: String): Result<List<TeumRequestDateResult>> = runCatching {
        val response = api.getTeumRequestsByDate(date)
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw Exception("HTTP ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
        }

        if (body.isSuccess && body.result != null) {
            body.result
        } else {
            throw Exception("${body.code} - ${body.message}")
        }
    }
}
