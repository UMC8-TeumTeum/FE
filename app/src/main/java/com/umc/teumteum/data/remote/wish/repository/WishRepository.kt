package com.umc.teumteum.data.remote.wish.repository

import android.util.Log
import com.umc.teumteum.data.remote.activity.model.AssignWishRequest
import com.umc.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.umc.teumteum.data.remote.wish.model.EditWishRequest
import com.umc.teumteum.data.remote.wish.model.RegisterWishRequest
import com.umc.teumteum.data.remote.wish.model.WishResult
import com.umc.teumteum.data.remote.wish.model.WishlistResult
import com.umc.teumteum.data.remote.wish.service.WishService
import com.umc.teumteum.utils.handleApiResponse
import com.umc.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishRepository @Inject constructor(
    private val wishService: WishService
) {
    // 위시 등록
    suspend fun registerWish(request: RegisterWishRequest): Result<Unit> = runCatching {
        val response = wishService.registerWish(request)
        Log.d("RegisterWish", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 특정 위시 조회
    suspend fun getWish(wishId: Long): Result<WishResult> = runCatching {
        val response = wishService.getWish(wishId)
        Log.d("GetWish", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 특정 위시 편집
    suspend fun editWish(wishId: Long, request: EditWishRequest): Result<Unit> = runCatching {
        val response = wishService.editWish(wishId, request)
        Log.d("EditWish", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 위시 삭제 (리스트 형태)
    suspend fun deleteWish(request: DeleteWishesRequest): Result<Unit> = runCatching {
        val response = wishService.deleteWishes(request)
        Log.d("DeleteWish", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 위시리스트 조회
    suspend fun getWishlist(duration: String, page: Int): Result<WishlistResult> = runCatching {
        val response = wishService.getWishlist(duration, page)
        Log.d("GetWishlist", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 위시 빈틈 채우기
    suspend fun assignWish(wishId: Long, request: AssignWishRequest): Result<Unit> = runCatching {
        val response = wishService.assignWish(wishId, request)
        Log.d("AssignWish", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }
}