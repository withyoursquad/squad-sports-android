package com.squadsports.sdk.storage

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import kotlinx.coroutines.runBlocking

/**
 * Tests for StorageAdapter using a plain Map-based implementation.
 * EncryptedSharedPreferences requires Android context and is tested
 * via instrumentation tests. This validates the interface contract.
 */
class SquadStorageAdapterTest {

    /** In-memory StorageAdapter for unit testing without Android context. */
    private class MapStorageAdapter : StorageAdapter {
        private val store = mutableMapOf<String, String>()

        override suspend fun getItem(key: String): String? = store[key]
        override suspend fun setItem(key: String, value: String) { store[key] = value }
        override suspend fun removeItem(key: String) { store.remove(key) }
        override suspend fun getAllKeys(): List<String> = store.keys.toList()
    }

    private lateinit var adapter: StorageAdapter

    @Before
    fun setUp() {
        adapter = MapStorageAdapter()
    }

    @Test
    fun `getItem returns null for unknown key`() = runBlocking {
        assertNull(adapter.getItem("nonexistent"))
    }

    @Test
    fun `setItem and getItem round-trip`() = runBlocking {
        adapter.setItem("token", "abc123")
        assertEquals("abc123", adapter.getItem("token"))
    }

    @Test
    fun `setItem overwrites existing value`() = runBlocking {
        adapter.setItem("token", "old")
        adapter.setItem("token", "new")
        assertEquals("new", adapter.getItem("token"))
    }

    @Test
    fun `removeItem deletes the key`() = runBlocking {
        adapter.setItem("token", "abc")
        adapter.removeItem("token")
        assertNull(adapter.getItem("token"))
    }

    @Test
    fun `removeItem on nonexistent key does not crash`() = runBlocking {
        adapter.removeItem("ghost")
        assertNull(adapter.getItem("ghost"))
    }

    @Test
    fun `getAllKeys returns empty list initially`() = runBlocking {
        assertEquals(emptyList<String>(), adapter.getAllKeys())
    }

    @Test
    fun `getAllKeys returns all stored keys`() = runBlocking {
        adapter.setItem("a", "1")
        adapter.setItem("b", "2")
        adapter.setItem("c", "3")
        val keys = adapter.getAllKeys().sorted()
        assertEquals(listOf("a", "b", "c"), keys)
    }

    @Test
    fun `getAllKeys reflects removals`() = runBlocking {
        adapter.setItem("x", "1")
        adapter.setItem("y", "2")
        adapter.removeItem("x")
        assertEquals(listOf("y"), adapter.getAllKeys())
    }

    @Test
    fun `stores empty string values`() = runBlocking {
        adapter.setItem("empty", "")
        assertEquals("", adapter.getItem("empty"))
    }

    @Test
    fun `stores JSON string values`() = runBlocking {
        val json = """{"user":"test","token":"xyz"}"""
        adapter.setItem("session", json)
        assertEquals(json, adapter.getItem("session"))
    }
}
