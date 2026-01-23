package com.umc.teumteum.application

import android.app.Application
import com.umc.teumteum.BuildConfig
import com.umc.teumteum.R
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NidOAuth
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

        NidOAuth.initialize(
            this,
            BuildConfig.NAVER_CLIENT_ID,
            BuildConfig.NAVER_CLIENT_SECRET,
            getString(R.string.app_name)
        )

    }
}