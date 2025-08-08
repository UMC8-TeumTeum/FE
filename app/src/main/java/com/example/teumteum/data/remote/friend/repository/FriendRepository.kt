package com.example.teumteum.data.remote.friend.repository

import android.util.Log
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.utils.handleApiResponse
import javax.inject.Inject

class FriendRepository @Inject constructor(
    private val api: FriendService
) {
    suspend fun searchUser(keyword: String): Result<List<FriendSearchResult>> = runCatching {
        val response = api.searchUserByNickname(keyword)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: emptyList()
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    suspend fun getFriendProfile(userId: Int): Result<FriendProfileResult> = runCatching {
        val response = api.getFriendProfile(userId)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result ?: throw Exception("프로필 결과 없음")
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    suspend fun getReceivedTeums(): Result<List<TeumReceivedItem>> = runCatching {
        val response = api.getReceivedTeumRequests()
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.content ?: emptyList()  //  수정됨
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }


    suspend fun sendTeumRequest(request: TeumRequest): Result<Int> = runCatching {
        val response = api.sendTeumRequest(request)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true) {
            body.result?.id ?: throw Exception("teumId 없음")
        } else {
            throw Exception("${body?.code ?: "HTTP ${response.code()}"} - ${body?.message ?: response.message()}")
        }
    }

    suspend fun respondToTeum(responseId: Int, status: String): Result<TeumStatusResult> = runCatching {
        val request = TeumStatusRequest(status)
        val response = api.patchTeumStatus(responseId, request)
        val body = response.body()
        if (response.isSuccessful && body?.isSuccess == true && body.result != null) {
            body.result
        } else {
            throw Exception("${body?.code ?: "HTTP${response.code()}"} - ${body?.message ?: "오류"}")
        }
    }

    suspend fun readTeumRequest(responseId: Int): Result<Int> = runCatching {
        val response = api.readTeumRequest(responseId)
        Log.d("ReadTeumRequest", "response = ${response.body()}")
        handleApiResponse(response)
    }

}
