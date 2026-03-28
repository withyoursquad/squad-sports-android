package com.squadsports.sdk

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.squadsports.sdk.api.SquadAPIClient
import com.squadsports.sdk.auth.SquadAuth
import com.squadsports.sdk.journey.SquadJourneyManager
import com.squadsports.sdk.realtime.SquadAnalytics
import com.squadsports.sdk.realtime.SquadEventProcessor
import com.squadsports.sdk.squadline.SquadLineManager
import com.squadsports.sdk.utils.SquadLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Full configuration for the Squad Sports SDK.
 */
data class SquadSDKConfig(
    val apiKey: String,
    val environment: SquadEnvironment = SquadEnvironment.PRODUCTION,
    val community: CommunityConfig,
    val features: FeatureFlags = FeatureFlags(),
    val sso: SSOConfig? = null,
    val partnerAuth: PartnerAuthContext? = null,
)

enum class SquadEnvironment(val baseUrl: String) {
    PRODUCTION("https://api-release.withyoursquad.com"),
    STAGING("https://api-staging.withyoursquad.com"),
    DEVELOPMENT("https://api-dev.withyoursquad.com"),
}

data class CommunityConfig(
    val id: String,
    val name: String,
    val primaryColor: String,
    val secondaryColor: String? = null,
)

data class FeatureFlags(
    val squadLine: Boolean = false,
    val freestyle: Boolean = true,
    val messaging: Boolean = true,
    val polls: Boolean = true,
    val events: Boolean = true,
    val wallet: Boolean = false,
)

data class SSOConfig(
    val provider: SSOProvider,
    val accessToken: String? = null,
    val clientId: String? = null,
)

data class PartnerUserData(
    val email: String? = null,
    val phone: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val externalUserId: String? = null,
)

data class PartnerAuthContext(
    val partnerId: String,
    val communityId: String,
    val userData: PartnerUserData? = null,
)

enum class SSOProvider(val value: String) {
    TICKETMASTER("ticketmaster"),
    OAUTH2("oauth2"),
    CUSTOM("custom"),
}

/**
 * Main entry point for the Squad Sports SDK on Android.
 *
 * **Simplest integration (Nexus partner):**
 * ```kotlin
 * SquadSportsSDK.setup(context, partnerId = "acme-sports")
 * SquadExperienceActivity.launch(context)
 * ```
 *
 * **With Ticketmaster SSO:**
 * ```kotlin
 * SquadSportsSDK.setup(
 *     context,
 *     partnerId = "acme-sports",
 *     ssoToken = tmAccessToken,
 *     ssoProvider = SSOProvider.TICKETMASTER
 * )
 * ```
 *
 * **Full control:**
 * ```kotlin
 * SquadSportsSDK.initialize(context, SquadSDKConfig(...))
 * ```
 */
object SquadSportsSDK {

    private var _instance: SquadSportsSDKInstance? = null

    val shared: SquadSportsSDKInstance
        get() = _instance
            ?: throw IllegalStateException("SquadSportsSDK not initialized. Call setup() or initialize() first.")

    val isInitialized: Boolean
        get() = _instance != null

    /**
     * Fire-and-forget setup. Call from Application.onCreate() or any Activity.
     * No coroutine scope needed — the SDK handles it internally.
     */
    fun setup(
        context: Context,
        partnerId: String,
        apiKey: String,
        environment: SquadEnvironment = SquadEnvironment.PRODUCTION,
        ssoToken: String? = null,
        ssoProvider: SSOProvider? = null,
        userData: PartnerUserData? = null,
        pushToken: String? = null,
        onReady: ((SquadSportsSDKInstance) -> Unit)? = null,
    ) {
        kotlinx.coroutines.MainScope().launch {
            val instance = setupAsync(context, partnerId, apiKey, environment, ssoToken, ssoProvider, userData, pushToken)
            onReady?.invoke(instance)
        }
    }

    /**
     * Async setup for callers already in a coroutine scope.
     */
    suspend fun setupAsync(
        context: Context,
        partnerId: String,
        apiKey: String,
        environment: SquadEnvironment = SquadEnvironment.PRODUCTION,
        ssoToken: String? = null,
        ssoProvider: SSOProvider? = null,
        userData: PartnerUserData? = null,
        pushToken: String? = null,
    ): SquadSportsSDKInstance {
        require(partnerId.isNotBlank()) { "Squad SDK: partnerId is empty. Pass your Nexus partner ID to setup()." }
        require(apiKey.isNotBlank()) { "Squad SDK: apiKey is empty. Pass your API key to setup()." }

        val config = NexusPartnerResolver.resolve(partnerId, apiKey, environment, ssoToken, ssoProvider, userData)
        val instance = SquadSportsSDKInstance(context.applicationContext, config)
        _instance = instance

        // Configure logger context
        SquadLogger.partnerId = partnerId
        SquadLogger.environment = environment.name

        // Configure analytics
        SquadAnalytics.configure(
            partnerId = partnerId,
            userId = instance.auth.storedUserId,
            baseUrl = environment.baseUrl,
        )

        // Restore session
        val restored = instance.restoreSession()

        if (restored) {
            SquadAnalytics.track("session_restored")
            SquadLogger.info("Session restored for partner $partnerId")

            // Community scoping — re-auth if community changed
            config.partnerAuth?.let { partnerAuth ->
                val storedCommunityId = instance.auth.storedCommunityId
                if (storedCommunityId != null && storedCommunityId != partnerAuth.communityId) {
                    instance.auth.logout()
                }
            }
        }

        // Auto-authenticate via SSO
        if (config.sso?.accessToken != null && instance.auth.storedAccessToken == null) {
            val ssoSuccess = instance.authenticateWithSSO(config.sso.provider, config.sso.accessToken)
            if (ssoSuccess) {
                SquadAnalytics.track("sso_login", mapOf("provider" to config.sso.provider.value))
                SquadLogger.info("SSO login succeeded via ${config.sso.provider.value}")
            }
            config.partnerAuth?.let { partnerAuth ->
                if (instance.auth.storedAccessToken != null) {
                    instance.auth.saveCommunityId(partnerAuth.communityId)
                    instance.auth.savePartnerId(partnerAuth.partnerId)
                }
            }
        }

        // Partner user sync — if userData provided and still no valid token
        config.partnerAuth?.let { partnerAuth ->
            partnerAuth.userData?.let { userData ->
                if (instance.auth.storedAccessToken == null) {
                    val success = instance.partnerUserSync(partnerAuth.partnerId, partnerAuth.communityId, userData)
                    if (success) {
                        SquadAnalytics.track("partner_sync_success", mapOf("partnerId" to partnerAuth.partnerId))
                        SquadLogger.info("Partner user sync succeeded for ${partnerAuth.partnerId}")
                    } else {
                        SquadAnalytics.track("partner_sync_failed", mapOf("partnerId" to partnerAuth.partnerId))
                        SquadLogger.warn("Partner user sync failed. User will be directed to auth screen.")
                    }
                }
            }
        }

        // Wire silent re-auth on 401/403 (partner sync + SSO retry)
        val ssoConfig = config.sso
        val partnerAuth = config.partnerAuth
        val hasPartnerUserData = partnerAuth?.userData != null
        val hasSSOToken = ssoConfig?.accessToken != null

        if (hasPartnerUserData || hasSSOToken) {
            instance.apiClient.silentReAuthHandler = handler@{
                // Try partner re-auth first
                if (hasPartnerUserData && partnerAuth?.userData != null) {
                    SquadLogger.info("Attempting silent partner re-auth")
                    val success = instance.partnerUserSync(
                        partnerAuth.partnerId, partnerAuth.communityId, partnerAuth.userData
                    )
                    if (success) {
                        SquadLogger.info("Silent partner re-auth succeeded")
                        return@handler instance.auth.storedAccessToken
                    }
                }
                // Try SSO token re-exchange
                if (hasSSOToken && ssoConfig != null) {
                    SquadLogger.info("Attempting silent SSO re-auth")
                    try {
                        val success = instance.authenticateWithSSO(ssoConfig.provider, ssoConfig.accessToken!!)
                        if (success) {
                            SquadLogger.info("Silent SSO re-auth succeeded")
                            return@handler instance.auth.storedAccessToken
                        }
                    } catch (_: Exception) {
                        SquadLogger.warn("Silent SSO re-auth failed")
                    }
                }
                SquadLogger.warn("All silent re-auth attempts failed")
                null
            }
        }

        // Register lifecycle observer
        instance.registerLifecycleObserver()

        // Hydrate journey after auth setup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val journeyData = instance.apiClient.getCustomerJourney()
                val journeyJson = JSONObject(String(journeyData))
                val flagsObj = journeyJson.optJSONObject("flags")
                val milestonesObj = journeyJson.optJSONObject("milestones")
                val flagsMap = mutableMapOf<String, Boolean>()
                val milestonesMap = mutableMapOf<String, Boolean>()
                flagsObj?.keys()?.forEach { key -> flagsMap[key] = flagsObj.optBoolean(key, false) }
                milestonesObj?.keys()?.forEach { key -> milestonesMap[key] = milestonesObj.optBoolean(key, false) }
                SquadJourneyManager.shared.hydrate(flags = flagsMap, milestones = milestonesMap)
            } catch (e: Exception) {
                SquadJourneyManager.shared.hydrate(emptyMap(), emptyMap())
            }
            SquadJourneyManager.shared.setMilestoneSync { milestone ->
                try { instance.apiClient.addMilestoneToJourney(milestone) } catch (_: Exception) {}
            }
        }

        // Register push token if provided
        if (pushToken != null) {
            instance.apiClient.updateDeviceInfo(pushToken, "android")
        }

        // Track SDK initialization
        SquadAnalytics.track("sdk_initialized", mapOf(
            "partnerId" to partnerId,
            "environment" to environment.name,
        ))
        SquadLogger.info("SDK initialized for partner $partnerId in ${environment.name}")

        return instance
    }

    /**
     * Initialize with full config (no auto-resolution).
     */
    fun initialize(context: Context, config: SquadSDKConfig): SquadSportsSDKInstance {
        val instance = SquadSportsSDKInstance(context.applicationContext, config)
        _instance = instance
        return instance
    }

    fun reset() {
        _instance = null
    }
}

/**
 * Instance holding all SDK components.
 */
class SquadSportsSDKInstance internal constructor(
    val context: Context,
    val config: SquadSDKConfig,
) {
    val apiClient = SquadAPIClient(
        baseUrl = config.environment.baseUrl,
        apiKey = config.apiKey,
    )

    val auth = SquadAuth(context, apiClient)
    val squadLine = SquadLineManager(context, apiClient)

    private var lifecycleObserver: SquadLifecycleObserver? = null

    suspend fun restoreSession(): Boolean {
        val token = auth.storedAccessToken ?: return false
        apiClient.setAccessToken(token)
        return apiClient.validateToken()
    }

    suspend fun partnerUserSync(partnerId: String, communityId: String, userData: PartnerUserData): Boolean {
        return try {
            val data = apiClient.partnerUserSync(partnerId, communityId, userData)
            val json = JSONObject(String(data))
            val accessToken = json.optString("accessToken", "")
            val userId = json.optString("userId", "")
            if (accessToken.isNotEmpty() && userId.isNotEmpty()) {
                apiClient.setAccessToken(accessToken)
                auth.saveToken(accessToken)
                auth.saveUserId(userId)
                auth.saveCommunityId(communityId)
                auth.savePartnerId(partnerId)
                SquadLogger.userId = userId
                SquadAnalytics.setUserId(userId)
                true
            } else false
        } catch (e: Exception) {
            SquadLogger.error("Partner user sync exception: ${e.message}", throwable = e)
            false
        }
    }

    suspend fun authenticateWithSSO(provider: SSOProvider, token: String): Boolean {
        return try {
            val data = apiClient.exchangeSSOToken(provider.value, token)
            val json = JSONObject(String(data))
            val accessToken = json.optString("accessToken", "")
            if (accessToken.isNotEmpty()) {
                apiClient.setAccessToken(accessToken)
                true
            } else false
        } catch (e: Exception) {
            SquadLogger.error("SSO authentication failed: ${e.message}", throwable = e)
            false
        }
    }

    /**
     * Registers a ProcessLifecycleOwner observer to handle app
     * foreground / background transitions.
     */
    internal fun registerLifecycleObserver() {
        try {
            val observer = SquadLifecycleObserver(this)
            lifecycleObserver = observer
            // Must be called on main thread
            MainScope().launch {
                ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
            }
        } catch (e: Exception) {
            // ProcessLifecycleOwner may not be available in all contexts (e.g. tests)
            SquadLogger.warn("Could not register lifecycle observer: ${e.message}")
        }
    }
}

/**
 * Observes app-level lifecycle (foreground / background) via ProcessLifecycleOwner.
 */
internal class SquadLifecycleObserver(
    private val sdk: SquadSportsSDKInstance,
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        // App moved to foreground
        SquadLogger.info("App foregrounded — reconnecting EventProcessor, validating token")
        SquadEventProcessor.shared.connect()

        // Validate token in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sdk.apiClient.validateToken()
            } catch (_: Exception) { }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App moved to background
        SquadLogger.info("App backgrounded — flushing analytics, disconnecting EventProcessor")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SquadAnalytics.flush()
            } catch (_: Exception) { }
        }
        SquadEventProcessor.shared.disconnect()
    }
}

/**
 * Resolves partner ID into full SDK config via Nexus API.
 */
internal object NexusPartnerResolver {
    private val client = OkHttpClient()

    suspend fun resolve(
        partnerId: String,
        apiKey: String,
        environment: SquadEnvironment,
        ssoToken: String?,
        ssoProvider: SSOProvider?,
        userData: PartnerUserData? = null,
    ): SquadSDKConfig = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${environment.baseUrl}/v2/partners/$partnerId/provision")
            .header("Accept", "application/json")
            .header("X-Squad-Partner-ID", partnerId)
            .header("X-Squad-API-Key", apiKey)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Partner '$partnerId' not found. Check your partner ID at partners.squadforsports.com"
                )
            }

            val json = JSONObject(response.body?.string() ?: "{}")
            val community = json.getJSONObject("community")

            var ssoConfig: SSOConfig? = null
            if (ssoToken != null) {
                val ssoJson = json.optJSONObject("sso")
                ssoConfig = SSOConfig(
                    provider = ssoProvider ?: SSOProvider.TICKETMASTER,
                    accessToken = ssoToken,
                    clientId = ssoJson?.optString("clientId"),
                )
            }

            val communityId = community.getString("id")

            val partnerAuth = when {
                userData != null -> PartnerAuthContext(partnerId, communityId, userData)
                ssoToken != null -> PartnerAuthContext(partnerId, communityId)
                else -> null
            }

            SquadSDKConfig(
                apiKey = apiKey,
                environment = environment,
                community = CommunityConfig(
                    id = communityId,
                    name = community.getString("name"),
                    primaryColor = community.getString("primaryColor"),
                    secondaryColor = community.optString("secondaryColor", null),
                ),
                sso = ssoConfig,
                partnerAuth = partnerAuth,
            )
        }
    }
}
