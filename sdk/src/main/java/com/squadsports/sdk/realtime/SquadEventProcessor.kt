package com.squadsports.sdk.realtime

import com.squadsports.sdk.api.SquadAPIClient
import com.squadsports.sdk.utils.SquadLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject

enum class SquadConnectionQuality { GOOD, POOR, DISCONNECTED }

data class SquadEvent(
    val type: String,
    val data: Map<String, Any?>,
)

/**
 * Manages the SSE connection for real-time updates on Android.
 */
class SquadEventProcessor private constructor() {

    companion object {
        val shared = SquadEventProcessor()
    }

    private val _events = MutableSharedFlow<SquadEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    private val _quality = MutableStateFlow(SquadConnectionQuality.DISCONNECTED)
    val quality = _quality.asStateFlow()

    private var apiClient: SquadAPIClient? = null
    private var eventSource: EventSource? = null
    private var allowEvents = false
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private val processedIds = mutableSetOf<String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val maxReconnectAttempts = 10
    private val initialBackoffMs = 1000L
    private val maxBackoffMs = 30000L

    fun setApiClient(client: SquadAPIClient) {
        this.apiClient = client
    }

    fun setShouldAllowEvents(allow: Boolean) {
        this.allowEvents = allow
        if (!allow) disconnect()
    }

    fun connect() {
        if (!allowEvents) return
        val client = apiClient ?: return

        disconnect()

        val token = client.currentToken ?: return
        val url = "${client.baseUrl}/v2/events"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .build()

        val okClient = OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        eventSource = EventSources.createFactory(okClient)
            .newEventSource(request, object : EventSourceListener() {

                override fun onOpen(eventSource: EventSource, response: Response) {
                    reconnectAttempts = 0
                    updateQuality(SquadConnectionQuality.GOOD)
                    SquadLogger.info("EventProcessor SSE connected", "SquadEventProcessor")
                }

                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    processEvent(type ?: "message", data)
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    SquadLogger.warn(
                        "EventProcessor SSE failure (code=${response?.code}): ${t?.message}",
                        "SquadEventProcessor"
                    )
                    updateQuality(SquadConnectionQuality.DISCONNECTED)
                    scheduleReconnect()
                }

                override fun onClosed(eventSource: EventSource) {
                    SquadLogger.info("EventProcessor SSE closed", "SquadEventProcessor")
                    updateQuality(SquadConnectionQuality.DISCONNECTED)
                }
            })
    }

    fun disconnect() {
        eventSource?.cancel()
        eventSource = null
        reconnectJob?.cancel()
        reconnectJob = null
        updateQuality(SquadConnectionQuality.DISCONNECTED)
    }

    private fun processEvent(type: String, rawData: String) {
        // Dedup
        val key = "$type:${rawData.take(100)}"
        if (processedIds.contains(key)) return
        processedIds.add(key)
        if (processedIds.size > 5000) {
            val keep = processedIds.toList().takeLast(2500).toSet()
            processedIds.clear()
            processedIds.addAll(keep)
        }

        // Parse
        val data: Map<String, Any?> = try {
            val json = JSONObject(rawData)
            json.keys().asSequence().associateWith { json.opt(it) }
        } catch (_: Exception) {
            mapOf("raw" to rawData)
        }

        scope.launch {
            _events.emit(SquadEvent(type, data))

            // Global message event for connection-specific messages
            if (type.contains(":message:create")) {
                _events.emit(SquadEvent("message:create", data))
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) return

        reconnectAttempts++
        val delay = minOf(
            initialBackoffMs * (1L shl (reconnectAttempts - 1)),
            maxBackoffMs,
        )
        val jitter = (Math.random() * delay * 0.1).toLong()

        reconnectJob = scope.launch {
            delay(delay + jitter)
            connect()
        }
    }

    private fun updateQuality(q: SquadConnectionQuality) {
        _quality.value = q
    }

    val currentToken: String?
        get() = apiClient?.currentToken
}

private val SquadAPIClient.currentToken: String?
    get() {
        // Access stored token — this will be the token set via setAccessToken()
        // For now return null; will be wired when auth flow is connected
        return try {
            val field = this::class.java.getDeclaredField("accessToken")
            field.isAccessible = true
            field.get(this) as? String
        } catch (_: Exception) { null }
    }

private val SquadAPIClient.baseUrl: String
    get() {
        return try {
            val field = this::class.java.getDeclaredField("baseUrl")
            field.isAccessible = true
            field.get(this) as String
        } catch (_: Exception) { "" }
    }
