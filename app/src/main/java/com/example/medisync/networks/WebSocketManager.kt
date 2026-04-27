package com.example.medisync.networks

import android.content.Context
import android.util.Log
import com.example.medisync.data.TokenManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

object WebSocketManager {

    private const val TAG = "WebSocketManager"

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private var manuallyDisconnected = false

    // Public state
    private val _state = MutableStateFlow(State.DISCONNECTED)
    val state: StateFlow<State> = _state

    // Public event stream — ViewModels filter by type
    private val _events = MutableSharedFlow<ServerEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events: SharedFlow<ServerEvent> = _events

    enum class State { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING }

    data class ServerEvent(val type: String, val data: JsonElement)

    fun connect(context: Context) {
        if (_state.value == State.CONNECTED || _state.value == State.CONNECTING) return
        manuallyDisconnected = false
        _state.value = State.CONNECTING

        scope.launch {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.w(TAG, "No token — skip connect")
                _state.value = State.DISCONNECTED
                return@launch
            }

            val request = Request.Builder()
                .url("${RetrofitInstance.WS_URL}?token=$token")
                .build()

            webSocket = client.newWebSocket(request, listener(context))
        }
    }

    fun disconnect() {
        manuallyDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _state.value = State.DISCONNECTED
    }

    fun send(type: String, data: Map<String, Any?> = emptyMap()): Boolean {
        val envelope = buildJsonObject {
            put("type", type)
            put("data", buildJsonObject {
                data.forEach { (k, v) ->
                    when (v) {
                        is String -> put(k, v)
                        is Int -> put(k, v)
                        is Long -> put(k, v)
                        is Boolean -> put(k, v)
                        is Double -> put(k, v)
                        null -> put(k, JsonNull)
                        else -> put(k, v.toString())
                    }
                }
            })
        }
        val sent = webSocket?.send(envelope.toString()) ?: false
        if (!sent) Log.w(TAG, "send($type) dropped — socket not open")
        return sent
    }

    private fun listener(context: Context) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WS open")
            _state.value = State.CONNECTED
            reconnectAttempts = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val obj = json.parseToJsonElement(text).jsonObject
                val type = obj["type"]?.jsonPrimitive?.content ?: return
                val data = obj["data"] ?: JsonNull
                scope.launch { _events.emit(ServerEvent(type, data)) }
            } catch (e: Exception) {
                Log.e(TAG, "Parse error: $text", e)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WS closed $code $reason")
            _state.value = State.DISCONNECTED
            if (!manuallyDisconnected) scheduleReconnect(context)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WS failure (status=${response?.code})", t)
            _state.value = State.DISCONNECTED
            if (!manuallyDisconnected) scheduleReconnect(context)
        }
    }

    private fun scheduleReconnect(context: Context) {
        reconnectJob?.cancel()
        val delayMs = min(2_000L * (1L shl reconnectAttempts), 30_000L)
        reconnectAttempts++
        _state.value = State.RECONNECTING
        Log.d(TAG, "Reconnect in ${delayMs}ms (attempt $reconnectAttempts)")
        reconnectJob = scope.launch {
            delay(delayMs)
            connect(context)
        }
    }
}