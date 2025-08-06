package com.example.teumteum.utils

import com.example.teumteum.data.remote.alarm.AlarmRetrofitInterface
import com.example.teumteum.data.remote.calendar.CalendarRetrofitInterface
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.data.remote.home.HomeRetrofitInterface
import com.example.teumteum.data.remote.mypage.service.MyPageService
import com.example.teumteum.data.remote.onboarding.service.OnBoardingService
import com.example.teumteum.data.remote.wish.service.WishService
import com.example.teumteum.data.remote.todo.service.TodoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideTodoApi(retrofit: Retrofit): TodoService {
        return retrofit.create(TodoService::class.java)
    }

    @Provides
    @Singleton
    fun provideWishApi(retrofit: Retrofit): WishService {
        return retrofit.create(WishService::class.java)
    }

    @Provides
    @Singleton
    fun provideAlarmApi(retrofit: Retrofit): AlarmRetrofitInterface {
        return retrofit.create(AlarmRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideCalendarApi(retrofit: Retrofit): CalendarRetrofitInterface {
        return retrofit.create(CalendarRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendApi(retrofit: Retrofit): FriendService {
        return retrofit.create(FriendService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeRetrofitInterface {
        return retrofit.create(HomeRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideOnBoardingApi(retrofit: Retrofit): OnBoardingService {
        return retrofit.create(OnBoardingService::class.java)
    }

    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageService {
        return retrofit.create(MyPageService::class.java)
    }
}