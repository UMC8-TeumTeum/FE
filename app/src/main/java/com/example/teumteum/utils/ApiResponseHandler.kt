package com.example.teumteum.utils

import android.util.Log
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

inline fun <reified T> handleApiResponse(response: Response<ApiResponse<T>>): T {
    val body = response.body()

    if (response.isSuccessful && body?.isSuccess == true) {
        return body.result ?: throw ApiException("EMPTY_RESULT", "결과가 없습니다.")
    } else {
        val error = parseErrorBody(response)
        val code = error?.code ?: response.code().toString()
        val message = error?.message ?: "알 수 없는 오류"

        Log.e("ApiResponse", "API 실패: code=$code, message=$message")
        throw ApiException(code, message)
    }
}

inline fun handleApiResponseUnit(response: Response<ApiResponse<Unit>>) {
    val body = response.body()

    if (!(response.isSuccessful && body?.isSuccess == true)) {
        val error = parseErrorBody(response)
        val code = error?.code ?: response.code().toString()
        val message = error?.message ?: "알 수 없는 오류"

        Log.e("ApiResponseUnit", "API 실패: code=$code, message=$message")
        throw ApiException(code, message)
    }
}

fun parseErrorBody(response: Response<*>): ApiResponse<*>? {
    return try {
        val errorBody = response.errorBody()?.string()
        errorBody?.let {
            Gson().fromJson(it, ApiResponse::class.java)
        }
    } catch (e: IOException) {
        null
    }
}
