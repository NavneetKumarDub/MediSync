package com.example.medisync.data

import android.content.Context
import com.example.medisync.networks.ChatWebSocket
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.ServerMessage
import com.example.medisync.networks.WsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

class ChatRepository(context: Context) {

    private val appContext = context.applicationContext

    // Your server IP — change this to your machine's local IP
    // For emulator use 10.0.2.2, for real device use your WiFi IP
    private val wsClient = ChatWebSocket(RetrofitInstance.CHAT_WS_URL)

    val messages: SharedFlow<ServerMessage> = wsClient.messages
    val state:    SharedFlow<WsState>        = wsClient.state

    suspend fun connect(roomId: Int) {
        val token = TokenManager.getToken(appContext) ?: return
        wsClient.connect()
        // Small delay to ensure connection is open before joining
        delay(500)
        wsClient.sendJoin(roomId, token)
    }

    fun sendMessage(roomId: Int, text: String) {
        wsClient
            .sendMessage(roomId, text)
    }

    fun sendReadReceipt(roomId: Int, messageId: Int) {
        wsClient.sendReadReceipt(roomId, messageId)
    }

    fun disconnect() {
        wsClient.disconnect()
    }

    companion object {
        // Singleton so one WebSocket connection is shared
        @Volatile private var instance: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository(context).also { instance = it }
            }
        }
    }
}