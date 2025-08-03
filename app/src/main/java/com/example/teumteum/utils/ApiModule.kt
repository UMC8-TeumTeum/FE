package com.example.teumteum.utils

import com.example.teumteum.data.remote.activity.service.ActivityService
import com.example.teumteum.data.remote.agreement.AgreementRetrofitInterface
import com.example.teumteum.data.remote.alarm.AlarmRetrofitInterface
import com.example.teumteum.data.remote.calendar.CalendarRetrofitInterface
import com.example.teumteum.data.remote.friend.profile.FriendProfileRetrofitInterface
import com.example.teumteum.data.remote.friend.search.FriendSearchRetrofitInterface
import com.example.teumteum.data.remote.home.HomeRetrofitInterface
import com.example.teumteum.data.remote.onboarding.OnBoardingRetrofitInterface
import com.example.teumteum.data.remote.todo.service.TodoService
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
    fun provideFriendProfileApi(retrofit: Retrofit): FriendProfileRetrofitInterface {
        return retrofit.create(FriendProfileRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendSearchApi(retrofit: Retrofit): FriendSearchRetrofitInterface {
        return retrofit.create(FriendSearchRetrofitInterface::class.java)
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

    @Provides
    @Singleton
    fun provideActivityApi(retrofit: Retrofit): ActivityService {
        return retrofit.create(ActivityService::class.java)
    }
}