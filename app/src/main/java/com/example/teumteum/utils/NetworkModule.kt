package com.example.teumteum.utils

import android.content.Context
import android.util.Log
import com.example.teumteum.BuildConfig
import com.example.teumteum.data.remote.login.model.ReissueRequest
import com.example.teumteum.data.remote.login.service.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

//    @Provides
//    @Singleton
//    fun provideAuthInterceptor(): Interceptor {
//        return Interceptor { chain ->
//            val newRequest = chain.request().newBuilder()
//                .addHeader("Authorization", "Bearer ${BuildConfig.TEMP_ACCESS_TOKEN}")
//                .build()
//            chain.proceed(newRequest)
//        }
//    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            //토큰 필요없는 요청들
            val noAuthPaths = listOf("/api/auth/social-login/kakao", "/api/auth/reissue")
            if (noAuthPaths.any { originalRequest.url.encodedPath.contains(it) }) {
                // 토큰 필요 없는 요청은 바로 진행
                return@Interceptor chain.proceed(originalRequest)
            }

            var accessToken = tokenProvider.getAccessToken()
            val refreshToken = tokenProvider.getRefreshToken()

            val requestWithToken = originalRequest.newBuilder().apply {
                if (!accessToken.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $accessToken")
                }
            }.build()

            val response = chain.proceed(requestWithToken)

            // 액세스 토큰이 만료된 경우 (401 응답)
            if (response.code == 401 && !refreshToken.isNullOrEmpty()) {
                response.close()

                val newAccessToken = runBlocking {
                    try {
                        val retrofit = Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        val tempAuthService = retrofit.create(AuthService::class.java)

                        val reissueResponse = tempAuthService.reissue(ReissueRequest(refreshToken))

                        if (reissueResponse.isSuccessful) {
                            val newToken = reissueResponse.body()?.result
                            Log.d("Reissue", "NewAccessToken: ${newToken?.accessToken}, NewRefreshToken: ${newToken?.refreshToken}")
                            tokenProvider.saveTokens(
                                accessToken = newToken?.accessToken ?: "",
                                refreshToken = newToken?.refreshToken ?: refreshToken
                            )
                            newToken?.accessToken
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                if (!newAccessToken.isNullOrEmpty()) {
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                    return@Interceptor chain.proceed(newRequest)
                }
            }

            return@Interceptor response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: String,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

const val BASE_URL = BuildConfig.BASE_URL

fun getRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun getRetrofitWithToken(): Retrofit {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.TEMP_ACCESS_TOKEN}")
                .build()
            chain.proceed(request)
        }
        .build()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}