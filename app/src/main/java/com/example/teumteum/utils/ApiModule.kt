package com.example.teumteum.utils

import com.example.teumteum.data.remote.activity.service.ActivityService
import com.example.teumteum.data.remote.alarm.service.FcmService
import com.example.teumteum.data.remote.alarm.service.NotificationService
import com.example.teumteum.data.remote.calendar.service.CalendarService
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.data.remote.mypage.service.MyPageService
import com.example.teumteum.data.remote.home.service.HomeService
import com.example.teumteum.data.remote.login.service.AuthService
import com.example.teumteum.data.remote.onboarding.service.OnBoardingService
import com.example.teumteum.data.remote.wish.service.WishService
import com.example.teumteum.data.remote.todo.service.TodoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    // 인증이 필요한 API들 - @AuthRetrofit 사용
    @Provides
    @Singleton
    fun provideTodoApi(@AuthRetrofit retrofit: Retrofit): TodoService {
        return retrofit.create(TodoService::class.java)
    }

    @Provides
    @Singleton
    fun provideWishApi(@AuthRetrofit retrofit: Retrofit): WishService {
        return retrofit.create(WishService::class.java)
    }

    @Provides
    @Singleton
    fun provideCalendarApi(@AuthRetrofit retrofit: Retrofit): CalendarService {
        return retrofit.create(CalendarService::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendApi(@AuthRetrofit retrofit: Retrofit): FriendService {
        return retrofit.create(FriendService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(@AuthRetrofit retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
    }

    @Provides
    @Singleton
    fun provideOnBoardingApi(@AuthRetrofit retrofit: Retrofit): OnBoardingService {
        return retrofit.create(OnBoardingService::class.java)
    }

    @Provides
    @Singleton
    fun provideMyPageApi(@AuthRetrofit retrofit: Retrofit): MyPageService {
        return retrofit.create(MyPageService::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityApi(@AuthRetrofit retrofit: Retrofit): ActivityService {
        return retrofit.create(ActivityService::class.java)
    }

    @Provides
    @Singleton
    fun provideFcmApi(@AuthRetrofit retrofit: Retrofit): FcmService {
        return retrofit.create(FcmService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(@AuthRetrofit retrofit: Retrofit): NotificationService {
        return retrofit.create(NotificationService::class.java)
    }

    // 인증이 필요 없는 API - @NoAuthRetrofit 사용
    @Provides
    @Singleton
    fun provideLoginApi(@NoAuthRetrofit retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }
}