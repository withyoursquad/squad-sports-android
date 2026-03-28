package com.squadsports.sdk.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data models for Squad API responses.
 * These provide a JSON fallback until protobuf-kotlin models are generated from proto/.
 */
object SquadModels {

    data class User(
        val id: String,
        val displayName: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val imageUrl: String? = null,
        val community: String? = null,
        val status: String? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = User(
                id = json.getString("id"),
                displayName = json.optString("displayName", json.optString("display_name")),
                email = json.optString("email"),
                phone = json.optString("phone"),
                imageUrl = json.optString("imageUrl", json.optString("image_url")),
                community = json.optString("community"),
                status = json.optString("status"),
            )
        }
    }

    data class Connection(
        val id: String,
        val status: String? = null,
        val creator: User? = null,
        val recipient: User? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Connection(
                id = json.getString("id"),
                status = json.optString("status"),
                creator = json.optJSONObject("creator")?.let { User.fromJson(it) },
                recipient = json.optJSONObject("recipient")?.let { User.fromJson(it) },
            )
        }
    }

    data class Squad(val connections: List<Connection>) {
        companion object {
            fun fromJson(json: JSONObject): Squad {
                val arr = json.optJSONArray("connections") ?: JSONArray()
                val conns = (0 until arr.length()).map { Connection.fromJson(arr.getJSONObject(it)) }
                return Squad(conns)
            }
        }
    }

    data class Message(
        val id: String,
        val text: String? = null,
        val audioUrl: String? = null,
        val sender: User? = null,
        val createdAt: String? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Message(
                id = json.getString("id"),
                text = json.optString("text"),
                audioUrl = json.optString("audioUrl", json.optString("audio_url")),
                sender = json.optJSONObject("sender")?.let { User.fromJson(it) },
                createdAt = json.optString("createdAt", json.optString("created_at")),
            )
        }
    }

    data class Freestyle(
        val id: String,
        val audioUrl: String? = null,
        val duration: Double? = null,
        val creator: User? = null,
        val createdAt: String? = null,
        val listenCount: Int? = null,
        val reactionCount: Int? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Freestyle(
                id = json.getString("id"),
                audioUrl = json.optString("audioUrl", json.optString("audio_url")),
                duration = json.optDouble("duration").takeIf { !it.isNaN() },
                creator = json.optJSONObject("creator")?.let { User.fromJson(it) },
                createdAt = json.optString("createdAt", json.optString("created_at")),
                listenCount = json.optInt("listenCount", json.optInt("listen_count")),
                reactionCount = json.optInt("reactionCount", json.optInt("reaction_count")),
            )
        }
    }

    data class Community(
        val id: String,
        val name: String,
        val color: String? = null,
        val secondaryColor: String? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Community(
                id = json.getString("id"),
                name = json.getString("name"),
                color = json.optString("color"),
                secondaryColor = json.optString("secondary_color", json.optString("secondaryColor")),
            )
        }
    }

    data class Event(
        val id: String,
        val title: String? = null,
        val description: String? = null,
        val startDate: String? = null,
        val location: String? = null,
        val attendeeCount: Int = 0,
        val isAttending: Boolean = false,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Event(
                id = json.getString("id"),
                title = json.optString("title"),
                description = json.optString("description"),
                startDate = json.optString("startDate", json.optString("start_date")),
                location = json.optString("location"),
                attendeeCount = json.optInt("attendeeCount", json.optInt("attendee_count")),
                isAttending = json.optBoolean("isAttending", json.optBoolean("is_attending")),
            )
        }
    }

    data class Wallet(val balance: Int = 0) {
        companion object {
            fun fromJson(json: JSONObject) = Wallet(balance = json.optInt("balance"))
        }
    }

    data class Coupon(
        val id: String,
        val code: String? = null,
        val title: String? = null,
        val description: String? = null,
        val discount: String? = null,
        val status: String? = null,
    ) {
        companion object {
            fun fromJson(json: JSONObject) = Coupon(
                id = json.getString("id"),
                code = json.optString("code"),
                title = json.optString("title", json.optString("name")),
                description = json.optString("description"),
                discount = json.optString("discount", json.optString("value")),
                status = json.optString("status"),
            )
        }
    }
}
