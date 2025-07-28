package com.example.teumteum.data.remote.todo

import android.util.Log
import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import com.example.teumteum.ui.todo.view.RegisterTodoView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TodoService {
    private lateinit var todoRegisterView: RegisterTodoView

    fun setTodoRegisterView(todoRegisterView: RegisterTodoView) {
        this.todoRegisterView = todoRegisterView
    }

    companion object {
        private val gson = Gson()
    }

    // 투두 등록
    fun registerTodo(request: RegisterTodoRequest) {

        val todoService = getRetrofitWithToken().create(TodoRetrofitInterface::class.java)

        todoService.registerTodo(request).enqueue(object : Callback<RegisterTodoResponse> {
            override fun onResponse(
                call: Call<RegisterTodoResponse>,
                response: Response<RegisterTodoResponse>
            ) {
                Log.d("REGISTER/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse != null && registerResponse.code == "HOME2001") {
                        todoRegisterView.onRegisterTodoSuccess(registerResponse.code)
                    } else {
                        todoRegisterView.onRegisterTodoFailure(registerResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("REGISTER/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse =
                                TodoService.gson.fromJson(errorMsg, RegisterTodoResponse::class.java)
                            todoRegisterView.onRegisterTodoFailure(errorResponse.code)
                        } else {
                            todoRegisterView.onRegisterTodoFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("REGISTER/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoRegisterView.onRegisterTodoFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<RegisterTodoResponse>, t: Throwable) {
                Log.d("REGISTER/FAILURE", t.message.toString())
                todoRegisterView.onRegisterTodoFailure("NETWORK_ERROR")
            }
        })
    }

}