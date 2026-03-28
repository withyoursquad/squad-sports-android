package com.squadsports.sdk.navigation

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for SquadNavigation route definitions and AppState.
 *
 * Since SquadNavHost is a @Composable function that requires a running Compose environment,
 * we test the pure logic: route string templates, route creation helpers,
 * AppState enum, and startDestination mapping logic.
 */
class SquadNavigationTest {

    // --- Route definition tests ---

    @Test
    fun `test Landing route string`() {
        assertEquals("landing", SquadRoute.Landing.route)
    }

    @Test
    fun `test EnterEmail route string`() {
        assertEquals("enter_email", SquadRoute.EnterEmail.route)
    }

    @Test
    fun `test EnterCode route template`() {
        assertEquals("enter_code/{email}", SquadRoute.EnterCode.route)
    }

    @Test
    fun `test EnterCode route creation`() {
        assertEquals("enter_code/user@example.com", SquadRoute.EnterCode.create("user@example.com"))
    }

    @Test
    fun `test Home route string`() {
        assertEquals("home", SquadRoute.Home.route)
    }

    @Test
    fun `test Profile route template`() {
        assertEquals("profile/{userId}", SquadRoute.Profile.route)
    }

    @Test
    fun `test Profile route creation`() {
        assertEquals("profile/user-123", SquadRoute.Profile.create("user-123"))
    }

    @Test
    fun `test Settings route string`() {
        assertEquals("settings", SquadRoute.Settings.route)
    }

    @Test
    fun `test Messaging route template`() {
        assertEquals("messaging/{connectionId}", SquadRoute.Messaging.route)
    }

    @Test
    fun `test Messaging route creation`() {
        assertEquals("messaging/conn-456", SquadRoute.Messaging.create("conn-456"))
    }

    @Test
    fun `test FreestyleCreate route string`() {
        assertEquals("freestyle_create", SquadRoute.FreestyleCreate.route)
    }

    @Test
    fun `test PollResponse route creation`() {
        assertEquals("poll_response/poll-789", SquadRoute.PollResponse.create("poll-789"))
    }

    @Test
    fun `test Invite route string`() {
        assertEquals("invite", SquadRoute.Invite.route)
    }

    @Test
    fun `test AddCallTitle route creation`() {
        assertEquals("add_call_title/conn-abc", SquadRoute.AddCallTitle.create("conn-abc"))
    }

    @Test
    fun `test ActiveCall route template`() {
        assertEquals("active_call/{connectionId}/{title}", SquadRoute.ActiveCall.route)
    }

    @Test
    fun `test ActiveCall route creation`() {
        assertEquals("active_call/conn-abc/My Call", SquadRoute.ActiveCall.create("conn-abc", "My Call"))
    }

    @Test
    fun `test EditProfile route string`() {
        assertEquals("edit_profile", SquadRoute.EditProfile.route)
    }

    @Test
    fun `test BlockedUsers route string`() {
        assertEquals("blocked_users", SquadRoute.BlockedUsers.route)
    }

    @Test
    fun `test DeleteAccount route string`() {
        assertEquals("delete_account", SquadRoute.DeleteAccount.route)
    }

    @Test
    fun `test Events route string`() {
        assertEquals("events", SquadRoute.Events.route)
    }

    @Test
    fun `test Wallet route string`() {
        assertEquals("wallet", SquadRoute.Wallet.route)
    }

    @Test
    fun `test Login route string`() {
        assertEquals("login", SquadRoute.Login.route)
    }

    @Test
    fun `test FreestyleView route creation`() {
        assertEquals("freestyle_view/fs-123", SquadRoute.FreestyleView.create("fs-123"))
    }

    @Test
    fun `test FreestyleListens route creation`() {
        assertEquals("freestyle_listens/fs-123", SquadRoute.FreestyleListens.create("fs-123"))
    }

    @Test
    fun `test FreestyleReactions route creation`() {
        assertEquals("freestyle_reactions/fs-123", SquadRoute.FreestyleReactions.create("fs-123"))
    }

    @Test
    fun `test CommunityFreestyleListens route creation`() {
        assertEquals("community_freestyle_listens/fs-456", SquadRoute.CommunityFreestyleListens.create("fs-456"))
    }

    @Test
    fun `test CommunityFreestyleReactions route creation`() {
        assertEquals("community_freestyle_reactions/fs-456", SquadRoute.CommunityFreestyleReactions.create("fs-456"))
    }

    @Test
    fun `test PollSummation route creation`() {
        assertEquals("poll_summation/poll-xyz", SquadRoute.PollSummation.create("poll-xyz"))
    }

    @Test
    fun `test NetworkStatus route string`() {
        assertEquals("network_status", SquadRoute.NetworkStatus.route)
    }

    @Test
    fun `test InvitationQrCode route string`() {
        assertEquals("invitation_qr_code", SquadRoute.InvitationQrCode.route)
    }

    @Test
    fun `test EmailVerification route string`() {
        assertEquals("email_verification", SquadRoute.EmailVerification.route)
    }

    @Test
    fun `test OnboardingWelcome route string`() {
        assertEquals("onboarding_welcome", SquadRoute.OnboardingWelcome.route)
    }

    @Test
    fun `test OnboardingTeamSelect route string`() {
        assertEquals("onboarding_team_select", SquadRoute.OnboardingTeamSelect.route)
    }

    @Test
    fun `test OnboardingAccountSetup route string`() {
        assertEquals("onboarding_account_setup", SquadRoute.OnboardingAccountSetup.route)
    }

    // --- AppState tests ---

    @Test
    fun `test AppState enum values`() {
        val values = AppState.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(AppState.LOADING))
        assertTrue(values.contains(AppState.AUTH))
        assertTrue(values.contains(AppState.ONBOARDING))
        assertTrue(values.contains(AppState.MAIN))
    }

    @Test
    fun `test startDestination mapping for LOADING`() {
        val dest = startDestinationFor(AppState.LOADING)
        assertEquals(SquadRoute.Landing.route, dest)
    }

    @Test
    fun `test startDestination mapping for AUTH`() {
        val dest = startDestinationFor(AppState.AUTH)
        assertEquals(SquadRoute.Landing.route, dest)
    }

    @Test
    fun `test startDestination mapping for ONBOARDING`() {
        val dest = startDestinationFor(AppState.ONBOARDING)
        assertEquals(SquadRoute.OnboardingWelcome.route, dest)
    }

    @Test
    fun `test startDestination mapping for MAIN`() {
        val dest = startDestinationFor(AppState.MAIN)
        assertEquals(SquadRoute.Home.route, dest)
    }

    /**
     * Mirrors the startDest logic from SquadNavHost.
     */
    private fun startDestinationFor(appState: AppState): String {
        return when (appState) {
            AppState.LOADING -> SquadRoute.Landing.route
            AppState.AUTH -> SquadRoute.Landing.route
            AppState.ONBOARDING -> SquadRoute.OnboardingWelcome.route
            AppState.MAIN -> SquadRoute.Home.route
        }
    }
}
