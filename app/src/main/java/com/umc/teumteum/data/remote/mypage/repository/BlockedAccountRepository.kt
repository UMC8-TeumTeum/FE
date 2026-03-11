package com.umc.teumteum.data.remote.mypage.repository

import android.util.Log
import com.umc.teumteum.data.remote.mypage.model.BlockedUser
import com.umc.teumteum.data.remote.mypage.model.UnBlockResponse
import com.umc.teumteum.data.remote.mypage.service.BlockService
import com.umc.teumteum.ui.myhome.data.BlockedAccount
import com.umc.teumteum.utils.AuthRetrofit
import retrofit2.Retrofit
import javax.inject.Inject

class BlockedAccountRepository @Inject constructor(
    @AuthRetrofit private val retrofit: Retrofit
) {
    private val blockService: BlockService = retrofit.create(BlockService::class.java)

    suspend fun fetchBlockedAccounts(): List<BlockedAccount> {
        val response = blockService.getBlockedUsers()
        val http = response.code()
        val body = response.body()

        if (!response.isSuccessful) {
            Log.d("BlockedAccounts", "서버 오류 코드: ${response.code()}, 내용: ${response.errorBody()?.string()}")
            throw Exception("HTTP ${response.code()} - ${response.message()}")
        }

        if (body == null) {
            Log.d("BlockedAccounts", "응답 본문이 비어있습니다.")
            throw Exception("응답 본문이 비어있습니다.")
        }

        if (!body.isSuccess) {
            Log.d("BlockedAccounts", "차단 목록 조회 실패: ${body.message}")
            throw Exception(body.message)
        }

        val users: List<BlockedUser> = body.result?.content ?: emptyList()
        return users.map { user ->
            BlockedAccount(
                userId = user.userId.toLong(),
                nickName = user.nickname,
                job = user.job ?: "",
                profileImageUrl = user.profileImageUrl
            )
        }
    }

    // 사용자 차단 해제
    suspend fun unblockUser(userId: Long): Result<UnBlockResponse> {
        return try {
            val response = blockService.unblockUser(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("서버 오류"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
