package com.example.teumteum.data.remote.wish.repository

import android.util.Log
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.WishCategories
import com.example.teumteum.data.remote.wish.model.WishResult
import com.example.teumteum.data.remote.wish.model.WishlistResult
import com.example.teumteum.data.remote.wish.service.WishService
import com.example.teumteum.utils.handleApiResponse
import java.io.IOException
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
        handleApiResponse(response)
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
        handleApiResponse(response)
    }

    // 위시 삭제 (리스트 형태)
    suspend fun deleteWish(request: DeleteWishesRequest): Result<Unit> = runCatching {
        val response = wishService.deleteWishes(request)
        Log.d("DeleteWish", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 위시리스트 조회
    suspend fun getWishlist(duration: String, page: Int): Result<WishlistResult> = runCatching {
        val response = wishService.getWishlist(duration, page)
        Log.d("GetWishlist", "response = ${response.body()}")
        handleApiResponse(response)
    }
}