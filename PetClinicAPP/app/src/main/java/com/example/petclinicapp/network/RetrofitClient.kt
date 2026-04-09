package com.example.petclinicapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 1. Local Emulator: Use 10.0.2.2 to reach your machine's localhost from the Android Emulator
    private const val BASE_URL = "http://10.0.2.2:5269/api/"

    var token: String = "" // In a real app, this should be tracked via DataStore or SharedPreferences
    var userName: String = ""

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val builder = req.newBuilder()
        if (token.isNotEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        builder.addHeader("ngrok-skip-browser-warning", "69420")
        chain.proceed(builder.build())
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(authInterceptor)
        builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        return builder.build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
