package com.squadsports.sdk.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squadsports.sdk.api.SquadAPIClient
import com.squadsports.sdk.utils.SquadLogger
import org.json.JSONObject
/**
 * Manages authentication for the Squad Sports SDK on Android.
 * Stores tokens securely in EncryptedSharedPreferences.
 */
class SquadAuth(
    context: Context,
    private val apiClient: SquadAPIClient,
) {
    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "squad_sports_sdk_auth",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val storedAccessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val storedUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    val storedCommunityId: String?
        get() = prefs.getString(KEY_COMMUNITY_ID, null)

    val storedPartnerId: String?
        get() = prefs.getString(KEY_PARTNER_ID, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun saveCommunityId(communityId: String) {
        prefs.edit().putString(KEY_COMMUNITY_ID, communityId).apply()
    }

    fun savePartnerId(partnerId: String) {
        prefs.edit().putString(KEY_PARTNER_ID, partnerId).apply()
    }

    /**
     * Send a verification code to the given email.
     */
    suspend fun createSession(email: String): Boolean {
        return try {
            val body = """{"email":"$email"}""".toByteArray()
            val result = apiClient.postJson("/v2/sessions", body)
            val json = JSONObject(String(result))
            val status = json.optInt("status", 0)
            status == 2 || status == 8 // CODE_SENT or EMAIL_CODE_SENT
        } catch (e: Exception) {
            SquadLogger.error("createSession (email) failed: ${e.message}", "SquadAuth", e)
            false
        }
    }

    /**
     * Send a verification code to the given phone number.
     */
    suspend fun createSession(phone: String, isPhone: Boolean = true): Boolean {
        return try {
            val formattedPhone = if (phone.startsWith("+")) phone else "+$phone"
            val body = """{"phone":"$formattedPhone"}""".toByteArray()
            val result = apiClient.postJson("/v2/sessions", body)
            val json = JSONObject(String(result))
            val status = json.optInt("status", 0)
            status == 2 // CODE_SENT
        } catch (e: Exception) {
            SquadLogger.error("createSession (phone) failed: ${e.message}", "SquadAuth", e)
            false
        }
    }

    /**
     * Verify OTP code and obtain access token.
     */
    suspend fun fulfillSession(email: String? = null, phone: String? = null, code: String): Boolean {
        return try {
            val bodyMap = mutableMapOf<String, String>()
            email?.let { bodyMap["email"] = it }
            phone?.let { bodyMap["phone"] = if (it.startsWith("+")) it else "+$it" }
            bodyMap["code"] = code

            val body = JSONObject(bodyMap as Map<*, *>).toString().toByteArray()
            val result = apiClient.putJson("/v2/sessions", body)
            val json = JSONObject(String(result))

            val accessToken = json.optString("accessToken", json.optString("access_token", ""))
            val userId = json.optString("userId", json.optString("user_id", ""))

            if (accessToken.isNotEmpty()) {
                apiClient.setAccessToken(accessToken)
                prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_USER_ID, userId)
                    .apply()
                true
            } else false
        } catch (e: Exception) {
            SquadLogger.error("fulfillSession failed: ${e.message}", "SquadAuth", e)
            false
        }
    }

    /**
     * Clear all auth state.
     */
    fun logout() {
        apiClient.clearAccessToken()
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_COMMUNITY_ID = "community_id"
        private const val KEY_PARTNER_ID = "partner_id"
    }
}
