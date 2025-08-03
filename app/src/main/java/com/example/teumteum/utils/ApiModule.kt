package com.example.teumteum.utils

import com.example.teumteum.data.remote.agreement.AgreementRetrofitInterface
import com.example.teumteum.data.remote.alarm.AlarmRetrofitInterface
import com.example.teumteum.data.remote.calendar.CalendarRetrofitInterface
import com.example.teumteum.data.remote.friend.service.FriendService
import com.example.teumteum.data.remote.home.HomeRetrofitInterface
import com.example.teumteum.data.remote.onboarding.OnBoardingRetrofitInterface
import com.example.teumteum.data.remote.todo.TodoRetrofitInterface
import com.example.teumteum.data.remote.wish.service.WishService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideTodoApi(retrofit: Retrofit): TodoRetrofitInterface {
        return retrofit.create(TodoRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideWishApi(retrofit: Retrofit): WishService {
        return retrofit.create(WishService::class.java)
    }

    @Provides
    @Singleton
    fun provideAgreementApi(retrofit: Retrofit): AgreementRetrofitInterface {
        return retrofit.create(AgreementRetrofitInterface::class.java)
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
    fun provideOnBoardingApi(retrofit: Retrofit): OnBoardingRetrofitInterface {
        return retrofit.create(OnBoardingRetrofitInterface::class.java)
    }
}