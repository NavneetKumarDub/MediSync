package com.example.medisync.networks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{
//        const val BASE_URL = "http://10.0.2.2:3000/"
//        const val CHAT_WS_URL = "ws://10.0.2.2:3000/chat"
//        const val VIDEO_WS_URL = "ws://10.0.2.2:3000/video"
////
//    const val BASE_URL = "http://10.150.29.60:3000"
//    const val CHAT_WS_URL = "ws://10.150.29.60:3000/chat"
//    const val VIDEO_WS_URL = "ws://10.150.29.60:3000/video"

//    const val BASE_URL = "http://10.32.173.175:3000"
//    const val CHAT_WS_URL = "ws://10.32.173.175:3000/chat"
//    const val VIDEO_WS_URL = "ws://10.32.173.175:3000/video"

    const val BASE_URL = "http://210.79.129.117:3000/"

    const val CHAT_WS_URL = "ws://210.79.129.117:3000/chat"

    const val VIDEO_WS_URL = "ws://210.79.129.117:3000/video"
    const val MINIO_BASE_URL = "http://210.79.129.117:9000/profile-photos/"



//    const val BASE_URL = "https://gfnfmq1w-3000.inc1.devtunnels.ms/"
//    const val CHAT_WS_URL = "wss://gfnfmq1w-3000.inc1.devtunnels.ms/chat"
//    const val VIDEO_WS_URL = "wss://gfnfmq1w-3000.inc1.devtunnels.ms/video"

    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
