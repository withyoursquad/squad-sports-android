package com.squadsports.sdk.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.squadsports.sdk.SquadSportsSDKInstance
import com.squadsports.sdk.screens.*
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class SquadRoute(val route: String) {
    object Landing : SquadRoute("landing")
    object EnterEmail : SquadRoute("enter_email")
    object EnterCode : SquadRoute("enter_code/{email}") {
        fun create(email: String) = "enter_code/$email"
    }
    object EmailVerification : SquadRoute("email_verification")
    object OnboardingWelcome : SquadRoute("onboarding_welcome")
    object OnboardingTeamSelect : SquadRoute("onboarding_team_select")
    object OnboardingAccountSetup : SquadRoute("onboarding_account_setup")
    object Home : SquadRoute("home")
    object Profile : SquadRoute("profile/{userId}") {
        fun create(userId: String) = "profile/$userId"
    }
    object Settings : SquadRoute("settings")
    object Messaging : SquadRoute("messaging/{connectionId}") {
        fun create(connectionId: String) = "messaging/$connectionId"
    }
    object FreestyleCreate : SquadRoute("freestyle_create")
    object PollResponse : SquadRoute("poll_response/{pollId}") {
        fun create(pollId: String) = "poll_response/$pollId"
    }
    object Invite : SquadRoute("invite")
    object AddCallTitle : SquadRoute("add_call_title/{connectionId}") {
        fun create(connectionId: String) = "add_call_title/$connectionId"
    }
    object ActiveCall : SquadRoute("active_call/{connectionId}/{title}") {
        fun create(connectionId: String, title: String) = "active_call/$connectionId/$title"
    }
    // Settings sub-screens
    object EditProfile : SquadRoute("edit_profile")
    object BlockedUsers : SquadRoute("blocked_users")
    object DeleteAccount : SquadRoute("delete_account")
    // Events & Wallet
    object Events : SquadRoute("events")
    object Wallet : SquadRoute("wallet")
    // Auth
    object EnterPhone : SquadRoute("enter_phone")
    object Login : SquadRoute("login")
    // Freestyle sub-screens
    object FreestyleView : SquadRoute("freestyle_view/{freestyleId}") {
        fun create(id: String) = "freestyle_view/$id"
    }
    object FreestyleListens : SquadRoute("freestyle_listens/{freestyleId}") {
        fun create(id: String) = "freestyle_listens/$id"
    }
    object FreestyleReactions : SquadRoute("freestyle_reactions/{freestyleId}") {
        fun create(id: String) = "freestyle_reactions/$id"
    }
    object CommunityFreestyleListens : SquadRoute("community_freestyle_listens/{freestyleId}") {
        fun create(id: String) = "community_freestyle_listens/$id"
    }
    object CommunityFreestyleReactions : SquadRoute("community_freestyle_reactions/{freestyleId}") {
        fun create(id: String) = "community_freestyle_reactions/$id"
    }
    // Poll sub-screens
    object PollSummation : SquadRoute("poll_summation/{pollId}") {
        fun create(id: String) = "poll_summation/$id"
    }
    // Network
    object NetworkStatus : SquadRoute("network_status")
    // Invite
    object InvitationQrCode : SquadRoute("invitation_qr_code")
    object QRScanner : SquadRoute("qr_scanner")
}

enum class AppState { LOADING, AUTH, ONBOARDING, MAIN }

@Composable
fun SquadNavHost(sdk: SquadSportsSDKInstance) {
    val navController = rememberNavController()
    var appState by remember { mutableStateOf(AppState.LOADING) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val restored = sdk.restoreSession()
        if (!restored) {
            appState = AppState.AUTH
        } else if (sdk.config.partnerAuth != null) {
            // Partner flow: skip onboarding, go straight to main
            appState = AppState.MAIN
        } else {
            // Check user data for onboarding completeness
            try {
                val userData = sdk.apiClient.getLoggedInUser()
                val json = JSONObject(String(userData))
                val hasName = json.optString("displayName", "").isNotEmpty()
                val hasCommunity = json.optString("community", "").isNotEmpty()
                appState = if (hasName && hasCommunity) AppState.MAIN else AppState.ONBOARDING
            } catch (e: Exception) {
                appState = AppState.MAIN
            }
        }
    }

    val startDest = when (appState) {
        AppState.LOADING -> SquadRoute.Landing.route
        AppState.AUTH -> SquadRoute.Landing.route
        AppState.ONBOARDING -> SquadRoute.OnboardingWelcome.route
        AppState.MAIN -> SquadRoute.Home.route
    }

    if (appState == AppState.LOADING) {
        LoadingScreen(sdk)
        return
    }

    NavHost(navController = navController, startDestination = startDest) {
        // Auth
        composable(SquadRoute.Landing.route) {
            LandingScreen(sdk, navController)
        }
        composable(SquadRoute.EnterEmail.route) {
            EnterEmailScreen(sdk, navController)
        }
        composable(
            SquadRoute.EnterCode.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStack ->
            val email = backStack.arguments?.getString("email") ?: ""
            EnterCodeScreen(sdk, navController, email = email)
        }

        composable(SquadRoute.EmailVerification.route) {
            EmailVerificationScreen(sdk, navController)
        }

        // Onboarding
        composable(SquadRoute.OnboardingWelcome.route) {
            OnboardingWelcomeScreen(sdk, navController)
        }
        composable(SquadRoute.OnboardingTeamSelect.route) {
            OnboardingTeamSelectScreen(sdk, navController)
        }
        composable(SquadRoute.OnboardingAccountSetup.route) {
            OnboardingAccountSetupScreen(sdk, navController)
        }

        // Main
        composable(SquadRoute.Home.route) {
            HomeScreen(sdk, navController)
        }
        composable(
            SquadRoute.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: ""
            ProfileScreen(sdk, navController, userId)
        }
        composable(SquadRoute.Settings.route) {
            SettingsScreen(sdk, navController)
        }
        composable(
            SquadRoute.Messaging.route,
            arguments = listOf(navArgument("connectionId") { type = NavType.StringType })
        ) { backStack ->
            val connectionId = backStack.arguments?.getString("connectionId") ?: ""
            MessagingScreen(sdk, navController, connectionId)
        }
        composable(SquadRoute.FreestyleCreate.route) {
            FreestyleCreateScreen(sdk, navController)
        }
        composable(
            SquadRoute.PollResponse.route,
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) { backStack ->
            val pollId = backStack.arguments?.getString("pollId") ?: ""
            PollResponseScreen(sdk, navController, pollId)
        }
        composable(SquadRoute.Invite.route) {
            InviteScreen(sdk, navController)
        }
        composable(
            SquadRoute.AddCallTitle.route,
            arguments = listOf(navArgument("connectionId") { type = NavType.StringType })
        ) { backStack ->
            val connectionId = backStack.arguments?.getString("connectionId") ?: ""
            AddCallTitleScreen(sdk, navController, connectionId)
        }
        composable(
            SquadRoute.ActiveCall.route,
            arguments = listOf(
                navArgument("connectionId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
            )
        ) { backStack ->
            val connectionId = backStack.arguments?.getString("connectionId") ?: ""
            val title = backStack.arguments?.getString("title") ?: ""
            ActiveCallScreen(sdk, navController, connectionId, title)
        }

        // Settings sub-screens
        composable(SquadRoute.EditProfile.route) {
            EditProfileScreen(sdk, navController)
        }
        composable(SquadRoute.BlockedUsers.route) {
            BlockedUsersScreen(sdk, navController)
        }
        composable(SquadRoute.DeleteAccount.route) {
            DeleteAccountScreen(sdk, navController)
        }

        // Events & Wallet
        composable(SquadRoute.Events.route) {
            EventsScreen(sdk, navController)
        }
        composable(SquadRoute.Wallet.route) {
            WalletScreen(sdk, navController)
        }

        // Auth
        composable(SquadRoute.EnterPhone.route) {
            EnterPhoneScreen(sdk, navController)
        }
        composable(SquadRoute.Login.route) {
            LoginScreen(sdk, navController)
        }

        // Freestyle sub-screens
        composable(SquadRoute.FreestyleView.route, arguments = listOf(navArgument("freestyleId") { type = NavType.StringType })) {
            FreestyleViewScreen(sdk, navController, it.arguments?.getString("freestyleId") ?: "")
        }
        composable(SquadRoute.FreestyleListens.route, arguments = listOf(navArgument("freestyleId") { type = NavType.StringType })) {
            FreestyleListensScreen(sdk, navController, it.arguments?.getString("freestyleId") ?: "")
        }
        composable(SquadRoute.FreestyleReactions.route, arguments = listOf(navArgument("freestyleId") { type = NavType.StringType })) {
            FreestyleReactionsScreen(sdk, navController, it.arguments?.getString("freestyleId") ?: "")
        }
        composable(SquadRoute.CommunityFreestyleListens.route, arguments = listOf(navArgument("freestyleId") { type = NavType.StringType })) {
            CommunityFreestyleListensScreen(sdk, navController, it.arguments?.getString("freestyleId") ?: "")
        }
        composable(SquadRoute.CommunityFreestyleReactions.route, arguments = listOf(navArgument("freestyleId") { type = NavType.StringType })) {
            CommunityFreestyleReactionsScreen(sdk, navController, it.arguments?.getString("freestyleId") ?: "")
        }

        // Poll sub-screens
        composable(SquadRoute.PollSummation.route, arguments = listOf(navArgument("pollId") { type = NavType.StringType })) {
            PollSummationScreen(sdk, navController, it.arguments?.getString("pollId") ?: "")
        }

        // Network
        composable(SquadRoute.NetworkStatus.route) {
            NetworkStatusScreen(sdk, navController)
        }

        // Invite
        composable(SquadRoute.InvitationQrCode.route) {
            InvitationQrCodeScreen(sdk, navController)
        }
        composable(SquadRoute.QRScanner.route) {
            QRScannerScreen(sdk, navController) { code ->
                // Handle scanned code - navigate back with result
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("scanned_code", code)
                navController.popBackStack()
            }
        }
    }
}
