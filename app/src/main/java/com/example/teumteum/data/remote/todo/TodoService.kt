package com.example.teumteum.data.remote.todo

import android.util.Log
import com.example.teumteum.data.remote.todo.dto.GetTodoListResponse
import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import com.example.teumteum.data.remote.wish.WishRetrofitInterface
import com.example.teumteum.data.remote.wish.WishService
import com.example.teumteum.data.remote.wish.WishService.Companion
import com.example.teumteum.data.remote.wish.dto.GetWishlistResponse
import com.example.teumteum.ui.todo.view.GetTodoListView
import com.example.teumteum.ui.todo.view.RegisterTodoView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TodoService {
    private lateinit var todoRegisterView: RegisterTodoView
    private lateinit var todoListGetView: GetTodoListView

    fun setTodoRegisterView(todoRegisterView: RegisterTodoView) {
        this.todoRegisterView = todoRegisterView
    }

    fun setTodoListGetView(todoListGetView: GetTodoListView) {
        this.todoListGetView = todoListGetView
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
                        val todoId = registerResponse.result?.todoId
                        Log.d("REGISTER/TODO_ID", "등록된 투두 ID: $todoId")
                        todoRegisterView.onRegisterTodoSuccess(registerResponse.code, todoId)
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

    // 투두리스트 조회
    fun getTodoList(date: String) {
        val todoService = getRetrofitWithToken().create(TodoRetrofitInterface::class.java)

        todoService.getTodoList(date).enqueue(object : Callback<GetTodoListResponse> {
            override fun onResponse(
                call: Call<GetTodoListResponse>,
                response: Response<GetTodoListResponse>
            ) {
                Log.d("TODOLIST/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getTodoListResponse = response.body()

                    if (getTodoListResponse != null && getTodoListResponse.isSuccess) {
                        val todoList = response.body()?.result?.todoList ?: emptyList()
                        todoListGetView.onGetTodoListSuccess(getTodoListResponse.code, todoList)
                    } else {
                        todoListGetView.onGetTodoListFailure(
                            getTodoListResponse?.code ?: "UNKNOWN",
                            getTodoListResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("TODOLIST/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, GetTodoListResponse::class.java)
                            todoListGetView.onGetTodoListFailure(errorResponse.code, errorResponse.message)
                        } else {
                            todoListGetView.onGetTodoListFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("TODOLIST/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoListGetView.onGetTodoListFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetTodoListResponse>, t: Throwable) {
                Log.d("TODOLIST/FAILURE", t.message.toString())
                todoListGetView.onGetTodoListFailure("NETWORK_ERROR")
            }
        })
    }

}