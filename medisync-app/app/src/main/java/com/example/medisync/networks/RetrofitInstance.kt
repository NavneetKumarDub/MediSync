package com.example.medisync.networks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{


//    const val BASE_URL = "https://n2m4vcwn-3000.inc1.devtunnels.ms/"
//
//    const val CHAT_WS_URL = "wss://n2m4vcwn-3000.inc1.devtunnels.ms/chat"
//
//    const val VIDEO_WS_URL = "wss://n2m4vcwn-3000.inc1.devtunnels.ms/video"
//
//    const val MINIO_BASE_URL = "https://n2m4vcwn-9000.inc1.devtunnels.ms/profile-photos/"

//    const val BASE_URL = "http://10.0.2.2:3000/"
//
//    const val CHAT_WS_URL = "ws://10.0.2.2:3000/chat"
//
//    const val VIDEO_WS_URL = "ws://10.0.2.2:3000/video"
//
//    const val MINIO_BASE_URL = "http://10.192.200.175:9000/profile-photos/"

//    const val BASE_URL = "https://true-shrew-67.rshare.io/"
//    const val CHAT_WS_URL = "wss://true-shrew-67.rshare.io/chat"
//    const val VIDEO_WS_URL = "wss://true-shrew-67.rshare.io/video"
//    const val MINIO_BASE_URL = "https://full-caiman-63.rshare.io/profile-photos/"

//    const val BASE_URL = "http://10.0.2.2:3000/"
//    const val CHAT_WS_URL = "ws://10.0.2.2:3000/chat"
//    const val VIDEO_WS_URL = "ws://10.0.2.2:3000/video"

    const val BASE_URL = "http://10.192.200.175:3000/"
    const val CHAT_WS_URL = "ws://10.192.200.175:3000/chat"
    const val VIDEO_WS_URL = "ws://10.192.200.175:3000/video"

    const val MINIO_BASE_URL = "http://10.192.200.175:9000/profile-photos/"




    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
