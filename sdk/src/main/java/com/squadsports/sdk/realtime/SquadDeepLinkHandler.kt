package com.squadsports.sdk.realtime

import android.net.Uri
import com.squadsports.sdk.navigation.SquadRoute

/**
 * Parses Squad deep links and universal links into navigation routes.
 *
 * Supported:
 * - squad://invite/{code}
 * - squad://profile/{userId}
 * - squad://message/{connectionId}
 * - https://app.withyoursquad.com/invite/{code}
 */
object SquadDeepLinkHandler {

    fun parseUri(uri: Uri): String? {
        val path = when {
            uri.scheme == "squad" -> "${uri.host ?: ""}${uri.path ?: ""}"
            uri.host == "app.withyoursquad.com" -> uri.path ?: ""
            else -> return null
        }

        val segments = path.trim('/').split("/").filter { it.isNotEmpty() }
        val type = segments.getOrNull(0) ?: return null
        val id = segments.getOrNull(1)

        return when (type) {
            "invite" -> SquadRoute.Invite.route
            "profile" -> id?.let { SquadRoute.Profile.create(it) }
            "message", "messaging" -> id?.let { SquadRoute.Messaging.create(it) }
            "poll" -> id?.let { SquadRoute.PollResponse.create(it) }
            "events", "event" -> SquadRoute.Events.route
            "wallet" -> SquadRoute.Wallet.route
            "freestyle" -> SquadRoute.FreestyleCreate.route
            else -> SquadRoute.Home.route
        }
    }
}
