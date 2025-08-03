package com.example.teumteum.data.remote.wish.repository

import android.util.Log
import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.DeleteWishesResponse
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.EditWishResponse
import com.example.teumteum.data.remote.wish.model.GetWishResponse
import com.example.teumteum.data.remote.wish.model.GetWishlistResponse
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishResponse
import com.example.teumteum.data.remote.wish.model.WishlistResult
import com.example.teumteum.data.remote.wish.service.WishService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishRepository @Inject constructor(
    private val wishService: WishService
) {
    // 위시 등록
    suspend fun registerWish(request: RegisterWishRequest): Result<Unit> {
        return try {
            val response = wishService.registerWish(request)
            Log.d("WishRegister", "response = ${response.body()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                return if (apiResponse.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }

    // 특정 위시 조회
    suspend fun getWish(wishId: Long): Result<Wish> {
        return try {
            val response = wishService.getWish(wishId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess && apiResponse.result != null) {
                    Result.success(apiResponse.result)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }

    // 특정 위시 편집
    suspend fun editWish(wishId: Long, request: EditWishRequest): Result<Unit> {
        return try {
            val response = wishService.editWish(wishId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }

    // 특정 위시 삭제
    suspend fun deleteWish(request: DeleteWishesRequest): Result<Unit> {
        return try {
            val response = wishService.deleteWishes(request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }

    // 위시리스트 조회
    suspend fun getWishlist(duration: String, page: Int): Result<WishlistResult> {
        return try {
            val response = wishService.getWishlist(duration, page)
            Log.d("WishlistGet", "response = ${response.body()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess && apiResponse.result != null) {
                    Result.success(apiResponse.result)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }

}