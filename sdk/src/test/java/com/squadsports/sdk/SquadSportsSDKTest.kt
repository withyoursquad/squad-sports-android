package com.squadsports.sdk

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class SquadSportsSDKTest {

    @Before
    fun setUp() {
        SquadSportsSDK.reset()
    }

    // --- Data class / Config tests ---

    @Test
    fun `test SquadSDKConfig creation with defaults`() {
        val config = SquadSDKConfig(
            apiKey = "test-api-key",
            community = CommunityConfig(
                id = "comm-1",
                name = "Test Community",
                primaryColor = "#FF0000",
            ),
        )
        assertEquals("test-api-key", config.apiKey)
        assertEquals(SquadEnvironment.PRODUCTION, config.environment)
        assertNull(config.sso)
        assertNull(config.partnerAuth)
        assertFalse(config.features.squadLine)
        assertTrue(config.features.freestyle)
        assertTrue(config.features.messaging)
        assertTrue(config.features.polls)
        assertTrue(config.features.events)
        assertFalse(config.features.wallet)
    }

    @Test
    fun `test SquadEnvironment base URLs`() {
        assertEquals("https://api-release.withyoursquad.com", SquadEnvironment.PRODUCTION.baseUrl)
        assertEquals("https://api-staging.withyoursquad.com", SquadEnvironment.STAGING.baseUrl)
        assertEquals("https://api-dev.withyoursquad.com", SquadEnvironment.DEVELOPMENT.baseUrl)
    }

    @Test
    fun `test CommunityConfig with optional secondaryColor`() {
        val config = CommunityConfig(
            id = "comm-1",
            name = "Dallas Mavericks",
            primaryColor = "#002B5E",
        )
        assertNull(config.secondaryColor)

        val configWithSecondary = config.copy(secondaryColor = "#B8C4CA")
        assertEquals("#B8C4CA", configWithSecondary.secondaryColor)
    }

    @Test
    fun `test FeatureFlags defaults`() {
        val flags = FeatureFlags()
        assertFalse(flags.squadLine)
        assertTrue(flags.freestyle)
        assertTrue(flags.messaging)
        assertTrue(flags.polls)
        assertTrue(flags.events)
        assertFalse(flags.wallet)
    }

    @Test
    fun `test FeatureFlags can disable features`() {
        val flags = FeatureFlags(squadLine = false, freestyle = false, wallet = false)
        assertFalse(flags.squadLine)
        assertFalse(flags.freestyle)
        assertTrue(flags.messaging)
        assertTrue(flags.polls)
        assertTrue(flags.events)
        assertFalse(flags.wallet)
    }

    @Test
    fun `test SSOConfig creation`() {
        val sso = SSOConfig(
            provider = SSOProvider.TICKETMASTER,
            accessToken = "tm-token-123",
            clientId = "client-abc",
        )
        assertEquals(SSOProvider.TICKETMASTER, sso.provider)
        assertEquals("tm-token-123", sso.accessToken)
        assertEquals("client-abc", sso.clientId)
    }

    @Test
    fun `test SSOProvider values`() {
        assertEquals("ticketmaster", SSOProvider.TICKETMASTER.value)
        assertEquals("oauth2", SSOProvider.OAUTH2.value)
        assertEquals("custom", SSOProvider.CUSTOM.value)
    }

    @Test
    fun `test PartnerUserData creation with all fields`() {
        val userData = PartnerUserData(
            email = "user@test.com",
            phone = "+15551234567",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            externalUserId = "ext-123",
        )
        assertEquals("user@test.com", userData.email)
        assertEquals("+15551234567", userData.phone)
        assertEquals("Test User", userData.displayName)
        assertEquals("https://example.com/avatar.jpg", userData.avatarUrl)
        assertEquals("ext-123", userData.externalUserId)
    }

    @Test
    fun `test PartnerUserData defaults to nulls`() {
        val userData = PartnerUserData()
        assertNull(userData.email)
        assertNull(userData.phone)
        assertNull(userData.displayName)
        assertNull(userData.avatarUrl)
        assertNull(userData.externalUserId)
    }

    @Test
    fun `test PartnerAuthContext creation`() {
        val partnerAuth = PartnerAuthContext(
            partnerId = "acme-sports",
            communityId = "comm-1",
            userData = PartnerUserData(email = "user@test.com"),
        )
        assertEquals("acme-sports", partnerAuth.partnerId)
        assertEquals("comm-1", partnerAuth.communityId)
        assertNotNull(partnerAuth.userData)
        assertEquals("user@test.com", partnerAuth.userData?.email)
    }

    // --- Singleton pattern tests ---

    @Test
    fun `test isInitialized returns false before setup`() {
        assertFalse(SquadSportsSDK.isInitialized)
    }

    @Test(expected = IllegalStateException::class)
    fun `test shared throws when not initialized`() {
        SquadSportsSDK.shared
    }

    @Test
    fun `test reset clears the instance`() {
        // After reset, isInitialized should be false
        SquadSportsSDK.reset()
        assertFalse(SquadSportsSDK.isInitialized)
    }

    @Test(expected = IllegalStateException::class)
    fun `test shared throws after reset`() {
        SquadSportsSDK.reset()
        SquadSportsSDK.shared
    }

    @Test
    fun `test SquadSDKConfig with staging environment`() {
        val config = SquadSDKConfig(
            apiKey = "staging-key",
            environment = SquadEnvironment.STAGING,
            community = CommunityConfig(
                id = "comm-staging",
                name = "Staging Community",
                primaryColor = "#000000",
            ),
        )
        assertEquals(SquadEnvironment.STAGING, config.environment)
        assertEquals("staging-key", config.apiKey)
    }

    @Test
    fun `test SquadSDKConfig with full SSO and partner auth`() {
        val config = SquadSDKConfig(
            apiKey = "full-key",
            environment = SquadEnvironment.DEVELOPMENT,
            community = CommunityConfig(
                id = "comm-dev",
                name = "Dev Community",
                primaryColor = "#123456",
                secondaryColor = "#654321",
            ),
            sso = SSOConfig(
                provider = SSOProvider.OAUTH2,
                accessToken = "oauth-token",
                clientId = "oauth-client",
            ),
            partnerAuth = PartnerAuthContext(
                partnerId = "partner-dev",
                communityId = "comm-dev",
                userData = PartnerUserData(
                    email = "dev@example.com",
                    displayName = "Developer",
                ),
            ),
        )
        assertNotNull(config.sso)
        assertNotNull(config.partnerAuth)
        assertEquals(SSOProvider.OAUTH2, config.sso?.provider)
        assertEquals("partner-dev", config.partnerAuth?.partnerId)
    }
}
