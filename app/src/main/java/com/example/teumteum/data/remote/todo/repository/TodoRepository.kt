package com.example.teumteum.data.remote.todo.repository

import android.util.Log
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.model.TodoResult
import com.example.teumteum.data.remote.todo.service.TodoService
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoService: TodoService
) {

    // 투두 등록
    suspend fun registerTodo(request: RegisterTodoRequest): Result<TodoResult> {
        return try {
            val response = todoService.registerTodo(request)
            Log.d("TodoRegister", "response = ${response.body()}")

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