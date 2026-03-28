package com.squadsports.sdk.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface StorageAdapter {
    suspend fun getItem(key: String): String?
    suspend fun setItem(key: String, value: String)
    suspend fun removeItem(key: String)
    suspend fun getAllKeys(): List<String>
}

class EncryptedPrefsStorageAdapter(context: Context) : StorageAdapter {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "squad_secure_storage",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun getItem(key: String): String? = prefs.getString(key, null)
    override suspend fun setItem(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override suspend fun removeItem(key: String) = prefs.edit().remove(key).apply()
    override suspend fun getAllKeys(): List<String> = prefs.all.keys.toList()
}
