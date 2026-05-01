package com.example.medisync.networks

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import org.json.JSONObject

sealed class VideoSignalEvent {
    data class RoomJoined(val roomId: Int, val socketId: String) : VideoSignalEvent()
    data class UserJoined(val roomId: Int, val socketId: String) : VideoSignalEvent()
    data class UserLeft(val roomId: Int, val socketId: String) : VideoSignalEvent()
    data class OfferReceived(val sdp: String, val fromId: String) : VideoSignalEvent()
    data class AnswerReceived(val sdp: String, val fromId: String) : VideoSignalEvent()
    data class IceCandidateReceived(val candidate: String, val sdpMid: String, val sdpMLineIndex: Int, val fromId: String) : VideoSignalEvent()
    data class RenegotiateReceived(val sdp: String, val fromId: String) : VideoSignalEvent()
}

class VideoWebSocketManager {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    // ViewModel observes this to react to incoming signals
    private val _events = MutableSharedFlow<VideoSignalEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<VideoSignalEvent> = _events.asSharedFlow()

    fun connect(roomId: Int) {
        val request = Request.Builder()
            .url(RetrofitInstance.VIDEO_WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("VideoWS", "Connected to signaling server")
                // Auto join the room once connected
                joinRoom(roomId)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("VideoWS", "Received: $text")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("VideoWS", "WebSocket error: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("VideoWS", "WebSocket closed: $reason")
            }
        })
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.getString("type")

            when (type) {
                "room-joined" -> {
                    _events.tryEmit(
                        VideoSignalEvent.RoomJoined(
                            roomId = json.getInt("roomId"),
                            socketId = json.getString("id")
                        )
                    )
                }

                "user-joined" -> {
                    _events.tryEmit(
                        VideoSignalEvent.UserJoined(
                            roomId = json.getInt("roomId"),
                            socketId = json.getString("id")
                        )
                    )
                }

                "user-left" -> {
                    _events.tryEmit(
                        VideoSignalEvent.UserLeft(
                            roomId = json.getInt("roomId"),
                            socketId = json.getString("id")
                        )
                    )
                }

                "offer" -> {
                    val sdpJson = json.getJSONObject("sdp")
                    _events.tryEmit(
                        VideoSignalEvent.OfferReceived(
                            sdp = sdpJson.getString("sdp"),
                            fromId = json.getString("id")
                        )
                    )
                }

                "answer" -> {
                    val sdpJson = json.getJSONObject("sdp")
                    _events.tryEmit(
                        VideoSignalEvent.AnswerReceived(
                            sdp = sdpJson.getString("sdp"),
                            fromId = json.getString("id")
                        )
                    )
                }

                "ice-candidate" -> {
                    val candidateJson = json.getJSONObject("candidate")
                    _events.tryEmit(
                        VideoSignalEvent.IceCandidateReceived(
                            candidate = candidateJson.getString("candidate"),
                            sdpMid = candidateJson.getString("sdpMid"),
                            sdpMLineIndex = candidateJson.getInt("sdpMLineIndex"),
                            fromId = json.getString("id")
                        )
                    )
                }

                "renegotiate" -> {
                    val sdpJson = json.getJSONObject("sdp")
                    _events.tryEmit(
                        VideoSignalEvent.RenegotiateReceived(
                            sdp = sdpJson.getString("sdp"),
                            fromId = json.getString("id")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("VideoWS", "Failed to parse message: ${e.message}")
        }
    }

    // ─── Send Helpers ─────────────────────────────────────────────────────────

    fun joinRoom(roomId: Int) {
        send(JSONObject().apply {
            put("type", "join-room")
            put("roomId", roomId)
        })
    }

    fun sendOffer(sdp: String, roomId: Int, targetId: String? = null) {
        send(JSONObject().apply {
            put("type", "offer")
            put("roomId", roomId)
            targetId?.let { put("targetId", it) }
            put("sdp", JSONObject().apply {
                put("type", "offer")
                put("sdp", sdp)
            })
        })
    }

    fun sendAnswer(sdp: String, roomId: Int, targetId: String? = null) {
        send(JSONObject().apply {
            put("type", "answer")
            put("roomId", roomId)
            targetId?.let { put("targetId", it) }
            put("sdp", JSONObject().apply {
                put("type", "answer")
                put("sdp", sdp)
            })
        })
    }

    fun sendIceCandidate(candidate: String, sdpMid: String, sdpMLineIndex: Int, roomId: Int, targetId: String? = null) {
        send(JSONObject().apply {
            put("type", "ice-candidate")
            put("roomId", roomId)
            targetId?.let { put("targetId", it) }
            put("candidate", JSONObject().apply {
                put("candidate", candidate)
                put("sdpMid", sdpMid)
                put("sdpMLineIndex", sdpMLineIndex)
            })
        })
    }

    fun sendRenegotiate(sdp: String, roomId: Int, targetId: String? = null) {
        send(JSONObject().apply {
            put("type", "renegotiate")
            put("roomId", roomId)
            targetId?.let { put("targetId", it) }
            put("sdp", JSONObject().apply {
                put("type", "offer")
                put("sdp", sdp)
            })
        })
    }

    fun leaveRoom(roomId: Int) {
        send(JSONObject().apply {
            put("type", "leave-room")
            put("roomId", roomId)
        })
    }

    private fun send(json: JSONObject) {
        val message = json.toString()
        Log.d("VideoWS", "Sending: $message")
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
}