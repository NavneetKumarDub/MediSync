package com.example.medisync.networks

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


sealed class WsState {
    object Connecting  : WsState()
    object Connected   : WsState()
    object Reconnecting: WsState()
    data class Error(val message: String) : WsState()
    object Closed      : WsState()
}


sealed class ServerMessage {
    data class Joined(val roomId: Int) : ServerMessage()
    data class NewMessage(
        val messageId: Int,
        val roomId: Int,
        val senderId: Int,
        val text: String,
        val sentAt: String
    ) : ServerMessage()
    data class ReadReceipt(val messageId: Int) : ServerMessage()
    data class Error(val message: String)      : ServerMessage()
}

class ChatWebSocket(
    private val serverUrl: String  
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    
    private val _messages = MutableSharedFlow<ServerMessage>(replay = 0)
    val messages: SharedFlow<ServerMessage> = _messages

    private val _state = MutableSharedFlow<WsState>(replay = 1)
    val state: SharedFlow<WsState> = _state

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)  
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    fun connect() {
        _state.tryEmit(WsState.Connecting)

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                reconnectAttempts = 0
                _state.tryEmit(WsState.Connected)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JsonParser.parseString(text).asJsonObject
                    val type = json.get("type")?.asString ?: return

                    val serverMsg: ServerMessage = when (type) {
                        "joined" -> ServerMessage.Joined(
                            roomId = json.get("roomId").asInt
                        )
                        "message" -> ServerMessage.NewMessage(
                            messageId = json.get("messageId").asInt,
                            roomId    = json.get("roomId").asInt,
                            senderId  = json.get("senderId").asInt,
                            text      = json.get("text").asString,
                            sentAt    = json.get("sentAt").asString
                        )
                        "read" -> ServerMessage.ReadReceipt(
                            messageId = json.get("messageId").asInt
                        )
                        "error" -> ServerMessage.Error(
                            message = json.get("message").asString
                        )
                        else -> return
                    }

                    _messages.tryEmit(serverMsg)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _state.tryEmit(WsState.Error(t.message ?: "Connection failed"))
                tryReconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _state.tryEmit(WsState.Closed)
            }
        })
    }

    

    fun sendJoin(roomId: Int, token: String) {
        send(mapOf("type" to "join", "roomId" to roomId, "token" to token))
    }

    fun sendMessage(roomId: Int, text: String) {
        send(mapOf("type" to "message", "roomId" to roomId, "text" to text))
    }

    fun sendReadReceipt(roomId: Int, messageId: Int) {
        send(mapOf("type" to "read", "roomId" to roomId, "messageId" to messageId))
    }

    private fun send(data: Map<String, Any>) {
        webSocket?.send(gson.toJson(data))
    }

    

    private fun tryReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            _state.tryEmit(WsState.Error("Max reconnect attempts reached"))
            return
        }
        reconnectAttempts++
        _state.tryEmit(WsState.Reconnecting)

        
        Thread.sleep(3000)
        connect()
    }

    fun disconnect() {
        webSocket?.close(1000, "User left chat")
        webSocket = null
    }
}