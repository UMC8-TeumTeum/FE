package com.example.teumteum.data.remote

import android.util.Log
import com.example.teumteum.ui.wish.WishView
import com.example.teumteum.utils.getRetrofit
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishService {
    private lateinit var wishView: WishView

    fun setWishView(wishView: WishView){
        this.wishView = wishView;
    }

    fun wishRegister(request: WishRegisterRequest){

        val wishService = getRetrofitWithToken().create(WishRetrofitInterface::class.java)

        wishService.wishRegister(request).enqueue(object: Callback<WishRegisterResponse>{
            override fun onResponse(
                call: Call<WishRegisterResponse>,
                response: Response<WishRegisterResponse>
            ) {
                Log.d("REGISTER/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse != null && registerResponse.code == "HOME2005") {
                        wishView.onRegisterSuccess(registerResponse.code)
                    } else {
                        wishView.onRegisterFailure(registerResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("REGISTER/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, WishRegisterResponse::class.java)
                            wishView.onRegisterFailure(errorResponse.code)
                        } else {
                            wishView.onRegisterFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("REGISTER/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        wishView.onRegisterFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<WishRegisterResponse>, t: Throwable) {
                Log.d("REGISTER/FAILURE", t.message.toString())
                wishView.onRegisterFailure("NETWORK_ERROR")
            }
        })
    }

    companion object {
        private val gson = Gson()
    }
}