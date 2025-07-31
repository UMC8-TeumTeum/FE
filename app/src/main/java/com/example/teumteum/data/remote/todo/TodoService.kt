package com.example.teumteum.data.remote.todo

import android.util.Log
import com.example.teumteum.data.remote.todo.dto.DeleteTodoResponse
import com.example.teumteum.data.remote.todo.dto.EditTodoRequest
import com.example.teumteum.data.remote.todo.dto.EditTodoResponse
import com.example.teumteum.data.remote.todo.dto.GetTodoListResponse
import com.example.teumteum.data.remote.todo.dto.GetTodoResponse
import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import com.example.teumteum.data.remote.wish.WishRetrofitInterface
import com.example.teumteum.data.remote.wish.WishService
import com.example.teumteum.data.remote.wish.WishService.Companion
import com.example.teumteum.data.remote.wish.dto.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.dto.DeleteWishesResponse
import com.example.teumteum.data.remote.wish.dto.EditWishRequest
import com.example.teumteum.data.remote.wish.dto.EditWishResponse
import com.example.teumteum.ui.todo.view.DeleteTodoView
import com.example.teumteum.ui.todo.view.EditTodoView
import com.example.teumteum.ui.todo.view.TodoListView
import com.example.teumteum.ui.todo.view.RegisterTodoView
import com.example.teumteum.ui.todo.view.TodoView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TodoService {
    private lateinit var todoRegisterView: RegisterTodoView
    private lateinit var todoListView: TodoListView
    private lateinit var todoView: TodoView
    private lateinit var todoEditView: EditTodoView
    private lateinit var todoDeleteView: DeleteTodoView

    fun setTodoRegisterView(todoRegisterView: RegisterTodoView) {
        this.todoRegisterView = todoRegisterView
    }

    fun setTodoListGetView(todoListView: TodoListView) {
        this.todoListView = todoListView
    }

    fun setTodoGetView(todoView: TodoView) {
        this.todoView = todoView
    }

    fun setTodoEditView(todoEditView: EditTodoView) {
        this.todoEditView = todoEditView
    }

    fun setTodoDeleteView(todoDeleteView: DeleteTodoView) {
        this.todoDeleteView = todoDeleteView
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
                            val errorResponse = gson.fromJson(errorMsg, RegisterTodoResponse::class.java)
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
                        todoListView.onGetTodoListSuccess(getTodoListResponse.code, todoList)
                    } else {
                        todoListView.onGetTodoListFailure(
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
                            todoListView.onGetTodoListFailure(errorResponse.code, errorResponse.message)
                        } else {
                            todoListView.onGetTodoListFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("TODOLIST/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoListView.onGetTodoListFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetTodoListResponse>, t: Throwable) {
                Log.d("TODOLIST/FAILURE", t.message.toString())
                todoListView.onGetTodoListFailure("NETWORK_ERROR")
            }
        })
    }

    // 특정 투두 조회
    fun getTodo(todoId: Long) {
        val todoService = getRetrofitWithToken().create(TodoRetrofitInterface::class.java)

        todoService.getTodo(todoId).enqueue(object : Callback<GetTodoResponse> {
            override fun onResponse(
                call: Call<GetTodoResponse>,
                response: Response<GetTodoResponse>
            ) {
                Log.d("TODO/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getTodoResponse = response.body()

                    if (getTodoResponse != null && getTodoResponse.code == "HOOM2003") {
                        val todo = getTodoResponse.result
                        if (todo != null) {
                            todoView.onGetTodoSuccess(todo)
                        }
                    } else {
                        todoView.onGetTodoFailure(
                            getTodoResponse?.code ?: "UNKNOWN",
                            getTodoResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("TODO/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, GetTodoResponse::class.java)
                            todoView.onGetTodoFailure(errorResponse.code, errorResponse.message)
                        } else {
                            todoView.onGetTodoFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("TODO/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoView.onGetTodoFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetTodoResponse>, t: Throwable) {
                Log.d("TODO/FAILURE", t.message.toString())
                todoView.onGetTodoFailure("NETWORK_ERROR")
            }
        })
    }

    // 투두 수정
    fun editTodo(todoId: Long, request: EditTodoRequest) {

        val todoService = getRetrofitWithToken().create(TodoRetrofitInterface::class.java)

        todoService.editTodo(todoId, request).enqueue(object : Callback<EditTodoResponse> {
            override fun onResponse(
                call: Call<EditTodoResponse>,
                response: Response<EditTodoResponse>
            ) {
                Log.d("EDIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val editResponse = response.body()

                    if (editResponse != null && editResponse.code == "HOME2002") {
                        todoEditView.onEditTodoSuccess(editResponse.code)
                    } else {
                        todoEditView.onEditTodoFailure(editResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("EDIT/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, EditTodoResponse::class.java)
                            todoEditView.onEditTodoFailure(errorResponse.code)
                        } else {
                            todoEditView.onEditTodoFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("EDIT/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoEditView.onEditTodoFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<EditTodoResponse>, t: Throwable) {
                Log.d("EDIT/FAILURE", t.message.toString())
                todoEditView.onEditTodoFailure("NETWORK_ERROR")
            }
        })
    }

    // 투두 삭제
    fun deleteTodo(todoId: Long) {

        val todoService = getRetrofitWithToken().create(TodoRetrofitInterface::class.java)

        todoService.deleteTodo(todoId).enqueue(object : Callback<DeleteTodoResponse> {
            override fun onResponse(
                call: Call<DeleteTodoResponse>,
                response: Response<DeleteTodoResponse>
            ) {
                Log.d("DELETE/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val deleteResponse = response.body()

                    if (deleteResponse != null && deleteResponse.code == "HOME2004") {
                        todoDeleteView.onDeleteTodoSuccess(deleteResponse.code)
                    } else {
                        todoDeleteView.onDeleteTodoFailure(deleteResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("DELETE/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, DeleteTodoResponse::class.java)
                            todoDeleteView.onDeleteTodoFailure(errorResponse.code)
                        } else {
                            todoDeleteView.onDeleteTodoFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("DELETE/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        todoDeleteView.onDeleteTodoFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<DeleteTodoResponse>, t: Throwable) {
                Log.d("DELETE/FAILURE", t.message.toString())
                todoDeleteView.onDeleteTodoFailure("NETWORK_ERROR")
            }
        })

    }

}