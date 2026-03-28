package com.squadsports.sdk.squadline

import android.content.Context
import com.squadsports.sdk.api.SquadAPIClient
import com.twilio.voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages Twilio Voice SDK integration for Squad Line calls on Android.
 */
class SquadLineManager(
    private val context: Context,
    private val apiClient: SquadAPIClient,
) {
    enum class CallState {
        IDLE, CONNECTING, RINGING, CONNECTED, DISCONNECTED, FAILED
    }

    interface Listener {
        fun onCallStateChanged(state: CallState)
        fun onIncomingCall(from: String, title: String)
        fun onConnected()
        fun onDisconnected()
        fun onError(error: Exception)
    }

    var listener: Listener? = null
    var callState: CallState = CallState.IDLE
        private set

    var isMuted: Boolean = false
        private set

    var isSpeakerOn: Boolean = false
        private set

    private var activeCall: Call? = null

    // Registration

    /**
     * Register for incoming calls with the given FCM token.
     */
    suspend fun register(fcmToken: String) = withContext(Dispatchers.IO) {
        val voiceToken = apiClient.getVoiceToken()
        Voice.register(voiceToken, Voice.RegistrationChannel.FCM, fcmToken,
            object : RegistrationListener {
                override fun onRegistered(accessToken: String, fcmToken: String) {
                    // Registration successful
                }

                override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) {
                    listener?.onError(Exception("Registration failed: ${error.message}"))
                }
            }
        )
    }

    /**
     * Handle incoming FCM message for voice calls.
     */
    fun handleMessage(data: Map<String, String>): Boolean {
        return Voice.handleMessage(context, data, object : MessageListener {
            override fun onCallInvite(callInvite: CallInvite) {
                val title = callInvite.customParameters["title"] ?: "Incoming Call"
                val from = callInvite.from ?: "Unknown"
                listener?.onIncomingCall(from, title)
            }

            override fun onCancelledCallInvite(cancelledCallInvite: CancelledCallInvite, callException: CallException?) {
                updateState(CallState.IDLE)
            }
        })
    }

    // Outgoing calls

    /**
     * Make a call to a squad connection.
     */
    suspend fun makeCall(connectionId: String, title: String, calleeIdentity: String? = null) {
        if (callState != CallState.IDLE) return

        updateState(CallState.CONNECTING)

        try {
            // Create line room
            apiClient.createConnectionsLineRoom(connectionId)

            // Get voice token
            val voiceToken = withContext(Dispatchers.IO) { apiClient.getVoiceToken() }

            // Connect via Twilio
            val params = mutableMapOf<String, String>()
            calleeIdentity?.let { params["To"] = it }

            val connectOptions = ConnectOptions.Builder(voiceToken)
                .params(params)
                .build()

            activeCall = Voice.connect(context, connectOptions, callListener)
            updateState(CallState.RINGING)
        } catch (e: Exception) {
            updateState(CallState.FAILED)
            listener?.onError(e)
            cleanup()
        }
    }

    // Call controls

    fun endCall() {
        activeCall?.disconnect()
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        activeCall?.mute(isMuted)
        return isMuted
    }

    fun toggleSpeaker(): Boolean {
        isSpeakerOn = !isSpeakerOn
        // Audio routing handled via AudioManager
        return isSpeakerOn
    }

    // Private

    private val callListener = object : Call.Listener {
        override fun onRinging(call: Call) {
            updateState(CallState.RINGING)
        }

        override fun onConnectFailure(call: Call, error: CallException) {
            updateState(CallState.FAILED)
            listener?.onError(Exception(error.message))
            cleanup()
        }

        override fun onConnected(call: Call) {
            updateState(CallState.CONNECTED)
            listener?.onConnected()
        }

        override fun onReconnecting(call: Call, error: CallException) {
            // Reconnecting...
        }

        override fun onReconnected(call: Call) {
            updateState(CallState.CONNECTED)
        }

        override fun onDisconnected(call: Call, error: CallException?) {
            updateState(CallState.DISCONNECTED)
            listener?.onDisconnected()
            cleanup()
        }
    }

    private fun updateState(state: CallState) {
        callState = state
        listener?.onCallStateChanged(state)
    }

    private fun cleanup() {
        activeCall = null
        isMuted = false
        isSpeakerOn = false
        callState = CallState.IDLE
    }
}
