package com.example.teumteum.data.remote.onboarding

import android.util.Log
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.dto.PresignedRequest
import com.example.teumteum.data.remote.onboarding.dto.PresignedResponse
import com.example.teumteum.data.remote.onboarding.dto.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.dto.ProfileImageResponse
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternResponse
import com.example.teumteum.data.remote.onboarding.dto.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.dto.ScheduleResponse
import com.example.teumteum.data.remote.wish.WishRetrofitInterface
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import com.example.teumteum.ui.signup.view.NicknameJobFieldView
import com.example.teumteum.ui.signup.view.ProfileImageView
import com.example.teumteum.ui.signup.view.SleepPatternView
import com.example.teumteum.ui.signup.view.ScheduleView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class OnBoardingService {

    private lateinit var nicknameJobFieldView: NicknameJobFieldView
    private lateinit var sleepPatternView: SleepPatternView
    private lateinit var scheduleView: ScheduleView
    private lateinit var profileImageView: ProfileImageView

    fun setNicknameJobFieldView(nicknameJobFieldView: NicknameJobFieldView) {
        this.nicknameJobFieldView = nicknameJobFieldView
    }

    fun setSleepPatternView(sleepPatternView: SleepPatternView){
        this.sleepPatternView = sleepPatternView
    }

    fun setScheduleView(scheduleView: ScheduleView){
        this.scheduleView = scheduleView
    }

    fun setProfileImageView(profileImageView: ProfileImageView){
        this.profileImageView = profileImageView
    }

    //온보딩 : 닉네임, 직종 입력
    fun postNicknameAndJobField(request: NicknameJobRequest) {
        val nicknameJobApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = nicknameJobApi.postNicknameAndJobField(request)

        call.enqueue(object : Callback<NicknameJobResponse> {
            override fun onResponse(
                call: Call<NicknameJobResponse>,
                response: Response<NicknameJobResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2002") {
                        nicknameJobFieldView.onNicknameJobSuccess(body.code)
                    } else {
                        nicknameJobFieldView.onNicknameJobFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    nicknameJobFieldView.onNicknameJobFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<NicknameJobResponse>, t: Throwable) {
                nicknameJobFieldView.onNicknameJobFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })
    }

    //수면패턴 등록
    fun postSleepPattern(request: SleepPatternRequest) {
        val sleepPatternApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = sleepPatternApi.postSleepPattern(request)
        Log.d("SLEEP_PATTERN_REQUEST", request.toString())


        call.enqueue(object : Callback<SleepPatternResponse> {
            override fun onResponse(
                call: Call<SleepPatternResponse>,
                response: Response<SleepPatternResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2005") {
                        sleepPatternView.onSleepPatternSuccess(body.code)
                    } else {
                        sleepPatternView.onSleepPatternFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    sleepPatternView.onSleepPatternFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<SleepPatternResponse>, t: Throwable) {
                sleepPatternView.onSleepPatternFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })
    }

    //반복일정 등록
    fun postSchedules(request: ScheduleRequest) {
        val scheduleApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = scheduleApi.postSchedules(request)

        Log.d("SCHEDULE_REQUEST", request.toString())

        call.enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(
                call: Call<ScheduleResponse>,
                response: Response<ScheduleResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2006") {
                        scheduleView.onScheduleSuccess(body.code)
                    } else {
                        scheduleView.onScheduleFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    scheduleView.onScheduleFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                scheduleView.onScheduleFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })

    }

    //PresignedUrl 발급
    fun requestPresignedUrl(request: PresignedRequest) {
        val presignedApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = presignedApi.requestPresignedUrl(request)

        Log.d("PRESIGNED_REQUEST", request.toString())

        call.enqueue(object : Callback<PresignedResponse> {
            override fun onResponse(
                call: Call<PresignedResponse>,
                response: Response<PresignedResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2003") {
                        profileImageView.onPresignedSuccess(body.code, body.result)
                    } else {
                        profileImageView.onPresignedFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    profileImageView.onPresignedFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<PresignedResponse>, t: Throwable) {
                profileImageView.onPresignedFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })

    }

    //이미지 등록
    fun postProfileImage(request: ProfileImageRequest) {
        val profileImageApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = profileImageApi.postProfileImage(request)

        Log.d("PROFILE_IMAGE_REQUEST", request.toString())

        call.enqueue(object : Callback<ProfileImageResponse> {
            override fun onResponse(
                call: Call<ProfileImageResponse>,
                response: Response<ProfileImageResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2004") {
                        profileImageView.onProfileImageSuccess(body.code)
                    } else {
                        profileImageView.onProfileImageFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    profileImageView.onProfileImageFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ProfileImageResponse>, t: Throwable) {
                profileImageView.onProfileImageFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })

    }
}