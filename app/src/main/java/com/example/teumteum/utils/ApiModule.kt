package com.example.teumteum.utils

import com.example.teumteum.data.remote.activity.service.ActivityService
import com.example.teumteum.data.remote.alarm.AlarmRetrofitInterface
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
    fun provideCalendarApi(retrofit: Retrofit): CalendarService {
        return retrofit.create(CalendarService::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendApi(retrofit: Retrofit): FriendService {
        return retrofit.create(FriendService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
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

    @Provides
    @Singleton
    fun provideActivityApi(retrofit: Retrofit): ActivityService {
        return retrofit.create(ActivityService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }
}