package com.squadsports.sdk.realtime

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class OfflineActionType(val value: String) {
    MESSAGE_CREATE("message:create"),
    FREESTYLE_CREATE("freestyle:create"),
    POLL_RESPONSE("poll:response"),
    MESSAGE_REACTION("message:reaction"),
    FREESTYLE_REACTION("freestyle:reaction"),
    USER_UPDATE("user:update"),
}

data class OfflineAction(
    val id: String,
    val type: OfflineActionType,
    val payload: String,
    val createdAt: Long,
    var attempts: Int = 0,
)

/**
 * Queues mutations when offline and replays them when connectivity returns.
 */
class SquadOfflineQueue(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("squad_offline_queue", Context.MODE_PRIVATE)
    private var queue = mutableListOf<OfflineAction>()
    private var isProcessing = false

    init { loadFromDisk() }

    fun enqueue(type: OfflineActionType, payload: String) {
        queue.add(OfflineAction(
            id = "${System.currentTimeMillis()}-${(Math.random() * 100000).toInt()}",
            type = type,
            payload = payload,
            createdAt = System.currentTimeMillis(),
        ))
        saveToDisk()
    }

    suspend fun processQueue(executor: suspend (OfflineAction) -> Boolean): Pair<Int, Int> {
        if (isProcessing || queue.isEmpty()) return 0 to 0
        isProcessing = true

        var succeeded = 0
        var failed = 0
        val pending = queue.toList()
        queue.clear()

        for (action in pending) {
            action.attempts++
            val success = executor(action)
            if (success) {
                succeeded++
            } else if (action.attempts < 3) {
                queue.add(action)
            } else {
                failed++
            }
        }

        isProcessing = false
        saveToDisk()
        return succeeded to failed
    }

    val pendingCount: Int get() = queue.size

    fun clear() {
        queue.clear()
        saveToDisk()
    }

    private fun saveToDisk() {
        val arr = JSONArray()
        queue.forEach { action ->
            arr.put(JSONObject().apply {
                put("id", action.id)
                put("type", action.type.value)
                put("payload", action.payload)
                put("createdAt", action.createdAt)
                put("attempts", action.attempts)
            })
        }
        prefs.edit().putString("queue", arr.toString()).apply()
    }

    private fun loadFromDisk() {
        val raw = prefs.getString("queue", null) ?: return
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.getString("type")
                val type = OfflineActionType.entries.find { it.value == typeStr } ?: continue
                queue.add(OfflineAction(
                    id = obj.getString("id"),
                    type = type,
                    payload = obj.getString("payload"),
                    createdAt = obj.getLong("createdAt"),
                    attempts = obj.optInt("attempts", 0),
                ))
            }
        } catch (_: Exception) {}
    }
}
