package com.umc.teumteum.data.remote.mypage.repository

import android.util.Log
import com.umc.teumteum.data.remote.mypage.model.MyInfoResponse
import com.umc.teumteum.data.remote.mypage.model.MyRoutineRequest
import com.umc.teumteum.data.remote.mypage.model.MyRoutineResponse
import com.umc.teumteum.data.remote.mypage.model.MySocialInfoResponse
import com.umc.teumteum.data.remote.mypage.model.ProfileUpdateRequest
import com.umc.teumteum.data.remote.mypage.model.PublicTodoResponse
import com.umc.teumteum.data.remote.mypage.service.MyPageService
import com.umc.teumteum.data.remote.onboarding.model.PresignedRequest
import com.umc.teumteum.data.remote.onboarding.model.PresignedResponse
import com.umc.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.umc.teumteum.data.remote.onboarding.model.Week
import com.umc.teumteum.utils.handleApiResponse
import com.umc.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService
) {
    //마이페이지 조회
    suspend fun getMyInfo(): Result<MyInfoResponse> = runCatching {
        val response = myPageService.getMyInfo()
        Log.d("MyInfo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //프리사인드 url 발급 요청
    suspend fun requestPresignedUrl(request: PresignedRequest): Result<PresignedResponse> = runCatching {
        val response = myPageService.requestPresignedUrl(request)
        Log.d("PresignedUrl", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //이미지 등록
    suspend fun postProfileImage(request: ProfileImageRequest): Result<Unit> = runCatching {
        val response = myPageService.postProfileImage(request)
        Log.d("ProfileImage", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    suspend fun deleteProfileImage(): Result<Unit> = runCatching {
        val response = myPageService.deleteProfileImage()
        Log.d("ProfileImageDelete", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //프로필 수정
    suspend fun updateProfile(request: ProfileUpdateRequest): Result<Unit> = runCatching {
        val response = myPageService.updateProfile(request)
        Log.d("ProfileUpdate", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //반복일정 조회
    suspend fun getMyRoutine(weekday: Week): Result<List<MyRoutineResponse>> = runCatching {
        val response = myPageService.getMyRoutine(weekday.name)
        Log.d("Routine", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //반복일정 추가
    suspend fun addMyRoutine(request: MyRoutineRequest): Result<Unit> = runCatching {
        val response = myPageService.addMyRoutine(request)
        Log.d("Routine", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //반복일정 수정
    suspend fun modifyMyRoutine(routineId: Long, request: MyRoutineRequest): Result<Unit> = runCatching {
        val response = myPageService.modifyMyRoutine(routineId, request)
        Log.d("Routine", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //반복일정 삭제
    suspend fun deleteMyRoutine(routineId: Long): Result<Unit> = runCatching {
        val response = myPageService.deleteMyRoutine(routineId)
        Log.d("Routine", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //소셜 정보 조회
    suspend fun getMySocialInfo(): Result<MySocialInfoResponse> = runCatching {
        val response = myPageService.getMySocialInfo()
        Log.d("MySocialInfo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //회원탈퇴
    suspend fun deleteUser(): Result<Unit> = runCatching {
        val response = myPageService.deleteUser()
        Log.d("User", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //나의 공개 투두 조회
    suspend fun getMyPublicTodos(): Result<List<PublicTodoResponse>> = runCatching {
        val response = myPageService.getMyPublicTodos()
        Log.d("User", "response = ${response.body()}")
        handleApiResponse(response)
    }
}