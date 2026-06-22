package com.example.medisync.networks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{


    const val BASE_URL = "http://210.79.129.117:3000/"

    const val CHAT_WS_URL = "ws://210.79.129.117:3000/chat"

    const val VIDEO_WS_URL = "ws://210.79.129.117:3000/video"
    const val MINIO_BASE_URL = "http://210.79.129.117:9000/profile-photos/"




    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
