package com.squadsports.sdk.api

import com.squadsports.sdk.utils.SquadLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Native Android API client for the Squad v2 API.
 * Mirrors the TypeScript SquadApiClient from @squad-sports/core.
 */
class SquadAPIClient(
    private val baseUrl: String,
    private val apiKey: String,
) {
    private var accessToken: String? = null

    companion object {
        private const val SDK_VERSION = "SquadSportsSDK-Android/1.6.0"
        private const val DEFAULT_TIMEOUT_SECONDS = 15L
        private const val UPLOAD_TIMEOUT_SECONDS = 30L
        private const val MAX_RETRY_AFTER_SECONDS = 30L
    }

    /**
     * Optional silent re-auth handler. When set, 401/403 responses will
     * invoke this callback to attempt a transparent partner re-sync.
     * The callback should return a new access token on success, or null on failure.
     */
    var silentReAuthHandler: (suspend () -> String?)? = null

    // --- SDK version header interceptor ---
    private val sdkVersionInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("X-Squad-SDK-Version", SDK_VERSION)
            .build()
        chain.proceed(request)
    }

    // --- Auth header interceptor ---
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("X-Squad-API-Key", apiKey)

        accessToken?.let {
            builder.header("Authorization", "Bearer $it")
        }

        chain.proceed(builder.build())
    }

    // --- 429 retry interceptor ---
    private val rateLimitInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 429) {
            val retryAfterHeader = response.header("Retry-After")
            val retryAfterSeconds = retryAfterHeader?.toLongOrNull() ?: 1L
            val cappedDelay = minOf(retryAfterSeconds, MAX_RETRY_AFTER_SECONDS)

            SquadLogger.warn("Rate limited (429). Retrying after ${cappedDelay}s", "SquadAPIClient")

            response.close()
            Thread.sleep(cappedDelay * 1000)

            // Retry once with fresh auth headers (rebuild to pick up any token changes)
            val retryRequest = request.newBuilder().build()
            chain.proceed(retryRequest)
        } else {
            response
        }
    }

    // --- 401/403 silent re-auth interceptor ---
    private val reAuthInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code in listOf(401, 403)) {
            val handler = silentReAuthHandler
            if (handler != null) {
                SquadLogger.info("Received ${response.code}, attempting silent re-auth", "SquadAPIClient")

                // Run the suspend handler on the current (IO) thread via runBlocking
                val newToken = try {
                    kotlinx.coroutines.runBlocking { handler() }
                } catch (e: Exception) {
                    SquadLogger.error("Silent re-auth handler threw: ${e.message}", "SquadAPIClient", e)
                    null
                }

                if (newToken != null) {
                    accessToken = newToken
                    SquadLogger.info("Silent re-auth succeeded, retrying request", "SquadAPIClient")
                    response.close()

                    val retryRequest = request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    chain.proceed(retryRequest)
                } else {
                    SquadLogger.warn("Silent re-auth failed, surfacing original ${response.code}", "SquadAPIClient")
                    response
                }
            } else {
                response
            }
        } else {
            response
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(sdkVersionInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(rateLimitInterceptor)
        .addInterceptor(reAuthInterceptor)
        .build()

    /** Separate client with longer timeouts for uploads. */
    private val uploadClient = client.newBuilder()
        .connectTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun setAccessToken(token: String) {
        accessToken = token
    }

    fun clearAccessToken() {
        accessToken = null
    }

    // Health

    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/").build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validateToken(): Boolean = withContext(Dispatchers.IO) {
        if (accessToken == null) return@withContext false
        try {
            val request = Request.Builder()
                .url("$baseUrl/v2/sessions/validate")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            true // Network error — assume valid
        }
    }

    // Voice

    suspend fun getVoiceToken(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/v2/voice/token")
            .post("".toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "Failed to get voice token")
            val body = response.body?.string() ?: throw ApiException(0, "Empty response")
            val json = JSONObject(body)
            json.getString("token")
        }
    }

    suspend fun createConnectionsLineRoom(connectionId: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/v2/connections/$connectionId/line-room")
            .post("{}".toRequestBody("application/x-protobuf".toMediaType()))
            .header("Accept", "application/protobuf")
            .build()

        client.newCall(request).execute().use { response ->
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    // Users

    suspend fun getLoggedInUser(): ByteArray = protobufGet("/v2/users/me")

    suspend fun getUser(id: String): ByteArray = protobufGet("/v2/users/$id")

    suspend fun searchUserByEmail(email: String): ByteArray = jsonPost(
        "/v2/users/rpc/searchUsersByEmail",
        JSONObject().apply { put("data", email) }.toString()
    )

    suspend fun searchUsersByPhone(phones: List<String>): ByteArray = jsonPost(
        "/v2/users/rpc/searchUsersByPhone",
        JSONObject().apply { put("data", org.json.JSONArray(phones)) }.toString()
    )

    suspend fun updateLoggedInUser(fields: JSONObject): ByteArray = withContext(Dispatchers.IO) {
        val body = fields.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("$baseUrl/v2/users/me").patch(body).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "Update user failed")
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    suspend fun blockUser(userId: String): ByteArray = jsonPost(
        "/v2/users/rpc/blockUser",
        JSONObject().apply { put("userId", userId) }.toString()
    )

    suspend fun unblockUser(userId: String): ByteArray = jsonPost(
        "/v2/users/rpc/unblockUser",
        JSONObject().apply { put("userId", userId) }.toString()
    )

    suspend fun requestAccountDeletion(): ByteArray = jsonPost("/v2/users/rpc/deleteUser", "{}")

    suspend fun getUserActivity(userId: String): ByteArray = protobufGet("/v2/users/$userId/activity")

    // Connections

    suspend fun getUserConnections(): ByteArray = protobufGet("/v2/users/me/connections")

    suspend fun sendOrAcceptInvite(userId: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/v2/users/me/connections/$userId")
            .post("".toRequestBody("application/x-protobuf".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    // Feed

    suspend fun getFeed(page: Int = 1, pageSize: Int = 20): ByteArray =
        protobufGet("/v2/feed?page=$page&page_size=$pageSize")

    suspend fun createFreestyle(body: ByteArray): ByteArray = protobufPost("/v2/feed/freestyles", body)

    suspend fun createFreestyleReaction(freestyleId: String, body: ByteArray): ByteArray =
        protobufPost("/v2/feed/freestyles/$freestyleId/reactions", body)

    suspend fun getFreestylePrompts(): ByteArray = protobufGet("/v2/feed/freestyles/prompts")

    // Messages

    suspend fun getMessages(connectionId: String): ByteArray =
        protobufGet("/v2/connections/$connectionId/messages")

    suspend fun createMessage(body: ByteArray): ByteArray = protobufPost("/v2/messages", body)

    suspend fun createMessageReaction(messageId: String, body: ByteArray): ByteArray =
        protobufPost("/v2/messages/$messageId/reactions", body)

    // Polls

    suspend fun getActivePolls(): ByteArray = protobufGet("/v2/polls/active")

    suspend fun createOrUpdatePollResponse(pollId: String, body: ByteArray): ByteArray =
        protobufPost("/v2/polls/$pollId/responses", body)

    suspend fun createPollNudge(pollId: String, recipientId: String): ByteArray =
        protobufPost("/v2/polls/$pollId/nudges/$recipientId", byteArrayOf())

    suspend fun createPollResponseReaction(pollId: String, responseId: String, body: ByteArray): ByteArray =
        protobufPost("/v2/polls/$pollId/responses/$responseId/reactions", body)

    // Communities

    suspend fun fetchAllCommunities(): ByteArray = protobufGet("/v2/communities")

    suspend fun updateUserCommunity(communityId: String): ByteArray = jsonPost(
        "/v2/users/me/community",
        JSONObject().apply { put("communityId", communityId) }.toString()
    )

    // Events

    suspend fun getEvents(): ByteArray = protobufGet("/v2/events/active")

    suspend fun getAttendees(eventId: String): ByteArray = protobufGet("/v2/events/$eventId/attendees")

    suspend fun setAttendee(eventId: String, body: ByteArray): ByteArray =
        protobufPost("/v2/events/$eventId/attendee", body)

    // Wallet & Coupons

    suspend fun getWallet(): ByteArray = protobufGet("/v2/wallets/me")

    suspend fun getCoupons(): ByteArray = protobufGet("/v2/coupons/")

    suspend fun redeemCoupon(code: String, body: ByteArray): ByteArray =
        protobufPost("/v2/coupons/$code/redeem", body)

    suspend fun listBrands(): ByteArray = protobufGet("/v2/coupons/brands")

    // Invites

    suspend fun getInvitationCode(code: String): ByteArray = protobufGet("/v2/invites/$code")

    suspend fun redeemInviteCode(code: String): ByteArray = protobufPost("/v2/invites/$code", byteArrayOf())

    // Journey

    suspend fun getCustomerJourney(): ByteArray = protobufGet("/v2/users/me/journey")

    suspend fun addMilestoneToJourney(milestone: String): ByteArray = jsonPost(
        "/v2/users/me/journey/milestones",
        JSONObject().apply { put("milestone", milestone) }.toString()
    )

    // Diagnostics

    suspend fun uploadDiagnostics(body: ByteArray): ByteArray = protobufPost("/v2/diagnostics", body)

    // Partner User Sync

    suspend fun partnerUserSync(
        partnerId: String,
        communityId: String,
        userData: com.squadsports.sdk.PartnerUserData,
    ): ByteArray = withContext(Dispatchers.IO) {
        val bodyObj = JSONObject().apply {
            put("partnerId", partnerId)
            put("communityId", communityId)
            userData.email?.let { put("email", it) }
            userData.phone?.let { put("phone", it) }
            userData.displayName?.let { put("displayName", it) }
            userData.avatarUrl?.let { put("avatarUrl", it) }
            userData.externalUserId?.let { put("externalUserId", it) }
        }
        val body = bodyObj.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/v2/auth/partner-sync")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "Partner sync failed")
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    // SSO

    suspend fun exchangeSSOToken(provider: String, token: String): ByteArray = withContext(Dispatchers.IO) {
        val body = """{"token":"$token"}""".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/v2/auth/sso/$provider")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "SSO exchange failed")
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    // Sponsorships

    /** Fetch active sponsorship placements for the current community. */
    suspend fun getActivePlacements(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/v2/sponsorships/active")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "Failed to fetch placements")
            response.body?.string() ?: """{"placements":[]}"""
        }
    }

    /** Report sponsorship impressions in batch (fire-and-forget). */
    fun reportImpressions(impressions: List<JSONObject>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().apply {
                    put("impressions", org.json.JSONArray(impressions))
                }.toString()
                jsonPost("/v2/sponsorships/impressions", body)
            } catch (_: Exception) {}
        }
    }

    /** Register a push notification token for this device. */
    fun updateDeviceInfo(token: String, platform: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().apply {
                    put("pushToken", token)
                    put("platform", platform)
                    put("sdkVersion", SDK_VERSION)
                }.toString()
                jsonPost("/v2/device-info", body)
            } catch (_: Exception) {}
        }
    }

    // Private helpers

    private suspend fun protobufGet(path: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .get()
            .header("Accept", "application/protobuf")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code !in listOf(400, 404)) {
                throw ApiException(response.code, "Request failed: $path")
            }
            response.body?.bytes() ?: byteArrayOf()
        }
    }
    private suspend fun protobufPost(path: String, body: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .post(body.toRequestBody("application/x-protobuf".toMediaType()))
            .header("Accept", "application/protobuf")
            .header("Content-Type", "application/x-protobuf")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code !in listOf(400, 404)) {
                throw ApiException(response.code, "POST failed: $path")
            }
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    private suspend fun jsonPost(path: String, body: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "POST failed: $path")
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    // JSON request helpers (for auth flow — legacy, kept for compatibility)

    suspend fun postJson(path: String, body: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "POST failed: $path")
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    suspend fun putJson(path: String, body: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .put(body.toRequestBody("application/json".toMediaType()))
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "PUT failed: $path")
            response.body?.bytes() ?: byteArrayOf()
        }
    }
}

class ApiException(val statusCode: Int, message: String) : Exception(message)
