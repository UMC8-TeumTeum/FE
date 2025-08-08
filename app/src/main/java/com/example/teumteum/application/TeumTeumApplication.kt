package com.example.teumteum.application

import android.app.Application
import com.example.teumteum.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TeumTeumApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //Kakao SDK 초기화
        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.NATIVE_APP_KEY
        )
    }
}