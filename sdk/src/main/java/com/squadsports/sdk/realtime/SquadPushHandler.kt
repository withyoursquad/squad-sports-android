package com.squadsports.sdk.realtime

import com.squadsports.sdk.navigation.SquadRoute

/**
 * Maps push notification data to navigation actions.
 *
 * Usage in FirebaseMessagingService:
 * ```kotlin
 * override fun onMessageReceived(message: RemoteMessage) {
 *     // Let Squad Line handle call notifications
 *     if (SquadSportsSDK.shared.squadLine.handleMessage(message.data)) return
 *
 *     // Route other notifications
 *     val route = SquadPushHandler.handleNotification(message.data)
 *     if (route != null) {
 *         // Navigate to route (via intent or stored pending route)
 *     }
 * }
 * ```
 */
object SquadPushHandler {

    fun handleNotification(data: Map<String, String>): String? {
        val type = data["type"] ?: return null

        return when (type) {
            "message" -> {
                val connectionId = data["connectionId"] ?: data["connection_id"] ?: return null
                SquadRoute.Messaging.create(connectionId)
            }
            "squad_invite" -> SquadRoute.Home.route
            "incoming_call" -> {
                // Emit to EventProcessor for in-app overlay
                val callerId = data["callerId"] ?: data["caller_id"] ?: ""
                val title = data["title"] ?: "Incoming Call"
                SquadEventProcessor.shared.let { processor ->
                    // Event will be picked up by call overlay
                }
                null
            }
            "poll" -> {
                val pollId = data["pollId"] ?: data["poll_id"] ?: return null
                SquadRoute.PollResponse.create(pollId)
            }
            "freestyle" -> SquadRoute.Home.route
            "reaction" -> {
                val connectionId = data["connectionId"] ?: data["connection_id"]
                if (connectionId != null) SquadRoute.Messaging.create(connectionId)
                else SquadRoute.Home.route
            }
            else -> null
        }
    }
}
