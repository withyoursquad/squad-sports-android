package com.squadsports.sdk.realtime

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Monitors network connectivity and triggers reconnection.
 */
class SquadNetworkMonitor(private val context: Context) {

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var callback: ConnectivityManager.NetworkCallback? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var offlineQueue: SquadOfflineQueue? = null

    fun setOfflineQueue(queue: SquadOfflineQueue) {
        this.offlineQueue = queue
    }

    fun startMonitoring() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = !_isOnline.value
                _isOnline.value = true

                if (wasOffline) {
                    onReconnected()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                SquadEventProcessor.shared.setShouldAllowEvents(false)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isOnline.value = hasInternet
            }
        }

        connectivityManager?.registerNetworkCallback(request, callback!!)
    }

    fun stopMonitoring() {
        callback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        callback = null
    }

    private fun onReconnected() {
        // Reconnect SSE
        SquadEventProcessor.shared.setShouldAllowEvents(true)
        SquadEventProcessor.shared.connect()

        // Flush offline queue
        offlineQueue?.let { queue ->
            scope.launch {
                queue.processQueue { action ->
                    // Route to API calls when wired
                    false
                }
            }
        }
    }
}
