package com.example.teumteum.data.remote

import android.util.Log
import com.example.teumteum.ui.wish.RegisterWishView
import com.example.teumteum.ui.wish.WishlistView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishService {
    private lateinit var wishRegisterView: RegisterWishView
    private lateinit var wishlistView: WishlistView

    fun setWishRegisterView(wishRegisterView: RegisterWishView) {
        this.wishRegisterView = wishRegisterView;
    }

    fun setWishlistGetView(wishlistView: WishlistView) {
        this.wishlistView = wishlistView;
    }

    companion object {
        private val gson = Gson()
    }

    fun registerWish(request: RegisterWishRequest) {

        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.wishRegister(request).enqueue(object : Callback<RegisterWishResponse> {
            override fun onResponse(
                call: Call<RegisterWishResponse>,
                response: Response<RegisterWishResponse>
            ) {
                Log.d("REGISTER/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse != null && registerResponse.code == "HOME2005") {
                        wishRegisterView.onRegisterSuccess(registerResponse.code)
                    } else {
                        wishRegisterView.onRegisterFailure(registerResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("REGISTER/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse =
                                gson.fromJson(errorMsg, RegisterWishResponse::class.java)
                            wishRegisterView.onRegisterFailure(errorResponse.code)
                        } else {
                            wishRegisterView.onRegisterFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("REGISTER/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishRegisterView.onRegisterFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<RegisterWishResponse>, t: Throwable) {
                Log.d("REGISTER/FAILURE", t.message.toString())
                wishRegisterView.onRegisterFailure("NETWORK_ERROR")
            }
        })
    }

    fun getWishlist(duration: String, page: Int) {
        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.getWishlist(duration, page).enqueue(object : Callback<GetWishlistResponse> {
            override fun onResponse(
                call: Call<GetWishlistResponse>,
                response: Response<GetWishlistResponse>
            ) {
                Log.d("WISHLIST/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getWishlistResponse = response.body()

                    if (getWishlistResponse != null && getWishlistResponse.isSuccess) {
                        val wishlist = response.body()?.result?.wishlist ?: emptyList()
                        wishlistView.onGetWishListSuccess(wishlist)
                    } else {
                        wishlistView.onGetWishListFailure(
                            getWishlistResponse?.code ?: "UNKNOWN",
                            getWishlistResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("WISHLIST/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, GetWishlistResponse::class.java)
                            val errorMessage = errorResponse.result?.toString()
                            wishlistView.onGetWishListFailure(errorResponse.code, errorMessage)
                        } else {
                            wishlistView.onGetWishListFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("WISHLIST/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishlistView.onGetWishListFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetWishlistResponse>, t: Throwable) {
                Log.d("WISHLIST/FAILURE", t.message.toString())
                wishlistView.onGetWishListFailure("NETWORK_ERROR")
            }
        })
    }

}