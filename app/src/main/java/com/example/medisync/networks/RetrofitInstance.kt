package com.example.medisync.networks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{
//        const val BASE_URL = "http://10.0.2.2:3000/"
//        const val CHAT_WS_URL = "ws://10.0.2.2:3000/chat"
//        const val VIDEO_WS_URL = "ws://10.0.2.2:3000/video"
//
        const val BASE_URL = "http://10.208.65.60:3000"
        const val CHAT_WS_URL = "ws://10.208.65.60:3000/chat"
        const val VIDEO_WS_URL = "ws://10.208.65.60:3000/video"


    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
