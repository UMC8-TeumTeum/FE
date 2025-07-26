package com.example.teumteum.data.remote.wish

import android.util.Log
import com.example.teumteum.data.remote.wish.dto.EditWishRequest
import com.example.teumteum.data.remote.wish.dto.EditWishResponse
import com.example.teumteum.data.remote.wish.dto.GetWishResponse
import com.example.teumteum.data.remote.wish.dto.GetWishlistResponse
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import com.example.teumteum.ui.wish.view.EditWishView
import com.example.teumteum.ui.wish.view.RegisterWishView
import com.example.teumteum.ui.wish.view.WishView
import com.example.teumteum.ui.wish.view.WishlistView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishService {
    private lateinit var wishRegisterView: RegisterWishView
    private lateinit var wishlistView: WishlistView
    private lateinit var wishView: WishView
    private lateinit var wishEditView: EditWishView

    fun setWishRegisterView(wishRegisterView: RegisterWishView) {
        this.wishRegisterView = wishRegisterView
    }

    fun setWishlistGetView(wishlistView: WishlistView) {
        this.wishlistView = wishlistView
    }

    fun setWishGetView(wishView: WishView) {
        this.wishView = wishView
    }

    fun setWishEditView(wishEditView: EditWishView) {
        this.wishEditView = wishEditView
    }

    companion object {
        private val gson = Gson()
    }

    // 위시 등록
    fun registerWish(request: RegisterWishRequest) {

        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.registerWish(request).enqueue(object : Callback<RegisterWishResponse> {
            override fun onResponse(
                call: Call<RegisterWishResponse>,
                response: Response<RegisterWishResponse>
            ) {
                Log.d("REGISTER/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse != null && registerResponse.code == "HOME2005") {
                        wishRegisterView.onRegisterWishSuccess(registerResponse.code)
                    } else {
                        wishRegisterView.onRegisterWishFailure(registerResponse?.code ?: "UNKNOWN")
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
                            wishRegisterView.onRegisterWishFailure(errorResponse.code)
                        } else {
                            wishRegisterView.onRegisterWishFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("REGISTER/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishRegisterView.onRegisterWishFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<RegisterWishResponse>, t: Throwable) {
                Log.d("REGISTER/FAILURE", t.message.toString())
                wishRegisterView.onRegisterWishFailure("NETWORK_ERROR")
            }
        })
    }

    // 위시리스트 조회
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

    // 특정 위시 조회
    fun getWish(wishId: Long) {
        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.getWish(wishId).enqueue(object : Callback<GetWishResponse> {
            override fun onResponse(
                call: Call<GetWishResponse>,
                response: Response<GetWishResponse>
            ) {
                Log.d("WISH/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getWishResponse = response.body()

                    if (getWishResponse != null && getWishResponse.isSuccess) {
                        val wish = getWishResponse.result
                        wishView.onGetWishSuccess(wish)
                    } else {
                        wishView.onGetWishFailure(
                            getWishResponse?.code ?: "UNKNOWN",
                            getWishResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("WISH/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, GetWishResponse::class.java)
                            wishView.onGetWishFailure(errorResponse.code, errorResponse.message)
                        } else {
                            wishView.onGetWishFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("WISH/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishView.onGetWishFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetWishResponse>, t: Throwable) {
                Log.d("WISH/FAILURE", t.message.toString())
                wishView.onGetWishFailure("NETWORK_ERROR")
            }
        })
    }

    // 위시 수정
    fun editWish(wishId: Long, request: EditWishRequest) {

        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.editWish(wishId, request).enqueue(object : Callback<EditWishResponse> {
            override fun onResponse(
                call: Call<EditWishResponse>,
                response: Response<EditWishResponse>
            ) {
                Log.d("EDIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val editResponse = response.body()

                    if (editResponse != null && editResponse.code == "HOME2008") {
                        wishEditView.onEditWishSuccess(editResponse.code)
                    } else {
                        wishEditView.onEditWishFailure(editResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("EDIT/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse =
                                gson.fromJson(errorMsg, EditWishResponse::class.java)
                            wishEditView.onEditWishFailure(errorResponse.code)
                        } else {
                            wishEditView.onEditWishFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("EDIT/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishEditView.onEditWishFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<EditWishResponse>, t: Throwable) {
                Log.d("EDIT/FAILURE", t.message.toString())
                wishEditView.onEditWishFailure("NETWORK_ERROR")
            }
        })
    }

}