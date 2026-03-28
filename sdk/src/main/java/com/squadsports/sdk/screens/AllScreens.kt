@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
package com.squadsports.sdk.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.squadsports.sdk.SquadSportsSDKInstance
import com.squadsports.sdk.navigation.SquadRoute
import com.squadsports.sdk.theme.parseColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
// kotlin.math imports available if needed for circular layouts
import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Brush
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

private val DarkBg = Color(0xFF111111)
private val CardBg = Color(0xFF212121)
private val Gray3 = Color(0xFF353535)
private val Gray5 = Color(0xFF3D3D3D)
private val GrayText = Color(0xFF8A8A8A)
private val DividerColor = Color(0xFF353535)
private val ErrorColor = Color(0xFFFF955C)
private val Purple1 = Color(0xFF6E82E7)
private val Orange1 = Color(0xFFFF955C)
private val SquadCircleColor = Color(0xFFFAFAFA)
private val Gray6 = Color(0xFF8A8A8A)
private val SquadGreen = Color(0xFF11EC0F)
private val Gold = Color(0xFFD1C282)
private val Red = Color(0xFFFF4478)

// ============================================================
// LOADING / SPLASH
// ============================================================

@Composable
fun LoadingScreen(sdk: SquadSportsSDKInstance) {
    val primary = parseColor(sdk.config.community.primaryColor)

    // Animated decorative circles
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val circle1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "c1s"
    )
    val circle1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "c1a"
    )
    val circle2Scale by infiniteTransition.animateFloat(
        initialValue = 1.1f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOut), RepeatMode.Reverse),
        label = "c2s"
    )
    val circle2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOut), RepeatMode.Reverse),
        label = "c2a"
    )
    val circle3Scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOut), RepeatMode.Reverse),
        label = "c3s"
    )
    val circle3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.08f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOut), RepeatMode.Reverse),
        label = "c3a"
    )

    // Fade-in on mount
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800), label = "fadeIn"
    )

    Box(
        Modifier.fillMaxSize().background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Decorative circle 1 - top right
        Box(
            Modifier
                .offset(x = 80.dp, y = (-120).dp)
                .scale(circle1Scale)
                .size(200.dp)
                .background(primary.copy(alpha = circle1Alpha), CircleShape)
        )
        // Decorative circle 2 - bottom left
        Box(
            Modifier
                .offset(x = (-100).dp, y = 140.dp)
                .scale(circle2Scale)
                .size(260.dp)
                .background(Purple1.copy(alpha = circle2Alpha), CircleShape)
        )
        // Decorative circle 3 - center right
        Box(
            Modifier
                .offset(x = 120.dp, y = 40.dp)
                .scale(circle3Scale)
                .size(160.dp)
                .background(Orange1.copy(alpha = circle3Alpha), CircleShape)
        )

        // Center content
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                "squad",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = contentAlpha),
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White.copy(alpha = contentAlpha * 0.7f),
                strokeWidth = 2.dp
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Powered by Squad Sports",
                fontSize = 12.sp,
                color = GrayText.copy(alpha = contentAlpha),
                modifier = Modifier.padding(bottom = 48.dp)
            )
        }
    }
}

// ============================================================
// LANDING
// ============================================================

@Composable
fun LandingScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)

    Column(
        Modifier.fillMaxSize().background(DarkBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            Modifier.size(100.dp).border(3.dp, primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = primary)
        }
        Spacer(Modifier.height(16.dp))
        Text("Squad Sports", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Connect with your squad", color = GrayText)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { nav.navigate(SquadRoute.EnterEmail.route) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary),
        ) {
            Text("Get Started", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0A0A))
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { nav.navigate(SquadRoute.EnterEmail.route) }) {
            Text("I already have an account", color = Color.White)
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ============================================================
// ENTER EMAIL
// ============================================================

@Composable
fun EnterEmailScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var firstName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isValid = firstName.isNotBlank() && email.contains("@") && email.contains(".")

    Column(Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        IconButton(onClick = { nav.popBackStack() }) {
            Text("<", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Create Your Account", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(24.dp))

        SquadOutlinedField(firstName, { firstName = it }, "First name")
        Spacer(Modifier.height(16.dp))
        SquadOutlinedField(email, { email = it; error = null }, "Email address",
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Go,
            onAction = {
                if (isValid) scope.launch {
                    loading = true
                    if (sdk.auth.createSession(email = email)) {
                        nav.navigate(SquadRoute.EnterCode.create(email))
                    } else {
                        error = "Failed to send code"
                    }
                    loading = false
                }
            })

        error?.let {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.background(ErrorColor.copy(alpha = 0.14f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Box(Modifier.width(4.dp).height(16.dp).background(ErrorColor, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(it, color = ErrorColor, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    if (sdk.auth.createSession(email = email)) {
                        nav.navigate(SquadRoute.EnterCode.create(email))
                    } else { error = "Failed to send code" }
                    loading = false
                }
            },
            enabled = isValid && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                disabledContainerColor = CardBg,
            ),
        ) {
            Text(if (loading) "Sending..." else "Send Code",
                fontWeight = FontWeight.SemiBold, color = if (isValid) Color(0xFF0A0A0A) else GrayText)
        }
        Spacer(Modifier.height(16.dp))
        Text("By tapping \"Send Code\" you agree to the Terms & Conditions and Privacy Policy",
            color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// ============================================================
// ENTER CODE
// ============================================================

@Composable
fun EnterCodeScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, email: String) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            kotlinx.coroutines.delay(1000)
            resendCooldown--
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        // Progress bar
        Box(Modifier.fillMaxWidth().height(4.dp).background(DividerColor)) {
            Box(Modifier.fillMaxWidth(0.16f).height(4.dp).background(Color.White))
        }
        Spacer(Modifier.height(16.dp))
        IconButton(onClick = { nav.popBackStack() }) {
            Text("<", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Enter Verification Code", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(24.dp))

        SquadOutlinedField(code, { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
            "6-digit code", keyboardType = KeyboardType.Number, imeAction = ImeAction.Go,
            onAction = {
                if (code.length == 6) scope.launch {
                    loading = true
                    if (sdk.auth.fulfillSession(email = email, code = code)) {
                        nav.navigate(SquadRoute.Home.route) { popUpTo(0) { inclusive = true } }
                    } else { error = "Invalid code" }
                    loading = false
                }
            })

        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = {
                if (resendCooldown == 0) {
                    scope.launch { sdk.auth.createSession(email = email); code = "" }
                    resendCooldown = 60
                }
            },
            enabled = resendCooldown == 0
        ) {
            Row {
                Text("Didn't receive a code? ", color = GrayText)
                Text(
                    if (resendCooldown > 0) "Resend (${resendCooldown}s)" else "Resend",
                    color = if (resendCooldown > 0) GrayText else Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = ErrorColor, fontSize = 14.sp) }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    if (sdk.auth.fulfillSession(email = email, code = code)) {
                        nav.navigate(SquadRoute.Home.route) { popUpTo(0) { inclusive = true } }
                    } else { error = "Invalid code" }
                    loading = false
                }
            },
            enabled = code.length == 6 && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = CardBg),
        ) {
            Text(if (loading) "Verifying..." else "Verify", fontWeight = FontWeight.SemiBold,
                color = if (code.length == 6) Color(0xFF151515) else GrayText)
        }
    }
}

// ============================================================
// ONBOARDING
// ============================================================

// ============================================================
// EMAIL VERIFICATION
// ============================================================

@Composable
fun EmailVerificationScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { nav.popBackStack() }, Modifier.align(Alignment.Start).padding(16.dp)) {
            Text("<", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(80.dp).background(Color(0xFF6E82E7), CircleShape), contentAlignment = Alignment.Center) {
            Text("@", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Check Your Email", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text("We sent a verification link to your email address.\nTap the link to continue.",
            color = GrayText, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(Modifier.size(16.dp), color = Color(0xFF6E82E7), strokeWidth = 2.dp)
            Text("Waiting for verification...", color = GrayText, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = {}) { Row { Text("Didn't receive the email? ", color = GrayText); Text("Resend", color = Color.White, fontWeight = FontWeight.Medium) } }
        OutlinedButton(onClick = { nav.popBackStack() }, Modifier.fillMaxWidth().padding(24.dp).height(48.dp), shape = RoundedCornerShape(8.dp)) {
            Text("Use verification code instead", color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ============================================================
// ONBOARDING
// ============================================================

@Composable
fun OnboardingWelcomeScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    Column(Modifier.fillMaxSize().background(DarkBg).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(120.dp).border(3.dp, primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("S", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = primary)
        }
        Spacer(Modifier.height(32.dp))
        Text("Welcome to Squad", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text("Connect with your squad, share freestyles, and experience sports together.",
            color = GrayText, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { nav.navigate(SquadRoute.OnboardingTeamSelect.route) },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary),
        ) { Text("Get Started", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0A0A)) }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun OnboardingTeamSelectScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var selected by remember { mutableStateOf<String?>(null) }
    var communities by remember { mutableStateOf(listOf<Triple<String, String, String>>()) } // id, name, color
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // If partner has a fixed community, auto-select and skip
        val partnerCommunityId = sdk.config.partnerAuth?.communityId
        if (partnerCommunityId != null) {
            try {
                val body = """{"id":"$partnerCommunityId"}""".toByteArray()
                sdk.apiClient.postJson("/v2/users/me/community", body)
            } catch (_: Exception) {}
            nav.navigate(SquadRoute.OnboardingAccountSetup.route)
            return@LaunchedEffect
        }

        try {
            val data = sdk.apiClient.fetchAllCommunities()
            val json = org.json.JSONObject(String(data))
            val arr = json.optJSONArray("communities")
            if (arr != null) {
                communities = (0 until arr.length()).map { i ->
                    val c = arr.getJSONObject(i)
                    Triple(c.optString("id"), c.optString("name"), c.optString("color", "#6E82E7"))
                }
            }
        } catch (_: Exception) {
            communities = listOf(Triple("default", "Squad Default", "#6E82E7"))
        }
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Pick Your Team") { nav.popBackStack() }
        Column(Modifier.weight(1f).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Choose your team to join their community", color = GrayText)
            Spacer(Modifier.height(16.dp))
            communities.forEach { (id, name, color) ->
                val c = parseColor(color)
                val isSelected = selected == id
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (isSelected) c.copy(alpha = 0.1f) else CardBg, RoundedCornerShape(12.dp))
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) c else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { selected = id }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(32.dp).background(c, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (isSelected) Text("v", color = c, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            onClick = { nav.navigate(SquadRoute.OnboardingAccountSetup.route) },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (selected != null) primary else CardBg),
        ) { Text("Continue", fontWeight = FontWeight.SemiBold, color = if (selected != null) Color(0xFF0A0A0A) else GrayText) }
    }
}

@Composable
fun OnboardingAccountSetupScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(DarkBg).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenHeader("Set Up Profile") { nav.popBackStack() }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(100.dp).background(Color(0xFF6E82E7), CircleShape), contentAlignment = Alignment.Center) {
            Text(if (name.isNotEmpty()) name.first().uppercase() else "?", fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Text("Tap to add a photo", color = GrayText, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(32.dp))
        Text("What should we call you?", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(12.dp))
        SquadOutlinedField(name, { name = it }, "Your name")
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { nav.navigate(SquadRoute.Home.route) { popUpTo(0) { inclusive = true } } },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (name.isNotBlank()) primary else CardBg),
        ) { Text("Complete Setup", fontWeight = FontWeight.SemiBold, color = if (name.isNotBlank()) Color(0xFF0A0A0A) else GrayText) }
    }
}

// ============================================================
// HOME
// ============================================================

private data class SquadMember(val id: String, val name: String, val avatarInitial: String)
private data class FeedItem(val id: String, val authorName: String, val authorInitial: String, val type: String, val preview: String, val timestamp: String, val isNew: Boolean)
private data class PollItem(val id: String, val question: String, val optionCount: Int)

@Composable
fun HomeScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("") }
    var userInitial by remember { mutableStateOf("?") }
    var userId by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(listOf<SquadMember>()) }
    var feedItems by remember { mutableStateOf(listOf<FeedItem>()) }
    var polls by remember { mutableStateOf(listOf<PollItem>()) }
    var loading by remember { mutableStateOf(true) }
    var showPostOnboardingOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Load user
                val userData = sdk.apiClient.getLoggedInUser()
                val userJson = JSONObject(String(userData))
                val user = userJson.optJSONObject("user") ?: userJson
                userName = user.optString("firstName", user.optString("name", ""))
                userInitial = if (userName.isNotEmpty()) userName.first().uppercase() else "?"
                userId = user.optString("id", "")

                // Load connections
                val connData = sdk.apiClient.getUserConnections()
                val connJson = JSONObject(String(connData))
                val connArr = connJson.optJSONArray("connections") ?: JSONArray()
                val memberList = mutableListOf<SquadMember>()
                for (i in 0 until connArr.length()) {
                    val c = connArr.getJSONObject(i)
                    val connUser = c.optJSONObject("user") ?: c
                    val n = connUser.optString("firstName", connUser.optString("name", "User"))
                    memberList.add(SquadMember(
                        id = connUser.optString("id", ""),
                        name = n,
                        avatarInitial = if (n.isNotEmpty()) n.first().uppercase() else "?"
                    ))
                }
                members = memberList

                // Show overlay if first time and has members
                if (memberList.isNotEmpty()) {
                    showPostOnboardingOverlay = true
                }

                // Load feed
                val feedData = sdk.apiClient.getFeed()
                val feedJson = JSONObject(String(feedData))
                val feedArr = feedJson.optJSONArray("items") ?: feedJson.optJSONArray("feed") ?: JSONArray()
                val feedList = mutableListOf<FeedItem>()
                for (i in 0 until feedArr.length()) {
                    val f = feedArr.getJSONObject(i)
                    val author = f.optJSONObject("author")
                    val aName = author?.optString("firstName", author.optString("name", "User")) ?: "User"
                    feedList.add(FeedItem(
                        id = f.optString("id", "$i"),
                        authorName = aName,
                        authorInitial = if (aName.isNotEmpty()) aName.first().uppercase() else "?",
                        type = f.optString("type", "freestyle"),
                        preview = f.optString("title", f.optString("prompt", "Shared a freestyle")),
                        timestamp = f.optString("createdAt", ""),
                        isNew = i < 3
                    ))
                }
                feedItems = feedList

                // Load polls
                val pollData = sdk.apiClient.getActivePolls()
                val pollJson = JSONObject(String(pollData))
                val pollArr = pollJson.optJSONArray("polls") ?: JSONArray()
                val pollList = mutableListOf<PollItem>()
                for (i in 0 until pollArr.length()) {
                    val p = pollArr.getJSONObject(i)
                    pollList.add(PollItem(
                        id = p.optString("id", "$i"),
                        question = p.optString("question", "Poll"),
                        optionCount = p.optJSONArray("options")?.length() ?: 2
                    ))
                }
                polls = pollList
            } catch (_: Exception) {}
            loading = false
        }
    }

    Box(Modifier.fillMaxSize().background(DarkBg)) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                // Header: avatar + invite button
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar - tap to go to profile
                        Box(
                            Modifier
                                .size(44.dp)
                                .background(Purple1, CircleShape)
                                .clickable {
                                    if (userId.isNotEmpty()) nav.navigate(SquadRoute.Profile.create(userId))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Spacer(Modifier.weight(1f))

                        // Invite button
                        Button(
                            onClick = { nav.navigate(SquadRoute.Invite.route) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text("+  Invite", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                // Squad Circle Display
                item {
                    SquadCircleDisplay(
                        members = members.take(5),
                        totalCount = members.size,
                        primary = primary,
                        onMemberClick = { memberId ->
                            nav.navigate(SquadRoute.Profile.create(memberId))
                        }
                    )
                }

                // "More Of Your Shifters" - overflow members (> 5)
                if (members.size > 5) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "More Of Your Shifters",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(members.drop(5)) { member ->
                                Column(
                                    Modifier.clickable { nav.navigate(SquadRoute.Profile.create(member.id)) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier.size(56.dp).background(Gray5, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(member.avatarInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        member.name, fontSize = 11.sp, color = GrayText,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 60.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Polls
                if (polls.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Active Polls",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(polls) { poll ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .background(CardBg, RoundedCornerShape(12.dp))
                                .clickable { nav.navigate(SquadRoute.PollResponse.create(poll.id)) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(36.dp).background(Purple1.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("?", color = Purple1, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(poll.question, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text("${poll.optionCount} options", color = GrayText, fontSize = 12.sp)
                            }
                            Text(">", color = GrayText, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Feed section
                if (feedItems.isNotEmpty()) {
                    // "What's New" header
                    val newItems = feedItems.filter { it.isNew }
                    val olderItems = feedItems.filter { !it.isNew }

                    if (newItems.isNotEmpty()) {
                        item { FeedSectionDivider("What's New") }
                        items(newItems) { item -> FeedCard(item, primary, nav) }
                    }

                    if (olderItems.isNotEmpty()) {
                        item { FeedSectionDivider("Older") }
                        items(olderItems) { item -> FeedCard(item, primary, nav) }
                    }
                } else if (members.isEmpty()) {
                    // Empty state
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Your circle is listening.\nDrop your first take.",
                                    color = GrayText, textAlign = TextAlign.Center,
                                    fontSize = 16.sp, lineHeight = 24.sp
                                )
                                Spacer(Modifier.height(20.dp))
                                Button(
                                    onClick = { nav.navigate(SquadRoute.FreestyleCreate.route) },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                                ) {
                                    Text("Create Freestyle", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }

        // Post-onboarding overlay
        if (showPostOnboardingOverlay) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showPostOnboardingOverlay = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .padding(32.dp)
                        .background(CardBg, RoundedCornerShape(20.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You're In!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Your squad is set up. Start connecting with your shifters.",
                        color = GrayText, textAlign = TextAlign.Center, fontSize = 15.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { showPostOnboardingOverlay = false },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Let's Go", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadCircleDisplay(
    members: List<SquadMember>,
    totalCount: Int,
    primary: Color,
    onMemberClick: (String) -> Unit
) {
    val sizes = listOf(88.dp, 80.dp, 72.dp, 64.dp, 56.dp)
    val fontSizes = listOf(32.sp, 28.sp, 26.sp, 22.sp, 20.sp)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle border
        Canvas(Modifier.size(240.dp)) {
            drawCircle(
                color = SquadCircleColor,
                radius = size.minDimension / 2,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            )
        }

        if (members.isEmpty()) {
            // Show 5 dashed placeholders in circular arrangement
            val placeholderPositions = listOf(
                Pair(0.dp, (-80).dp),    // top
                Pair((-70).dp, (-20).dp), // left
                Pair(70.dp, (-20).dp),   // right
                Pair((-50).dp, 60.dp),   // bottom-left
                Pair(50.dp, 60.dp)       // bottom-right
            )
            placeholderPositions.forEach { (x, y) ->
                Box(
                    Modifier.offset(x = x, y = y).size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(48.dp)) {
                        drawCircle(
                            color = SquadCircleColor.copy(alpha = 0.3f),
                            radius = size.minDimension / 2,
                            style = Stroke(
                                width = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )
                        )
                    }
                    Text("+", color = SquadCircleColor.copy(alpha = 0.4f), fontSize = 20.sp)
                }
            }
        } else {
            // Arrange members in circular layout: top / center row / bottom row
            when (members.size) {
                1 -> {
                    // Single member centered
                    SquadMemberAvatar(members[0], sizes[0], fontSizes[0], onMemberClick, Modifier)
                }
                2 -> {
                    SquadMemberAvatar(members[0], sizes[0], fontSizes[0], onMemberClick, Modifier.offset(y = (-40).dp))
                    SquadMemberAvatar(members[1], sizes[1], fontSizes[1], onMemberClick, Modifier.offset(y = 40.dp))
                }
                3 -> {
                    SquadMemberAvatar(members[0], sizes[0], fontSizes[0], onMemberClick, Modifier.offset(y = (-50).dp))
                    SquadMemberAvatar(members[1], sizes[1], fontSizes[1], onMemberClick, Modifier.offset(x = (-50).dp, y = 30.dp))
                    SquadMemberAvatar(members[2], sizes[2], fontSizes[2], onMemberClick, Modifier.offset(x = 50.dp, y = 30.dp))
                }
                4 -> {
                    SquadMemberAvatar(members[0], sizes[0], fontSizes[0], onMemberClick, Modifier.offset(y = (-60).dp))
                    SquadMemberAvatar(members[1], sizes[1], fontSizes[1], onMemberClick, Modifier.offset(x = (-60).dp, y = 0.dp))
                    SquadMemberAvatar(members[2], sizes[2], fontSizes[2], onMemberClick, Modifier.offset(x = 60.dp, y = 0.dp))
                    SquadMemberAvatar(members[3], sizes[3], fontSizes[3], onMemberClick, Modifier.offset(y = 60.dp))
                }
                else -> {
                    // 5 members: top, center-left, center-right, bottom-left, bottom-right
                    SquadMemberAvatar(members[0], sizes[0], fontSizes[0], onMemberClick, Modifier.offset(y = (-70).dp))
                    SquadMemberAvatar(members[1], sizes[1], fontSizes[1], onMemberClick, Modifier.offset(x = (-65).dp, y = (-10).dp))
                    SquadMemberAvatar(members[2], sizes[2], fontSizes[2], onMemberClick, Modifier.offset(x = 65.dp, y = (-10).dp))
                    SquadMemberAvatar(members[3], sizes[3], fontSizes[3], onMemberClick, Modifier.offset(x = (-45).dp, y = 60.dp))
                    SquadMemberAvatar(members[4], sizes[4], fontSizes[4], onMemberClick, Modifier.offset(x = 45.dp, y = 60.dp))
                }
            }

            // Show remaining count if more than displayed
            if (totalCount > 5) {
                Box(
                    Modifier
                        .offset(x = 90.dp, y = 70.dp)
                        .size(32.dp)
                        .background(Gray5, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+${totalCount - 5}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Squad count label
    Text(
        "${members.size} Shifter${if (members.size != 1) "s" else ""} in Your Circle",
        fontSize = 14.sp, color = GrayText,
        modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SquadMemberAvatar(
    member: SquadMember,
    size: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: (String) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier
            .size(size)
            .background(Purple1, CircleShape)
            .border(2.dp, DarkBg, CircleShape)
            .clickable { onClick(member.id) },
        contentAlignment = Alignment.Center
    ) {
        Text(member.avatarInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = fontSize)
    }
}

@Composable
private fun FeedSectionDivider(title: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(Modifier.weight(1f), color = Gray3)
        Text(
            title, color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(Modifier.weight(1f), color = Gray3)
    }
}

@Composable
private fun FeedCard(item: FeedItem, primary: Color, nav: NavHostController) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(Purple1, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.authorInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.authorName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                item.preview, color = GrayText, fontSize = 13.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionBtn(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick, modifier.height(72.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CardBg)) {
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ============================================================
// PROFILE, SETTINGS, MESSAGING, FREESTYLE, POLLS, INVITE, CALL
// ============================================================

@Composable fun ProfileScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, userId: String) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val scope = rememberCoroutineScope()

    var profileName by remember { mutableStateOf("") }
    var profileInitial by remember { mutableStateOf("?") }
    var isOwnProfile by remember { mutableStateOf(false) }
    var isInCircle by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var loadTimedOut by remember { mutableStateOf(false) }
    var headerExpanded by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Activity stats
    var messagesSent by remember { mutableIntStateOf(0) }
    var freestylesCount by remember { mutableIntStateOf(0) }
    var totalShifters by remember { mutableIntStateOf(0) }
    var pollResponses by remember { mutableIntStateOf(0) }

    // Connections (for Messages tab)
    var connections by remember { mutableStateOf(listOf<SquadMember>()) }

    // Freestyles (for Freestyles tab)
    var freestyles by remember { mutableStateOf(listOf<FeedItem>()) }

    val tabTitles = listOf("Messages", "Freestyles", "Activity")

    // Loading timeout with retry
    LaunchedEffect(Unit) {
        delay(10000)
        if (loading) loadTimedOut = true
    }

    fun loadData() {
        loading = true
        loadTimedOut = false
        scope.launch {
            try {
                // Check if own profile
                val meData = sdk.apiClient.getLoggedInUser()
                val meJson = JSONObject(String(meData))
                val me = meJson.optJSONObject("user") ?: meJson
                val myId = me.optString("id", "")
                isOwnProfile = myId == userId

                // Load target user
                val userData = sdk.apiClient.getUser(userId)
                val userJson = JSONObject(String(userData))
                val user = userJson.optJSONObject("user") ?: userJson
                profileName = user.optString("firstName", user.optString("name", "User"))
                profileInitial = if (profileName.isNotEmpty()) profileName.first().uppercase() else "?"

                // Load connections
                val connData = sdk.apiClient.getUserConnections()
                val connJson = JSONObject(String(connData))
                val connArr = connJson.optJSONArray("connections") ?: JSONArray()
                val connList = mutableListOf<SquadMember>()
                for (i in 0 until connArr.length()) {
                    val c = connArr.getJSONObject(i)
                    val connUser = c.optJSONObject("user") ?: c
                    val connId = connUser.optString("id", "")
                    val n = connUser.optString("firstName", connUser.optString("name", "User"))
                    connList.add(SquadMember(connId, n, if (n.isNotEmpty()) n.first().uppercase() else "?"))
                    if (connId == userId) isInCircle = true
                }
                connections = connList
                totalShifters = connList.size

                // Load activity
                try {
                    val actData = sdk.apiClient.getUserActivity(userId)
                    val actJson = JSONObject(String(actData))
                    val activity = actJson.optJSONObject("activity") ?: actJson
                    messagesSent = activity.optInt("messagesSent", activity.optInt("messages", 0))
                    freestylesCount = activity.optInt("freestyles", activity.optInt("freestylesCount", 0))
                    pollResponses = activity.optInt("pollResponses", activity.optInt("polls", 0))
                } catch (_: Exception) {}

                // Load feed for freestyles
                try {
                    val feedData = sdk.apiClient.getFeed()
                    val feedJson = JSONObject(String(feedData))
                    val feedArr = feedJson.optJSONArray("items") ?: feedJson.optJSONArray("feed") ?: JSONArray()
                    val fList = mutableListOf<FeedItem>()
                    for (i in 0 until feedArr.length()) {
                        val f = feedArr.getJSONObject(i)
                        val author = f.optJSONObject("author")
                        val authorId = author?.optString("id", "") ?: ""
                        if (isOwnProfile || authorId == userId) {
                            val aName = author?.optString("firstName", author.optString("name", "User")) ?: profileName
                            fList.add(FeedItem(
                                id = f.optString("id", "$i"),
                                authorName = aName,
                                authorInitial = if (aName.isNotEmpty()) aName.first().uppercase() else "?",
                                type = f.optString("type", "freestyle"),
                                preview = f.optString("title", f.optString("prompt", "Shared a freestyle")),
                                timestamp = f.optString("createdAt", ""),
                                isNew = i < 3
                            ))
                        }
                    }
                    freestyles = fList
                } catch (_: Exception) {}
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(userId) { loadData() }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar: back + settings/ellipsis
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Text("<", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.weight(1f))
            if (isOwnProfile) {
                IconButton(onClick = { nav.navigate(SquadRoute.Settings.route) }) {
                    Text("...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (loadTimedOut) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Taking longer than expected...", color = GrayText, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { loadData() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary)
                        ) {
                            Text("Retry", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        } else {
            // Expandable header
            AnimatedVisibility(
                visible = headerExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(96.dp).background(Purple1, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profileInitial, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${profileName}'s Lab",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Collapse/expand toggle
            Row(
                Modifier.fillMaxWidth().clickable { headerExpanded = !headerExpanded }.padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (headerExpanded) "^" else "v",
                    color = GrayText, fontSize = 14.sp
                )
            }

            // Check if viewing someone else's profile and not in their circle
            if (!isOwnProfile && !isInCircle) {
                Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "You're not in ${profileName}'s Circle yet",
                            color = GrayText, fontSize = 16.sp, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    try { sdk.apiClient.sendOrAcceptInvite(userId) } catch (_: Exception) {}
                                    isInCircle = true
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary)
                        ) {
                            Text("Connect", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // TabRow
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBg,
                    contentColor = Color.White,
                    divider = { Divider(color = Gray3) }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color.White else GrayText,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    // Messages tab - show connections to message
                    0 -> {
                        if (connections.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No connections yet", color = GrayText)
                            }
                        } else {
                            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
                                items(connections) { conn ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(CardBg, RoundedCornerShape(12.dp))
                                            .clickable { nav.navigate(SquadRoute.Messaging.create(conn.id)) }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier.size(44.dp).background(Purple1, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(conn.avatarInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(conn.name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                        Text(">", color = GrayText, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Freestyles tab
                    1 -> {
                        if (freestyles.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No freestyles yet", color = GrayText, fontSize = 15.sp)
                                    if (isOwnProfile) {
                                        Spacer(Modifier.height(16.dp))
                                        Button(
                                            onClick = { nav.navigate(SquadRoute.FreestyleCreate.route) },
                                            shape = RoundedCornerShape(24.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = primary)
                                        ) {
                                            Text("Create One", color = Color(0xFF0A0A0A), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        } else {
                            val newFreestyles = freestyles.filter { it.isNew }
                            val olderFreestyles = freestyles.filter { !it.isNew }

                            LazyColumn(
                                Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (newFreestyles.isNotEmpty()) {
                                    item { FeedSectionDivider("What's New") }
                                    items(newFreestyles) { item ->
                                        FreestyleCard(item)
                                    }
                                }
                                if (olderFreestyles.isNotEmpty()) {
                                    item { FeedSectionDivider("Older") }
                                    items(olderFreestyles) { item ->
                                        FreestyleCard(item)
                                    }
                                }
                            }
                        }
                    }

                    // Activity tab - 2x2 grid
                    2 -> {
                        Column(
                            Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(8.dp))
                            // Row 1: Messages Sent | Freestyles
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ActivityStatCard("Messages Sent", messagesSent.toString(), Purple1, Modifier.weight(1f))
                                ActivityStatCard("Freestyles", freestylesCount.toString(), Orange1, Modifier.weight(1f))
                            }
                            // Row 2: Total Shifters | Poll Responses
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ActivityStatCard("Total Shifters", totalShifters.toString(), Color(0xFF4ECDC4), Modifier.weight(1f))
                                ActivityStatCard("Poll Responses", pollResponses.toString(), Color(0xFFE7A06E), Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityStatCard(label: String, value: String, accentColor: Color, modifier: Modifier) {
    Column(
        modifier
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = accentColor)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 13.sp, color = GrayText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FreestyleCard(item: FeedItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(Purple1, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.authorInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.authorName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                item.preview, color = GrayText, fontSize = 13.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = GrayText)
    }
}

@Composable fun SettingsScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Settings") { nav.popBackStack() }
        LazyColumn(Modifier.padding(24.dp)) {
            item { SettingsSection("Account") {
                Row(Modifier.fillMaxWidth().clickable { nav.navigate(SquadRoute.EditProfile.route) }.padding(14.dp)) { Text("Edit Profile", color = Color.White, modifier = Modifier.weight(1f)); Text(">", color = GrayText) }
                Row(Modifier.fillMaxWidth().padding(14.dp)) { Text("Community", color = Color.White, modifier = Modifier.weight(1f)); Text(sdk.config.community.name, color = GrayText); Text(" >", color = GrayText) }
                Row(Modifier.fillMaxWidth().clickable { nav.navigate(SquadRoute.BlockedUsers.route) }.padding(14.dp)) { Text("Blocked Users", color = Color.White, modifier = Modifier.weight(1f)); Text(">", color = GrayText) }
            } }
            item { SettingsSection("About") { SettingsRow("Terms & Conditions"); SettingsRow("Privacy Policy"); SettingsRow("SDK Version", "1.6.0") } }
            item {
                TextButton(onClick = { sdk.auth.logout(); nav.navigate(SquadRoute.Landing.route) { popUpTo(0) { inclusive = true } } }) {
                    Text("Log Out", color = Color(0xFFFF4478))
                }
                TextButton(onClick = { nav.navigate(SquadRoute.DeleteAccount.route) }) {
                    Text("Delete Account", color = Color(0xFFFF4478))
                }
            }
        }
    }
}

@Composable fun MessagingScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, connectionId: String) {
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() } // text, isOwn
    val primary = parseColor(sdk.config.community.primaryColor)

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Messages") { nav.popBackStack() }
        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { (text, isOwn) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start) {
                    Text(text, color = if (isOwn) Color(0xFF0A0A0A) else Color.White, fontSize = 15.sp,
                        modifier = Modifier.background(if (isOwn) primary else CardBg, RoundedCornerShape(16.dp)).padding(14.dp, 10.dp))
                }
            }
        }
        Row(Modifier.padding(12.dp).padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Type a message...", color = GrayText) },
                shape = RoundedCornerShape(20.dp), singleLine = true, keyboardActions = KeyboardActions(onSend = { if (input.isNotBlank()) { messages.add(input.trim() to true); input = "" } }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send))
            Button(onClick = { if (input.isNotBlank()) { messages.add(input.trim() to true); input = "" } },
                Modifier.size(40.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (input.isNotBlank()) primary else DividerColor)) {
                Text(">", fontWeight = FontWeight.Bold, color = if (input.isNotBlank()) Color(0xFF0A0A0A) else GrayText)
            }
        }
    }
}

@Composable fun FreestyleCreateScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val scope = rememberCoroutineScope()

    // -- Prompt state --
    data class PromptItem(val id: String, val emoji: String, val text: String)
    var prompts by remember { mutableStateOf<List<PromptItem>>(emptyList()) }
    var selectedPromptId by remember { mutableStateOf<String?>(null) }
    var promptsLoading by remember { mutableStateOf(true) }

    // -- Recording state --
    var isRecording by remember { mutableStateOf(false) }
    var hasRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val maxDuration = 60

    // Load prompts from API on launch
    LaunchedEffect(Unit) {
        try {
            val data = sdk.apiClient.getFreestylePrompts()
            val json = JSONObject(String(data))
            val arr = json.optJSONArray("prompts") ?: JSONArray()
            val loaded = mutableListOf<PromptItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                loaded.add(PromptItem(
                    id = obj.optString("id", "p_$i"),
                    emoji = obj.optString("emoji", "\uD83C\uDFA4"),
                    text = obj.optString("text", "Freestyle")
                ))
            }
            prompts = loaded
        } catch (_: Exception) {
            // Fallback prompts so the screen is never empty
            prompts = listOf(
                PromptItem("default_1", "\uD83C\uDFA4", "Hot take"),
                PromptItem("default_2", "\uD83D\uDD25", "Game reaction"),
                PromptItem("default_3", "\u26BD", "Match prediction"),
                PromptItem("default_4", "\uD83C\uDFC6", "MVP pick"),
                PromptItem("default_5", "\uD83D\uDCAA", "Hype moment")
            )
        }
        promptsLoading = false
    }

    // Timer: ticks every second while recording, auto-stops at 60s
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording && elapsedSeconds < maxDuration) {
                delay(1000L)
                elapsedSeconds++
            }
            if (elapsedSeconds >= maxDuration) {
                isRecording = false
                hasRecording = true
            }
        }
    }

    // -- Helpers --
    fun timerColor(seconds: Int): Color = when {
        seconds <= 5 -> Color.Red
        seconds in 6..15 -> Orange1
        else -> Color.White
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    Box(Modifier.fillMaxSize().background(DarkBg)) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeader("New Freestyle") { nav.popBackStack() }

            Spacer(Modifier.height(16.dp))

            // -- Section title --
            Text(
                "Choose a prompt",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text("Select a prompt to start recording", color = GrayText, fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            // -- Prompt carousel --
            if (promptsLoading) {
                Box(Modifier.height(130.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(prompts) { prompt ->
                        val isSelected = selectedPromptId == prompt.id
                        val bgColor = if (isSelected) primary else Color.Transparent
                        val borderColor = if (isSelected) primary else Gray5
                        val textColor = if (isSelected) DarkBg else Color.White
                        val emojiAlpha = if (isSelected) 1f else 0.7f

                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(130.dp)
                                .clickable(enabled = !isRecording) {
                                    selectedPromptId = if (isSelected) null else prompt.id
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.5.dp, borderColor)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    prompt.emoji,
                                    fontSize = 32.sp,
                                    modifier = Modifier.alpha(emojiAlpha)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    prompt.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // -- Timer display --
            if (isRecording || hasRecording) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecording) {
                        // Pulsing red recording dot
                        val infiniteTransition = rememberInfiniteTransition(label = "rec")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 1f, targetValue = 0.2f,
                            animationSpec = infiniteRepeatable(
                                tween(600, easing = EaseInOut), RepeatMode.Reverse
                            ), label = "dot"
                        )
                        Box(
                            Modifier
                                .size(10.dp)
                                .alpha(dotAlpha)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("REC", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        formatTime(elapsedSeconds),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) timerColor(elapsedSeconds) else Color.White
                    )
                    Text(
                        " / ${formatTime(maxDuration)}",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // -- Record / Stop button --
            val canRecord = selectedPromptId != null
            val recordBtnBorder = when {
                isRecording -> Color.Red
                canRecord -> primary
                else -> Gray5
            }
            val recordBtnBg = when {
                isRecording -> Color.Red.copy(alpha = 0.1f)
                canRecord -> primary.copy(alpha = 0.1f)
                else -> Color.Transparent
            }

            Button(
                onClick = {
                    if (isRecording) {
                        isRecording = false
                        hasRecording = true
                    } else if (!hasRecording) {
                        isRecording = true
                        hasRecording = false
                    }
                },
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                enabled = canRecord && !hasRecording,
                colors = ButtonDefaults.buttonColors(
                    containerColor = recordBtnBg,
                    disabledContainerColor = Color.Transparent
                ),
                border = BorderStroke(4.dp, recordBtnBorder)
            ) {
                if (isRecording)
                    Box(Modifier.size(24.dp).background(Color.Red, RoundedCornerShape(4.dp)))
                else
                    Box(Modifier.size(24.dp).background(
                        if (canRecord) primary else Gray5,
                        RoundedCornerShape(12.dp)
                    ))
            }

            Spacer(Modifier.height(10.dp))

            Text(
                when {
                    !canRecord -> "Select a prompt first"
                    isRecording -> "Tap to stop"
                    hasRecording -> "Recording complete"
                    else -> "Tap to record"
                },
                color = GrayText, fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // -- Playback button (shown after recording) --
            if (hasRecording && !isRecording) {
                OutlinedButton(
                    onClick = { isPlaying = !isPlaying },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Gray5),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(if (isPlaying) "\u23F9  Stop" else "\u25B6  Play back", fontSize = 14.sp)
                }

                Spacer(Modifier.height(24.dp))

                // -- Discard + Share buttons --
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Discard
                    OutlinedButton(
                        onClick = {
                            hasRecording = false
                            isPlaying = false
                            elapsedSeconds = 0
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Gray5),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayText)
                    ) {
                        Text("Discard", fontWeight = FontWeight.Medium)
                    }

                    // Share Freestyle
                    Button(
                        onClick = {
                            scope.launch {
                                isUploading = true
                                try {
                                    val body = JSONObject().apply {
                                        put("promptId", selectedPromptId)
                                        put("durationSeconds", elapsedSeconds)
                                    }
                                    sdk.apiClient.createFreestyle(body.toString().toByteArray())
                                    nav.popBackStack()
                                } catch (_: Exception) {
                                    // upload failed — stay on screen
                                } finally {
                                    isUploading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text(
                            "Share Freestyle",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }

        // -- Loading overlay during upload --
        if (isUploading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = primary, strokeWidth = 3.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Sharing your freestyle...", color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable fun PollResponseScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, pollId: String) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var selected by remember { mutableStateOf<Int?>(null) }
    val options = listOf("Home Team", "Away Team")
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Poll") { nav.popBackStack() }
        Column(Modifier.padding(24.dp)) {
            Text("Who's going to win tonight?", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.height(24.dp))
            options.forEachIndexed { i, text ->
                val isSelected = selected == i
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .background(if (isSelected) primary.copy(alpha = 0.1f) else CardBg, RoundedCornerShape(12.dp))
                    .border(if (isSelected) 2.dp else 0.dp, if (isSelected) primary else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { selected = i }.padding(16.dp)) {
                    Text(text, color = Color.White, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.weight(1f))
                Button(onClick = {}, enabled = selected != null,
            modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp), shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (selected != null) primary else CardBg)) {
            Text("Vote", fontWeight = FontWeight.SemiBold, color = if (selected != null) Color(0xFF0A0A0A) else GrayText)
        }
        Spacer(Modifier.height(32.dp))
    }
}

private data class ContactEntry(val name: String, val phone: String)

@Composable fun InviteScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showQrView by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<ContactEntry>>(emptyList()) }
    var invitedSet by remember { mutableStateOf(setOf<String>()) }
    var contactsPermissionGranted by remember { mutableStateOf(false) }
    var connections by remember { mutableStateOf<List<JSONObject>>(emptyList()) }

    val maxSquadSize = 8
    val spotsRemaining = maxSquadSize - invitedSet.size

    // Contacts permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        contactsPermissionGranted = granted
        if (granted) {
            val resolver: ContentResolver = context.contentResolver
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            val loadedContacts = mutableListOf<ContactEntry>()
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: continue
                    val phone = it.getString(phoneIdx) ?: continue
                    loadedContacts.add(ContactEntry(name, phone))
                }
            }
            contacts = loadedContacts
        }
    }

    // Load invite code + connections
    LaunchedEffect(Unit) {
        try {
            val userData = sdk.apiClient.getLoggedInUser()
            val json = JSONObject(String(userData))
            inviteCode = json.optString("inviteCode", "SQD000")
        } catch (_: Exception) {
            inviteCode = "SQD000"
        }
        try {
            val connData = sdk.apiClient.getUserConnections()
            val arr = JSONArray(String(connData))
            val list = mutableListOf<JSONObject>()
            for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
            connections = list
        } catch (_: Exception) {}
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    // Filter contacts
    val filteredContacts = if (searchQuery.isBlank()) contacts
    else contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery)
    }

    // Group by first letter
    val groupedContacts = filteredContacts.groupBy {
        it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
    }.toSortedMap()

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar with back + title + QR toggle
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Text("<", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Text("Invite", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { showQrView = !showQrView }) {
                Text(if (showQrView) "List" else "QR", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primary)
            }
        }

        // Squad capacity: X spots remaining + dots
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$spotsRemaining spots remaining", fontSize = 14.sp, color = GrayText)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until maxSquadSize) {
                    Box(
                        Modifier.size(10.dp)
                            .background(
                                if (i < invitedSet.size) primary else Gray6,
                                CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (showQrView) {
            // QR view: invite code card with tap-to-copy + share
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Your Invite Code", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(Modifier.height(24.dp))
                Text(
                    inviteCode, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    letterSpacing = 4.sp,
                    modifier = Modifier
                        .background(CardBg, RoundedCornerShape(12.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(inviteCode))
                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(40.dp, 24.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Tap to copy", fontSize = 12.sp, color = GrayText)
                Spacer(Modifier.height(32.dp))
                Box(
                    Modifier.size(200.dp).background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151515))
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "Join my squad! Use code: $inviteCode")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Invite"))
                    },
                    Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Share Invite Link", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0A0A))
                }
            }
        } else {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search contacts or enter phone number", color = GrayText) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White, unfocusedBorderColor = DividerColor,
                    cursorColor = Color.White, focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black,
                )
            )
            Spacer(Modifier.height(12.dp))

            // Contacts list with sticky headers
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                groupedContacts.forEach { (letter, contactsInGroup) ->
                    stickyHeader {
                        Text(
                            letter, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primary,
                            modifier = Modifier.fillMaxWidth().background(DarkBg).padding(vertical = 8.dp)
                        )
                    }
                    items(contactsInGroup) { contact ->
                        val isInvited = invitedSet.contains(contact.phone)
                        val isFull = invitedSet.size >= maxSquadSize
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle
                            Box(
                                Modifier.size(44.dp).background(Purple1, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    contact.name.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(contact.name, fontSize = 16.sp, color = Color.White)
                                Text(contact.phone, fontSize = 13.sp, color = GrayText)
                            }
                            Button(
                                onClick = {
                                    if (!isFull && !isInvited) {
                                        invitedSet = invitedSet + contact.phone
                                    } else if (isFull && !isInvited) {
                                        Toast.makeText(context, "Squad is full! Max $maxSquadSize members.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isFull || isInvited,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInvited) SquadGreen.copy(alpha = 0.2f) else primary,
                                    disabledContainerColor = Gray5
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    if (isInvited) "Invited" else "Invite",
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (isInvited) SquadGreen else Color(0xFF0A0A0A)
                                )
                            }
                        }
                        Divider(color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable fun AddCallTitleScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, connectionId: String) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var title by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(DarkBg).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenHeader("Squad Line") { nav.popBackStack() }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(88.dp).border(3.dp, primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("C", fontSize = 28.sp, color = primary) // Phone icon placeholder
        }
        Spacer(Modifier.height(24.dp))
        Text("Why are you calling?", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(16.dp))
        SquadOutlinedField(title, { if (it.length <= 30) title = it }, "Enter a title...")
        Text("${title.length}/30", fontSize = 12.sp, color = if (title.length >= 25) ErrorColor else GrayText)
        Spacer(Modifier.weight(1f))
        Button(onClick = { nav.navigate(SquadRoute.ActiveCall.create(connectionId, title)) },
            enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (title.isNotBlank()) primary else CardBg)) {
            Text("Start Call", fontWeight = FontWeight.SemiBold, color = if (title.isNotBlank()) Color(0xFF0A0A0A) else GrayText)
        }
    }
}

@Composable fun ActiveCallScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, connectionId: String, title: String) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var elapsed by remember { mutableIntStateOf(0) }
    var muted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); elapsed++ } }

    Column(Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(96.dp).background(primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("C", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text("${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')}", color = GrayText)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { muted = !muted }, Modifier.size(64.dp).background(if (muted) Color.White.copy(0.3f) else Color.White.copy(0.1f), CircleShape)) {
                Text(if (muted) "Un" else "M", color = Color.White)
            }
            Button(onClick = { sdk.squadLine.endCall(); nav.popBackStack() }, Modifier.size(72.dp), shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4478))) {
                Text("End", color = Color.White, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}, Modifier.size(64.dp).background(Color.White.copy(0.1f), CircleShape)) {
                Text("Sp", color = Color.White)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

// ============================================================
// QR SCANNER SCREEN
// ============================================================

@Composable
fun QRScannerScreen(
    sdk: SquadSportsSDKInstance,
    nav: NavHostController,
    onScan: (String) -> Unit
) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var showRescan by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            // Camera preview placeholder
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera Preview", color = GrayText, fontSize = 16.sp)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission required", color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text("Grant Permission", color = Color(0xFF0A0A0A))
                    }
                }
            }
        }

        // Overlay frame with scan area
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Text("X", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.weight(1f))

            // Scan area with corner markers
            Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                // Corner markers - top left
                Box(Modifier.align(Alignment.TopStart).size(30.dp)) {
                    Box(Modifier.align(Alignment.TopStart).width(30.dp).height(3.dp).background(primary))
                    Box(Modifier.align(Alignment.TopStart).width(3.dp).height(30.dp).background(primary))
                }
                // Corner markers - top right
                Box(Modifier.align(Alignment.TopEnd).size(30.dp)) {
                    Box(Modifier.align(Alignment.TopEnd).width(30.dp).height(3.dp).background(primary))
                    Box(Modifier.align(Alignment.TopEnd).width(3.dp).height(30.dp).background(primary))
                }
                // Corner markers - bottom left
                Box(Modifier.align(Alignment.BottomStart).size(30.dp)) {
                    Box(Modifier.align(Alignment.BottomStart).width(30.dp).height(3.dp).background(primary))
                    Box(Modifier.align(Alignment.BottomStart).width(3.dp).height(30.dp).background(primary))
                }
                // Corner markers - bottom right
                Box(Modifier.align(Alignment.BottomEnd).size(30.dp)) {
                    Box(Modifier.align(Alignment.BottomEnd).width(30.dp).height(3.dp).background(primary))
                    Box(Modifier.align(Alignment.BottomEnd).width(3.dp).height(30.dp).background(primary))
                }

                // Scanned result overlay
                if (scannedCode != null) {
                    Box(
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Scanned!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SquadGreen)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                if (scannedCode != null) "Code detected" else "Point your camera at a QR code",
                color = GrayText, fontSize = 14.sp
            )

            Spacer(Modifier.weight(1f))

            // Rescan button
            if (showRescan) {
                Button(
                    onClick = {
                        scannedCode = null
                        showRescan = false
                    },
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Scan Again", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0A0A))
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ============================================================
// PROFILE PHOTO CAPTURE SHEET
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoCaptureSheet(
    onDismiss: () -> Unit,
    onPhotoUri: (Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // When a bitmap is captured, save it and return the URI
        if (bitmap != null) {
            try {
                val uri = MediaStore.Images.Media.insertImage(
                    context.contentResolver, bitmap, "squad_profile_${System.currentTimeMillis()}", null
                )
                onPhotoUri(if (uri != null) Uri.parse(uri) else null)
            } catch (_: Exception) {
                onPhotoUri(null)
            }
        }
        onDismiss()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        onPhotoUri(uri)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile Photo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(24.dp))

            // Take Photo
            Button(
                onClick = { cameraLauncher.launch(null) },
                Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gray5)
            ) {
                Text("Take Photo", fontWeight = FontWeight.Medium, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))

            // Choose from Library
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gray5)
            ) {
                Text("Choose from Library", fontWeight = FontWeight.Medium, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))

            // Cancel
            TextButton(
                onClick = onDismiss,
                Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium, color = GrayText)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ============================================================
// ENTER PHONE SCREEN
// ============================================================

@Composable
fun EnterPhoneScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isValid = phone.length == 10 && phone.all { it.isDigit() }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        // Back button
        IconButton(onClick = { nav.popBackStack() }) {
            Text("<", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))

        Text("Enter Your Phone Number", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(24.dp))

        // Phone field with +1 prefix
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .height(56.dp)
                    .background(Color.Black, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .border(1.dp, DividerColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+1", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.filter { it.isDigit() }
                    if (digitsOnly.length <= 10) {
                        phone = digitsOnly
                        error = null
                    }
                },
                placeholder = { Text("Phone number", color = GrayText) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = {
                    if (isValid) scope.launch {
                        loading = true
                        try {
                            if (sdk.auth.createSession(phone = "+1$phone", isPhone = true)) {
                                nav.navigate(SquadRoute.EnterCode.create("+1$phone"))
                            } else {
                                error = "Failed to send code. Please try again."
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "An error occurred"
                        }
                        loading = false
                    }
                }),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White, unfocusedBorderColor = DividerColor,
                    cursorColor = Color.White, focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black,
                )
            )
        }

        // Error display
        error?.let {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth()
                    .background(Orange1.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(4.dp).height(16.dp).background(Orange1, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(it, color = Orange1, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        // Send Code button
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    try {
                        if (sdk.auth.createSession(phone = "+1$phone", isPhone = true)) {
                            nav.navigate(SquadRoute.EnterCode.create("+1$phone"))
                        } else {
                            error = "Failed to send code. Please try again."
                        }
                    } catch (e: Exception) {
                        error = e.message ?: "An error occurred"
                    }
                    loading = false
                }
            },
            enabled = isValid && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                disabledContainerColor = CardBg,
            ),
        ) {
            Text(
                if (loading) "Sending..." else "Send Code",
                fontWeight = FontWeight.SemiBold,
                color = if (isValid && !loading) Color(0xFF0A0A0A) else GrayText
            )
        }
        Spacer(Modifier.height(16.dp))

        // Legal footer
        Text(
            "By tapping \"Send Code\" you agree to the Terms & Conditions and Privacy Policy",
            color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================
// SHARED COMPONENTS
// ============================================================

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Text("<", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
            textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun SquadOutlinedField(
    value: String, onValueChange: (String) -> Unit, placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text, imeAction: ImeAction = ImeAction.Next,
    onAction: (() -> Unit)? = null,
) {
    OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = GrayText) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onGo = { onAction?.invoke() }, onSend = { onAction?.invoke() }),
        singleLine = true, shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White, unfocusedBorderColor = DividerColor,
            cursorColor = Color.White, focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black,
        ))
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GrayText, letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp))
    Column(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(12.dp)), content = content)
}

@Composable
private fun SettingsRow(label: String, value: String? = null) {
    Row(Modifier.fillMaxWidth().padding(14.dp, 14.dp)) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        value?.let { Text(it, color = GrayText) }
        Text(" >", color = GrayText)
    }
}

// ============================================================
// EDIT PROFILE
// ============================================================

@Composable
fun EditProfileScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Text("<", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Text("Edit Profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            if (hasChanges) {
                TextButton(onClick = { nav.popBackStack() }) {
                    Text("Save", color = parseColor(sdk.config.community.primaryColor), fontWeight = FontWeight.SemiBold)
                }
            } else { Spacer(Modifier.width(48.dp)) }
        }
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp).background(Color(0xFF6E82E7), CircleShape), contentAlignment = Alignment.Center) {
                Text(if (name.isNotEmpty()) name.first().uppercase() else "?", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Change Photo", color = parseColor(sdk.config.community.primaryColor), fontSize = 14.sp)
            Spacer(Modifier.height(32.dp))
            Text("NAME", color = GrayText, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SquadOutlinedField(name, { name = it; hasChanges = true }, "Your name")
            Spacer(Modifier.height(24.dp))
            Text("EMAIL", color = GrayText, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Text("Not set", color = GrayText, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
            Spacer(Modifier.height(24.dp))
            Text("COMMUNITY", color = GrayText, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Text(sdk.config.community.name, color = GrayText, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
        }
    }
}

// ============================================================
// BLOCKED USERS
// ============================================================

@Composable
fun BlockedUsersScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Blocked Users") { nav.popBackStack() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No blocked users", color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Users you block will appear here", color = GrayText, fontSize = 14.sp)
            }
        }
    }
}

// ============================================================
// DELETE ACCOUNT
// ============================================================

@Composable
fun DeleteAccountScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    val reasons = listOf("It's not useful to me", "Privacy concerns", "Too many notifications", "Found an alternative", "Other")
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Delete Account") { nav.popBackStack() }
        Column(Modifier.weight(1f).padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().background(Color(0xFFFF4478).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text("!", color = Color(0xFFFF4478), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("This action is permanent and cannot be undone", color = Color(0xFFFF4478), fontSize = 14.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text("We're sorry to see you go", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Please tell us why you're leaving so we can improve", color = GrayText)
            Spacer(Modifier.height(24.dp))
            reasons.forEachIndexed { i, reason ->
                val isSelected = selectedReason == reason
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .background(if (isSelected) Color(0xFFFF4478).copy(alpha = 0.05f) else CardBg, RoundedCornerShape(12.dp))
                    .border(if (isSelected) 2.dp else 0.dp, if (isSelected) Color(0xFFFF4478) else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { selectedReason = reason }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(22.dp).border(2.dp, if (isSelected) Color(0xFFFF4478) else DividerColor, CircleShape), contentAlignment = Alignment.Center) {
                        if (isSelected) Box(Modifier.size(10.dp).background(Color(0xFFFF4478), CircleShape))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(reason, color = Color.White)
                }
            }
        }
        Column(Modifier.padding(24.dp).padding(bottom = 8.dp)) {
            Button(onClick = {
                scope.launch { sdk.auth.logout(); nav.navigate(SquadRoute.Landing.route) { popUpTo(0) { inclusive = true } } }
                        }, enabled = selectedReason != null,
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4478), disabledContainerColor = Color(0xFFFF4478).copy(alpha = 0.5f))) {
                Text("Delete My Account", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            TextButton(onClick = { nav.popBackStack() }, Modifier.fillMaxWidth()) {
                Text("Keep My Account", color = GrayText)
            }
        }
    }
}

// ============================================================
// EVENTS
// ============================================================

private data class EventItem(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val attendeeCount: Int,
    val isAttending: Boolean,
)

@Composable
fun EventsScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val scope = rememberCoroutineScope()

    var events by remember { mutableStateOf(listOf<EventItem>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var attendingIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val data = sdk.apiClient.getEvents()
                val json = JSONObject(String(data))
                val arr = json.optJSONArray("events") ?: JSONArray()
                val eventList = mutableListOf<EventItem>()
                val attendingSet = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    val e = arr.getJSONObject(i)
                    val id = e.optString("id", "$i")
                    val attending = e.optBoolean("isAttending", false)
                    if (attending) attendingSet.add(id)
                    eventList.add(EventItem(
                        id = id,
                        title = e.optString("title", "Event"),
                        date = e.optString("date", e.optString("startDate", "")),
                        location = e.optString("location", e.optString("venue", "")),
                        attendeeCount = e.optInt("attendeeCount", e.optInt("attendees", 0)),
                        isAttending = attending,
                    ))
                }
                events = eventList
                attendingIds = attendingSet
            } catch (_: Exception) {
                error = "Failed to load events"
            }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Events") { nav.popBackStack() }

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error ?: "Error", color = ErrorColor, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = {
                            loading = true; error = null
                            scope.launch {
                                try {
                                    val data = sdk.apiClient.getEvents()
                                    val json = JSONObject(String(data))
                                    val arr = json.optJSONArray("events") ?: JSONArray()
                                    val eventList = mutableListOf<EventItem>()
                                    val attendingSet = mutableSetOf<String>()
                                    for (i in 0 until arr.length()) {
                                        val e = arr.getJSONObject(i)
                                        val id = e.optString("id", "$i")
                                        val attending = e.optBoolean("isAttending", false)
                                        if (attending) attendingSet.add(id)
                                        eventList.add(EventItem(
                                            id = id,
                                            title = e.optString("title", "Event"),
                                            date = e.optString("date", e.optString("startDate", "")),
                                            location = e.optString("location", e.optString("venue", "")),
                                            attendeeCount = e.optInt("attendeeCount", e.optInt("attendees", 0)),
                                            isAttending = attending,
                                        ))
                                    }
                                    events = eventList
                                    attendingIds = attendingSet
                                } catch (_: Exception) { error = "Failed to load events" }
                                loading = false
                            }
                        }) { Text("Retry", color = primary) }
                    }
                }
            }
            events.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No upcoming events", color = GrayText)
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        val isAttending = attendingIds.contains(event.id)
                        var localAttendeeCount by remember(event.id) {
                            mutableStateOf(event.attendeeCount)
                        }
                        var toggling by remember { mutableStateOf(false) }

                        Column(
                            Modifier.fillMaxWidth()
                                .background(CardBg, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                event.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (event.date.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(event.date, color = GrayText, fontSize = 13.sp)
                            }
                            if (event.location.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(event.location, color = GrayText, fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "$localAttendeeCount attending",
                                    color = GrayText,
                                    fontSize = 12.sp,
                                )
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = {
                                        if (toggling) return@Button
                                        toggling = true
                                        scope.launch {
                                            try {
                                                val body = JSONObject().apply {
                                                    put("attending", !isAttending)
                                                }.toString().toByteArray()
                                                sdk.apiClient.setAttendee(event.id, body)
                                                if (isAttending) {
                                                    attendingIds = attendingIds - event.id
                                                    localAttendeeCount = (localAttendeeCount - 1).coerceAtLeast(0)
                                                } else {
                                                    attendingIds = attendingIds + event.id
                                                    localAttendeeCount += 1
                                                }
                                            } catch (_: Exception) {}
                                            toggling = false
                                        }
                                    },
                                    enabled = !toggling,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAttending) Gray3 else primary,
                                        disabledContainerColor = if (isAttending) Gray3.copy(alpha = 0.5f) else primary.copy(alpha = 0.5f),
                                    ),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        if (isAttending) "Attending" else "Attend",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// WALLET
// ============================================================

@Composable
fun WalletScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Wallet") { nav.popBackStack() }
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.fillMaxWidth()
                .background(CardBg, RoundedCornerShape(16.dp))
                .border(1.5.dp, primary, RoundedCornerShape(16.dp))
                .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your Balance", color = GrayText, fontSize = 12.sp)
                Text("0", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = primary)
                Text("points", color = GrayText, fontSize = 12.sp)
            }
            Spacer(Modifier.height(32.dp))
            Text("Your Coupons", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text("No coupons available", color = GrayText)
        }
    }
}

// ============================================================
// LOGIN (phone-based)
// ============================================================

@Composable
fun LoginScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isValid = phone.filter { it.isDigit() }.length >= 10

    Column(Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        IconButton(onClick = { nav.popBackStack() }) {
            Text("<", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Enter Your Phone Number", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().border(1.dp, DividerColor, RoundedCornerShape(8.dp))) {
            Text("+1", color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.padding(16.dp))
            OutlinedTextField(phone, { phone = it; error = null }, Modifier.weight(1f),
                placeholder = { Text("Phone number", color = GrayText) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = Color.White))
        }
        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = ErrorColor, fontSize = 14.sp) }
        Spacer(Modifier.weight(1f))
        Button(onClick = {
            scope.launch {
                loading = true
                if (sdk.auth.createSession(phone, isPhone = true)) {
                    nav.navigate(SquadRoute.EnterCode.create(phone))
                } else { error = "Failed to send code" }
                loading = false
            }
        }, enabled = isValid && !loading, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary, disabledContainerColor = CardBg)) {
            Text(if (loading) "Sending..." else "Send Code", fontWeight = FontWeight.SemiBold,
                color = if (isValid) Color(0xFF0A0A0A) else GrayText)
        }
    }
}

// ============================================================
// FREESTYLE (view)
// ============================================================

@Composable
fun FreestyleViewScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, freestyleId: String) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Freestyle") { nav.popBackStack() }
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Freestyle content", color = GrayText)
        }
    }
}

// ============================================================
// FREESTYLE LISTENS / REACTIONS
// ============================================================

@Composable
fun FreestyleListensScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, freestyleId: String) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Listens") { nav.popBackStack() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No listens yet", color = GrayText) }
    }
}

@Composable
fun FreestyleReactionsScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, freestyleId: String) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Reactions") { nav.popBackStack() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No reactions yet", color = GrayText) }
    }
}

@Composable
fun CommunityFreestyleListensScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, freestyleId: String) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Community Listens") { nav.popBackStack() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No community listens yet", color = GrayText) }
    }
}

@Composable
fun CommunityFreestyleReactionsScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, freestyleId: String) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Community Reactions") { nav.popBackStack() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No community reactions yet", color = GrayText) }
    }
}

// ============================================================
// POLL SUMMATION
// ============================================================

@Composable
fun PollSummationScreen(sdk: SquadSportsSDKInstance, nav: NavHostController, pollId: String) {
    val primary = parseColor(sdk.config.community.primaryColor)
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Poll Results") { nav.popBackStack() }
        Column(Modifier.padding(24.dp)) {
            Text("Poll Results", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("0 total votes", color = GrayText, fontSize = 14.sp)
        }
    }
}

// ============================================================
// NETWORK STATUS
// ============================================================

@Composable
fun NetworkStatusScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        ScreenHeader("Network Status") { nav.popBackStack() }
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Internet", color = Color.White, modifier = Modifier.weight(1f))
                Box(Modifier.size(10.dp).background(Color.Green, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Connected", color = GrayText, fontSize = 14.sp)
            }
            Divider(color = DividerColor)
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Real-time (SSE)", color = Color.White, modifier = Modifier.weight(1f))
                Box(Modifier.size(10.dp).background(Color.Green, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Good", color = GrayText, fontSize = 14.sp)
            }
        }
    }
}

// ============================================================
// INVITATION QR CODE
// ============================================================

@Composable
fun InvitationQrCodeScreen(sdk: SquadSportsSDKInstance, nav: NavHostController) {
    val primary = parseColor(sdk.config.community.primaryColor)
    Column(Modifier.fillMaxSize().background(DarkBg), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenHeader("Scan QR Code") { nav.popBackStack() }
        Spacer(Modifier.weight(1f))
        // Scanner frame
        Box(Modifier.size(250.dp).border(1.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text("Camera", color = GrayText)
        }
        Spacer(Modifier.height(24.dp))
        Text("Point your camera at a Squad QR code", color = GrayText, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
    }
}

// ============================================================
// FREESTYLE CARD COMPOSABLES
// ============================================================

data class FreestyleReaction(val emoji: String, val count: Int, val reactedByMe: Boolean = false)
data class ListenUser(val userId: String, val name: String, val avatarUrl: String?)
data class ReactionUser(val emoji: String, val userId: String, val name: String, val avatarUrl: String?)

@Composable
fun FreestyleFeedItem(
    sdk: SquadSportsSDKInstance,
    authorName: String,
    authorAvatarUrl: String?,
    communityName: String?,
    label: String?,
    bodyText: String,
    timestamp: String,
    reactions: List<FreestyleReaction>,
    listenUsers: List<ListenUser>,
    replyCount: Int,
    isUploading: Boolean = false,
    onTap: () -> Unit = {},
    onReact: (String) -> Unit = {},
    onReply: () -> Unit = {},
    onListen: () -> Unit = {}
) {
    val primary = parseColor(sdk.config.community.primaryColor)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable { onTap() }
            .alpha(if (isUploading) 0.5f else 1f)
            .padding(16.dp)
    ) {
        // Author row
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContactThumbnail(name = authorName, avatarUrl = authorAvatarUrl, size = 36)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (communityName != null) {
                    Text(communityName, color = GrayText, fontSize = 12.sp)
                }
            }
            Text(timestamp, color = GrayText, fontSize = 11.sp)
        }

        // Label tag
        if (label != null) {
            Spacer(Modifier.height(10.dp))
            LabelTagBadge(label = label)
        }

        // Body
        Spacer(Modifier.height(12.dp))
        Text(bodyText, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)

        // Uploading indicator
        if (isUploading) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = primary)
                Spacer(Modifier.width(6.dp))
                Text("Uploading...", color = GrayText, fontSize = 12.sp)
            }
        }

        // Reactions row
        if (reactions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            EmojiReactionsRow(reactions = reactions, onReact = onReact)
        }

        // Listen row
        if (listenUsers.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            ListenReactionsRow(users = listenUsers, onListen = onListen)
        }

        // Reply count
        if (replyCount > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "$replyCount ${if (replyCount == 1) "reply" else "replies"}",
                color = primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onReply() }
            )
        }
    }
}

@Composable
fun FreestyleReplyCard(
    authorName: String,
    authorAvatarUrl: String?,
    bodyText: String,
    timestamp: String,
    isPending: Boolean = false,
    isUploading: Boolean = false,
    reactions: List<FreestyleReaction> = emptyList(),
    onReact: (String) -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .alpha(if (isPending || isUploading) 0.5f else 1f)
    ) {
        ContactThumbnail(name = authorName, avatarUrl = authorAvatarUrl, size = 28)
        Spacer(Modifier.width(8.dp))
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Gray5)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(authorName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(timestamp, color = GrayText, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(bodyText, color = Color.White, fontSize = 14.sp)

            if (isUploading) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Purple1)
                    Spacer(Modifier.width(4.dp))
                    Text("Sending...", color = GrayText, fontSize = 11.sp)
                }
            }

            if (reactions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                EmojiReactionsRow(reactions = reactions, onReact = onReact)
            }
        }
    }
}

@Composable
fun EmojiReactionsRow(
    reactions: List<FreestyleReaction>,
    maxVisible: Int = 6,
    onReact: (String) -> Unit = {}
) {
    val visible = reactions.take(maxVisible)
    val overflow = reactions.size - maxVisible

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        visible.forEach { reaction ->
            val bg = if (reaction.reactedByMe) Purple1.copy(alpha = 0.25f) else Gray5
            val border = if (reaction.reactedByMe) Purple1 else Color.Transparent
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(20.dp))
                    .clickable { onReact(reaction.emoji) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reaction.emoji, fontSize = 14.sp)
                if (reaction.count > 1) {
                    Spacer(Modifier.width(3.dp))
                    Text("${reaction.count}", color = Color.White, fontSize = 12.sp)
                }
            }
        }
        if (overflow > 0) {
            Text("+$overflow", color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ListenReactionsRow(
    users: List<ListenUser>,
    maxVisible: Int = 3,
    onListen: () -> Unit = {}
) {
    val visible = users.take(maxVisible)
    val overflow = users.size - maxVisible

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onListen() }
    ) {
        // Overlapping avatars
        visible.forEachIndexed { index, user ->
            Box(Modifier.offset(x = (-8 * index).dp)) {
                ContactThumbnail(name = user.name, avatarUrl = user.avatarUrl, size = 22)
            }
        }
        val offset = (-8 * (visible.size - 1)).dp
        Spacer(Modifier.width(4.dp + offset))
        if (overflow > 0) {
            Text("+$overflow listening", color = GrayText, fontSize = 12.sp)
        } else {
            Text("listening", color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
fun LabelTagBadge(label: String) {
    val gradient = when (label.lowercase()) {
        "mental" -> Brush.horizontalGradient(listOf(Color(0xFF6E82E7), Color(0xFF9B6EE7)))
        "sports" -> Brush.horizontalGradient(listOf(Color(0xFF11EC0F), Color(0xFF0FD8EC)))
        "intelligence" -> Brush.horizontalGradient(listOf(Color(0xFFFF955C), Color(0xFFFF5C8A)))
        "general" -> Brush.horizontalGradient(listOf(Color(0xFF8A8A8A), Color(0xFFB0B0B0)))
        else -> Brush.horizontalGradient(listOf(Purple1, Purple1))
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label.replaceFirstChar { it.uppercase() },
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun UserListenRow(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactThumbnail(name = name, avatarUrl = avatarUrl, size = 36)
        Spacer(Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
fun UserReactionRow(
    emoji: String,
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        ContactThumbnail(name = name, avatarUrl = avatarUrl, size = 32)
        Spacer(Modifier.width(10.dp))
        Text(name, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
fun CommunityListensAggregate(
    communityName: String,
    totalListens: Int,
    topListeners: List<ListenUser>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Text(communityName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "$totalListens total ${if (totalListens == 1) "listen" else "listens"}",
            color = GrayText,
            fontSize = 13.sp
        )
        if (topListeners.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            topListeners.forEach { user ->
                UserListenRow(name = user.name, avatarUrl = user.avatarUrl)
            }
        }
    }
}

// ============================================================
// SENTINEL-LIKE COMPOSABLES (LaunchedEffect-based)
// ============================================================

@Composable
fun NetworkBannerEffect(
    onStatusChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onStatusChange(true)
            }

            override fun onLost(network: Network) {
                onStatusChange(false)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                onStatusChange(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val activeCaps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isOnline = activeCaps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        onStatusChange(isOnline)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

@Composable
fun NotificationHandlerEffect(
    sdk: SquadSportsSDKInstance,
    onNotificationReceived: (type: String, payload: JSONObject) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            sdk.apiClient.postJson("/v2/devices/register", JSONObject().apply {
                put("platform", "android")
                put("osVersion", Build.VERSION.RELEASE)
                put("model", Build.MODEL)
            }.toString().toByteArray())
            Log.d("SquadSDK", "Push notification registration sent")
        } catch (e: Exception) {
            Log.w("SquadSDK", "Failed to register for push notifications: ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        Log.d("SquadSDK", "Notification handler active — push delivery via FCM")
    }
}

@Composable
fun UpdateDeviceInfoEffect(sdk: SquadSportsSDKInstance) {
    LaunchedEffect(Unit) {
        try {
            sdk.apiClient.postJson("/v2/devices/info", JSONObject().apply {
                put("platform", "android")
                put("osVersion", Build.VERSION.RELEASE)
                put("model", Build.MODEL)
                put("manufacturer", Build.MANUFACTURER)
                put("sdkVersion", Build.VERSION.SDK_INT)
            }.toString().toByteArray())
            Log.d("SquadSDK", "Device info sent successfully")
        } catch (e: Exception) {
            Log.w("SquadSDK", "Failed to send device info: ${e.message}")
        }
    }
}

@Composable
fun FirstSquaddyEffect(
    sdk: SquadSportsSDKInstance,
    onFirstSquaddy: () -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d("SquadSDK", "FirstSquaddy sentinel active")
    }
}

@Composable
fun CommunityThemeSyncEffect(
    sdk: SquadSportsSDKInstance,
    onThemeUpdated: (primaryColor: String, secondaryColor: String) -> Unit = { _, _ -> }
) {
    LaunchedEffect(Unit) {
        onThemeUpdated(sdk.config.community.primaryColor, sdk.config.community.secondaryColor ?: sdk.config.community.primaryColor)
    }
}

// ============================================================
// NETWORK BANNER
// ============================================================

enum class NetworkStatus { Online, Offline, Reconnecting }

@Composable
fun NetworkBanner(
    status: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val isVisible = status != NetworkStatus.Online
    val bgColor = when (status) {
        NetworkStatus.Offline -> Red
        NetworkStatus.Reconnecting -> Orange1
        NetworkStatus.Online -> Color.Transparent
    }
    val message = when (status) {
        NetworkStatus.Offline -> "No internet connection"
        NetworkStatus.Reconnecting -> "Reconnecting..."
        NetworkStatus.Online -> ""
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================
// INVITE SUB-COMPOSABLES
// ============================================================

@Composable
fun ContactThumbnail(
    name: String,
    avatarUrl: String?,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "?" }

    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Gray5),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            // Placeholder for async image loading — SDK image loader will replace
            Text(initials, color = Color.White, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold)
        } else {
            Text(initials, color = Color.White, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ContactItemRow(
    name: String,
    phone: String?,
    avatarUrl: String?,
    isInvited: Boolean = false,
    onInvite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactThumbnail(name = name, avatarUrl = avatarUrl, size = 44)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (phone != null) {
                Text(phone, color = GrayText, fontSize = 13.sp)
            }
        }
        Button(
            onClick = onInvite,
            enabled = !isInvited,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isInvited) Gray5 else Purple1,
                disabledContainerColor = Gray5
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                if (isInvited) "Invited" else "Invite",
                fontSize = 13.sp,
                color = if (isInvited) GrayText else Color.White
            )
        }
    }
}

@Composable
fun ContactOnSquadRow(
    name: String,
    avatarUrl: String?,
    isAdded: Boolean = false,
    onAdd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactThumbnail(name = name, avatarUrl = avatarUrl, size = 44)
        Spacer(Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Button(
            onClick = onAdd,
            enabled = !isAdded,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdded) Gray5 else SquadGreen,
                disabledContainerColor = Gray5
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                if (isAdded) "Added" else "Add",
                fontSize = 13.sp,
                color = if (isAdded) GrayText else Color.Black
            )
        }
    }
}

@Composable
fun ContactSectionHeader(
    letter: String,
    modifier: Modifier = Modifier
) {
    Text(
        letter.uppercase(),
        color = GrayText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun RequestItemRow(
    name: String,
    avatarUrl: String?,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactThumbnail(name = name, avatarUrl = avatarUrl, size = 44)
        Spacer(Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDecline,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayText),
                border = BorderStroke(1.dp, Gray5),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Decline", fontSize = 13.sp)
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = SquadGreen),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Accept", fontSize = 13.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun InviteSquadMaxedContent(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Squad is Full", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Squads can have up to 12 people. Remove a squaddy to make room for someone new.",
            color = GrayText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Purple1),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Got It", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ============================================================
// PROFILE SUB-COMPOSABLES
// ============================================================

@Composable
fun EmptyFeedText(
    message: String = "Nothing here yet",
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = GrayText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NonSquaddyConnectionButton(
    sdk: SquadSportsSDKInstance,
    onAddToSquad: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primary = parseColor(sdk.config.community.primaryColor)

    Button(
        onClick = onAddToSquad,
        colors = ButtonDefaults.buttonColors(containerColor = primary),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(48.dp)
    ) {
        Text("Add to Squad", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProfilePollCard(
    question: String,
    options: List<String>,
    selectedIndex: Int? = null,
    totalVotes: Int = 0,
    onVote: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Text(question, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
        Spacer(Modifier.height(14.dp))

        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            val hasVoted = selectedIndex != null

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Purple1.copy(alpha = 0.2f) else Gray5)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) Purple1 else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = !hasVoted) { onVote(index) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    option,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        if (totalVotes > 0) {
            Spacer(Modifier.height(10.dp))
            Text("$totalVotes ${if (totalVotes == 1) "vote" else "votes"}", color = GrayText, fontSize = 12.sp)
        }
    }
}

// ============================================================
// COMMUNITIES
// ============================================================

@Composable
fun CommunityTagBadge(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(name, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CommunityRowItem(
    name: String,
    dotColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) dotColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Text("\u2713", color = dotColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityBottomSheet(
    communities: List<Pair<String, Color>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(40.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gray5)
            )
        },
        modifier = modifier
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                "Switch Community",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Divider(color = Gray5, thickness = 0.5.dp)
            communities.forEachIndexed { index, (name, color) ->
                CommunityRowItem(
                    name = name,
                    dotColor = color,
                    isSelected = index == selectedIndex,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

// ============================================================
// TOASTS
// ============================================================

data class SquadToastData(
    val id: Long = System.currentTimeMillis(),
    val type: SquadToastType,
    val message: String,
    val durationMs: Long = 3000
)

enum class SquadToastType { Success, Error, Busy }

@Composable
fun SuccessToast(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B3D1B))
            .border(1.dp, SquadGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("\u2713", color = SquadGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text(message, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ErrorToast(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3D2A1B))
            .border(1.dp, Orange1.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("\u26A0", color = Orange1, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(message, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BusyToast(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Gray5, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = Purple1
        )
        Spacer(Modifier.width(12.dp))
        Text(message, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ToastHost(
    toasts: List<SquadToastData>,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        toasts.forEach { toast ->
            key(toast.id) {
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(toast.id) {
                    visible = true
                    delay(toast.durationMs)
                    visible = false
                    delay(300) // animation out
                    onDismiss(toast.id)
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    when (toast.type) {
                        SquadToastType.Success -> SuccessToast(message = toast.message)
                        SquadToastType.Error -> ErrorToast(message = toast.message)
                        SquadToastType.Busy -> BusyToast(message = toast.message)
                    }
                }
            }
        }
    }
}

// ============================================================
// ONBOARDING SUB-COMPOSABLES
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoActionSheet(
    onTakePhoto: () -> Unit,
    onChoosePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(40.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gray5)
            )
        },
        modifier = modifier
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                "Profile Photo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Divider(color = Gray5, thickness = 0.5.dp)

            // Take Photo
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onTakePhoto() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("\uD83D\uDCF7", fontSize = 20.sp)
                Spacer(Modifier.width(14.dp))
                Text("Take Photo", color = Color.White, fontSize = 16.sp)
            }

            // Choose from Library
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onChoosePhoto() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("\uD83D\uDDBC", fontSize = 20.sp)
                Spacer(Modifier.width(14.dp))
                Text("Choose from Library", color = Color.White, fontSize = 16.sp)
            }

            Divider(color = Gray5, thickness = 0.5.dp)

            // Cancel
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Cancel", color = GrayText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ProfilePhotoSelector(
    photoUri: Uri?,
    size: Int = 120,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primary = Purple1

    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Gray5)
            .border(2.dp, primary.copy(alpha = 0.5f), CircleShape)
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            // Placeholder — SDK image loader handles actual rendering
            Text("Photo", color = Color.White, fontSize = 14.sp)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("\uD83D\uDCF7", fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text("Add Photo", color = GrayText, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun WelcomeAnimation(
    sdk: SquadSportsSDKInstance,
    modifier: Modifier = Modifier
) {
    val primary = parseColor(sdk.config.community.primaryColor)
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")

    // Pulsing circles
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse3"
    )

    // Rotating hearts
    val heartRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "heartRot"
    )

    Box(modifier.size(280.dp), contentAlignment = Alignment.Center) {
        // Decorative circles
        Box(
            Modifier
                .size(220.dp)
                .scale(pulse1)
                .alpha(0.15f)
                .clip(CircleShape)
                .background(primary)
        )
        Box(
            Modifier
                .size(160.dp)
                .scale(pulse2)
                .alpha(0.25f)
                .clip(CircleShape)
                .background(primary)
        )
        Box(
            Modifier
                .size(100.dp)
                .scale(pulse3)
                .alpha(0.35f)
                .clip(CircleShape)
                .background(primary)
        )

        // Rotating hearts around the center
        val heartCount = 6
        for (i in 0 until heartCount) {
            val angle = (heartRotation + (360f / heartCount) * i) * (PI / 180f)
            val radius = 100.dp
            val xOff = (cos(angle) * radius.value).dp
            val yOff = (sin(angle) * radius.value).dp
            Text(
                "\u2764",
                color = primary.copy(alpha = 0.6f),
                fontSize = 16.sp,
                modifier = Modifier.offset(x = xOff, y = yOff)
            )
        }

        // Center text
        Text(
            "Welcome!",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// SETTINGS SUB-COMPOSABLES
// ============================================================

@Composable
fun PersonalSettingsSection(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    onFirstNameChange: (String) -> Unit = {},
    onLastNameChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Personal Info", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SettingsTextField(label = "First Name", value = firstName, onValueChange = onFirstNameChange)
        SettingsTextField(label = "Last Name", value = lastName, onValueChange = onLastNameChange)
        SettingsTextField(
            label = "Email",
            value = email,
            onValueChange = onEmailChange,
            keyboardType = KeyboardType.Email
        )
        SettingsTextField(
            label = "Phone",
            value = phone,
            onValueChange = onPhoneChange,
            keyboardType = KeyboardType.Phone
        )
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Gray5,
                unfocusedContainerColor = Gray5,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Purple1,
                focusedIndicatorColor = Purple1,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        )
    }
}

@Composable
fun CommunitySettingsSection(
    sdk: SquadSportsSDKInstance,
    communities: List<Pair<String, Color>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Text("Community", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        communities.forEachIndexed { index, (name, color) ->
            CommunityRowItem(
                name = name,
                dotColor = color,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
            if (index < communities.lastIndex) {
                Divider(color = Gray5.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

// ============================================================
// AUDIO COMPOSABLES
// ============================================================

/**
 * Waveform visualization — a row of vertical bars whose heights reflect audio levels.
 * Progress fills bars left-to-right with [filledColor]; unfilled bars use [emptyColor].
 */
@Composable
fun WaveformView(
    levels: List<Float>,
    progress: Float,
    barCount: Int = 40,
    filledColor: Color = Purple1,
    emptyColor: Color = Gray5,
) {
    val displayLevels = remember(levels, barCount) {
        if (levels.isEmpty()) List(barCount) { 0.2f }
        else List(barCount) { i ->
            val idx = (i.toFloat() / barCount * levels.size).toInt().coerceIn(0, levels.lastIndex)
            levels[idx].coerceIn(0.05f, 1f)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        displayLevels.forEachIndexed { index, level ->
            val filled = index.toFloat() / barCount <= progress
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(level)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (filled) filledColor else emptyColor)
            )
        }
    }
}

/**
 * Recording timer — counts down from [maxSeconds] (default 60).
 * Colors: red <= 5s, orange 6-15s, white otherwise.
 * Shows a pulsing red "REC" badge while active.
 */
@Composable
fun RecordingTimer(
    isRecording: Boolean,
    elapsedSeconds: Int,
    maxSeconds: Int = 60,
) {
    val remaining = (maxSeconds - elapsedSeconds).coerceAtLeast(0)
    val timerColor = when {
        remaining <= 5 -> Red
        remaining <= 15 -> Orange1
        else -> Color.White
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dot_alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isRecording) {
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(dotAlpha)
                    .background(Red, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "REC",
                color = Red.copy(alpha = dotAlpha),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
        }
        val mins = remaining / 60
        val secs = remaining % 60
        Text(
            text = "%d:%02d".format(mins, secs),
            color = timerColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Circular record button — 72dp idle, 104dp recording, with scale animation.
 * Shows mic icon when idle, stop square when recording.
 */
@Composable
fun RecordButton(
    isRecording: Boolean,
    onToggle: () -> Unit,
    communityColor: Color = Purple1,
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 104f / 72f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "record_scale"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isRecording) Red else communityColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            // Stop square
            Box(
                Modifier
                    .size(24.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
            )
        } else {
            // Mic icon placeholder
            Text("\uD83C\uDFA4", fontSize = 28.sp, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Play/pause toggle for recorded audio playback using SquadAudioPlayer.
 */
@Composable
fun PlaybackButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Gray5)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // Pause bars
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.width(4.dp).height(18.dp).background(Color.White, RoundedCornerShape(1.dp)))
                Box(Modifier.width(4.dp).height(18.dp).background(Color.White, RoundedCornerShape(1.dp)))
            }
        } else {
            // Play triangle
            Text("\u25B6", color = Color.White, fontSize = 20.sp)
        }
    }
}

/**
 * Send button with loading spinner overlay.
 */
@Composable
fun SendButton(
    isLoading: Boolean,
    enabled: Boolean = true,
    communityColor: Color = Purple1,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled && !isLoading) communityColor else communityColor.copy(alpha = 0.4f))
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text("\u27A4", color = Color.White, fontSize = 20.sp)
        }
    }
}

/**
 * Expandable recording footer — collapsed shows a mic button, expanded shows
 * timer + record + playback + send + close controls. Uses AnimatedVisibility.
 */
@Composable
fun RecordingFooter(
    communityColor: Color = Purple1,
    onSend: (filePath: String, durationMs: Long) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var recordedPath by remember { mutableStateOf<String?>(null) }
    var recordedDuration by remember { mutableLongStateOf(0L) }
    var audioLevels by remember { mutableStateOf(listOf<Float>()) }

    val recorder = remember { com.squadsports.sdk.components.SquadAudioRecorder(context) }
    val player = remember { com.squadsports.sdk.components.SquadAudioPlayer() }

    // Timer tick while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording && elapsedSeconds < 60) {
                delay(1000)
                elapsedSeconds++
                // Sample pseudo-levels from recorder amplitude
                audioLevels = audioLevels + ((Math.random().toFloat() * 0.7f) + 0.15f)
            }
            if (elapsedSeconds >= 60 && isRecording) {
                // Auto-stop at 60s
                val result = recorder.stopRecording()
                isRecording = false
                if (result != null) {
                    recordedPath = result.first
                    recordedDuration = result.second
                    hasRecording = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Close button row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        "\u2715",
                        color = GrayText,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable {
                            recorder.cancelRecording()
                            player.stop()
                            isRecording = false
                            hasRecording = false
                            expanded = false
                            audioLevels = emptyList()
                            onClose()
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Timer
                RecordingTimer(isRecording = isRecording, elapsedSeconds = elapsedSeconds)

                Spacer(Modifier.height(12.dp))

                // Waveform
                WaveformView(
                    levels = audioLevels,
                    progress = if (hasRecording && !isRecording) player.progress else (elapsedSeconds / 60f),
                    filledColor = communityColor,
                    emptyColor = Gray5
                )

                Spacer(Modifier.height(16.dp))

                // Controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasRecording && !isRecording) {
                        PlaybackButton(isPlaying = isPlaying, onToggle = {
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                recordedPath?.let { path ->
                                    player.play(path)
                                    player.onStateChange = { state ->
                                        isPlaying = state == com.squadsports.sdk.components.SquadAudioPlayer.State.PLAYING
                                    }
                                    isPlaying = true
                                }
                            }
                        })
                    }

                    RecordButton(isRecording = isRecording, communityColor = communityColor, onToggle = {
                        if (isRecording) {
                            val result = recorder.stopRecording()
                            isRecording = false
                            if (result != null) {
                                recordedPath = result.first
                                recordedDuration = result.second
                                hasRecording = true
                            }
                        } else {
                            player.stop()
                            isPlaying = false
                            hasRecording = false
                            audioLevels = emptyList()
                            recorder.startRecording()
                            isRecording = true
                        }
                    })

                    if (hasRecording && !isRecording) {
                        SendButton(isLoading = isSending, communityColor = communityColor, onClick = {
                            isSending = true
                            recordedPath?.let { path ->
                                onSend(path, recordedDuration)
                            }
                        })
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // Collapsed mic button
        if (!expanded) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(communityColor)
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83C\uDFA4", fontSize = 24.sp)
            }
        }
    }
}

// ============================================================
// POLL COMPOSABLES
// ============================================================

data class PollOption(
    val id: String,
    val label: String,
    val imageUrl: String? = null,
    val voteCount: Int = 0,
    val votePercent: Float = 0f,
)

/**
 * Animated progress bar for poll options — width animates after voting.
 */
@Composable
fun AnimatedPollOptions(
    options: List<PollOption>,
    selectedId: String?,
    hasVoted: Boolean,
    communityColor: Color = Purple1,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val targetWidth by animateFloatAsState(
                targetValue = if (hasVoted) option.votePercent else 0f,
                animationSpec = tween(durationMillis = 600, easing = EaseInOut),
                label = "poll_bar_${option.id}"
            )

            PollOptionRow(
                option = option,
                isSelected = selectedId == option.id,
                hasVoted = hasVoted,
                animatedPercent = targetWidth,
                communityColor = communityColor,
                onSelect = { onSelect(option.id) }
            )
        }
    }
}

/**
 * Individual poll option row — image, label, selection state, animated result bar.
 */
@Composable
fun PollOptionRow(
    option: PollOption,
    isSelected: Boolean,
    hasVoted: Boolean,
    animatedPercent: Float,
    communityColor: Color = Purple1,
    onSelect: () -> Unit,
) {
    val borderColor = if (isSelected) communityColor else Gray5
    val bgAlpha = if (isSelected) 0.15f else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(communityColor.copy(alpha = bgAlpha))
            .clickable(enabled = !hasVoted) { onSelect() }
    ) {
        // Animated fill bar behind content
        if (hasVoted) {
            Box(
                Modifier
                    .fillMaxWidth(animatedPercent)
                    .fillMaxHeight()
                    .background(communityColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            )
        }

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option image placeholder
            option.imageUrl?.let {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray5),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83D\uDDBC", fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
            }

            Text(
                text = option.label,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )

            if (hasVoted) {
                Text(
                    text = "${(animatedPercent * 100).toInt()}%",
                    color = if (isSelected) communityColor else GrayText,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            } else {
                // Selection indicator
                Box(
                    Modifier
                        .size(20.dp)
                        .border(
                            width = if (isSelected) 6.dp else 2.dp,
                            color = if (isSelected) communityColor else Gray6,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Poll question card — displays question text, selectable options, and submit button.
 */
@Composable
fun PollQuestionCard(
    question: String,
    options: List<PollOption>,
    communityColor: Color = Purple1,
    onSubmit: (selectedOptionId: String) -> Unit,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(
            text = question,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        AnimatedPollOptions(
            options = options,
            selectedId = selectedId,
            hasVoted = hasVoted,
            communityColor = communityColor,
            onSelect = { id -> if (!hasVoted) selectedId = id }
        )

        Spacer(Modifier.height(16.dp))

        if (!hasVoted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedId != null) communityColor
                        else communityColor.copy(alpha = 0.3f)
                    )
                    .clickable(enabled = selectedId != null) {
                        selectedId?.let {
                            hasVoted = true
                            onSubmit(it)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Submit Vote", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Post-vote result card showing the chosen answer and poll results.
 */
@Composable
fun PollResponseCard(
    question: String,
    chosenLabel: String,
    totalVotes: Int,
    communityColor: Color = Purple1,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(question, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\u2714", color = communityColor, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "You voted: $chosenLabel",
                color = communityColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "$totalVotes total votes",
            color = GrayText,
            fontSize = 13.sp
        )
    }
}

/**
 * Badge for Community or Daily poll type.
 */
@Composable
fun PollTagBadge(
    tag: String,
    communityColor: Color = Purple1,
) {
    val bgColor = when (tag.lowercase()) {
        "community" -> communityColor
        "daily" -> Orange1
        else -> Gray5
    }
    Box(
        modifier = Modifier
            .background(bgColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag,
            color = bgColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * User reaction row — avatar circle + emoji + display name.
 */
@Composable
fun PollUserReactionRow(
    avatarInitial: String,
    emoji: String,
    displayName: String,
    communityColor: Color = Purple1,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(communityColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(avatarInitial, color = communityColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(displayName, color = Color.White, fontSize = 14.sp)
    }
}

/**
 * Nudge button with horizontal shake animation using rememberInfiniteTransition + translateX.
 */
@Composable
fun NudgeButton(
    label: String = "Nudge",
    communityColor: Color = Purple1,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nudge_shake")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nudge_tx"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(communityColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Home feed poll card — transitions from unanswered to answered state.
 */
@Composable
fun HomeActivePollCard(
    question: String,
    options: List<PollOption>,
    tag: String = "Community",
    communityColor: Color = Purple1,
    onSubmit: (String) -> Unit,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PollTagBadge(tag = tag, communityColor = communityColor)
            Spacer(Modifier.weight(1f))
            Text("\uD83D\uDCCA", fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = question,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        AnimatedPollOptions(
            options = options,
            selectedId = selectedId,
            hasVoted = hasVoted,
            communityColor = communityColor,
            onSelect = { id ->
                if (!hasVoted) {
                    selectedId = id
                    hasVoted = true
                    onSubmit(id)
                }
            }
        )
    }
}

/**
 * Squaddy profile variant of an active poll card.
 */
@Composable
fun SquaddyActivePollCard(
    question: String,
    options: List<PollOption>,
    squaddyName: String,
    communityColor: Color = Purple1,
    onSubmit: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "$squaddyName's Poll",
            color = GrayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        PollQuestionCard(
            question = question,
            options = options,
            communityColor = communityColor,
            onSubmit = onSubmit
        )
    }
}

/**
 * Swipeable card stack for polls — uses Modifier.graphicsLayer for scale and rotation.
 */
@Composable
fun PollCardStack(
    cards: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val visibleCards = cards.drop(currentIndex).take(3)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        visibleCards.reversed().forEachIndexed { stackIdx, card ->
            val depth = visibleCards.size - 1 - stackIdx
            val cardScale = 1f - (depth * 0.05f)
            val cardOffsetY = depth * 12f
            val cardRotation = depth * 2f

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        translationY = cardOffsetY
                        rotationZ = if (depth > 0) cardRotation else 0f
                    }
                    .then(
                        if (depth == 0) {
                            Modifier.clickable { currentIndex++ }
                        } else Modifier
                    )
            ) {
                card()
            }
        }
    }
}

/**
 * Results screen header for poll summation.
 */
@Composable
fun PollSummationHeader(
    question: String,
    totalVotes: Int,
    communityColor: Color = Purple1,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(
            "Poll Results",
            color = communityColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            question,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$totalVotes votes",
            color = GrayText,
            fontSize = 14.sp
        )
    }
}

/**
 * Modal bottom sheet with an emoji grid for adding reactions to polls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReactionSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val emojis = listOf(
        "\u26A1", "\uD83D\uDD25", "\uD83D\uDE02", "\u2764\uFE0F",
        "\uD83D\uDC94", "\u2753", "\uD83D\uDE4C", "\uD83D\uDE31",
        "\uD83D\uDCAF", "\uD83D\uDE0E", "\uD83E\uDD14", "\uD83C\uDF89",
        "\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDE22", "\uD83D\uDE21",
        "\uD83E\uDD29", "\uD83E\uDD73", "\uD83D\uDE09", "\uD83D\uDE44"
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = CardBg,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(Gray5, RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Add Reaction",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                // Emoji grid — 5 columns
                val rows = emojis.chunked(5)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onEmojiSelected(emoji) }
                                    .padding(8.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================
// MESSAGE COMPOSABLES
// ============================================================

/**
 * Right-aligned message bubble for the current user.
 * Light background tinted with community color, rounded corners with flat bottom-right.
 */
@Composable
fun MessageBubbleMine(
    text: String,
    timestamp: String = "",
    communityColor: Color = Purple1,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    communityColor.copy(alpha = 0.15f),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                )
                .border(
                    1.dp,
                    communityColor.copy(alpha = 0.3f),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text, color = Color.White, fontSize = 15.sp)
        }
        if (timestamp.isNotEmpty()) {
            Text(
                timestamp,
                color = GrayText,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Left-aligned message bubble from another user, with avatar.
 * Dark background, rounded corners with flat bottom-left.
 */
@Composable
fun MessageBubbleTheirs(
    text: String,
    avatarInitial: String,
    senderName: String = "",
    timestamp: String = "",
    communityColor: Color = Purple1,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 64.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar
        Box(
            Modifier
                .size(32.dp)
                .background(communityColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                avatarInitial,
                color = communityColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(8.dp))

        Column {
            if (senderName.isNotEmpty()) {
                Text(
                    senderName,
                    color = GrayText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        Gray5,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text, color = Color.White, fontSize = 15.sp)
            }
            if (timestamp.isNotEmpty()) {
                Text(
                    timestamp,
                    color = GrayText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                )
            }
        }
    }
}

/**
 * Emoji text for reaction types: bolt, fire, laugh, heart, broken, question.
 */
@Composable
fun MessageReactionIcon(
    reaction: String,
    modifier: Modifier = Modifier,
) {
    val emoji = when (reaction.lowercase()) {
        "bolt" -> "\u26A1"
        "fire" -> "\uD83D\uDD25"
        "laugh" -> "\uD83D\uDE02"
        "heart" -> "\u2764\uFE0F"
        "broken" -> "\uD83D\uDC94"
        "question" -> "\u2753"
        else -> reaction
    }
    Text(
        text = emoji,
        fontSize = 20.sp,
        modifier = modifier
    )
}

/**
 * Semi-transparent overlay with 6 emoji reaction buttons in a Row.
 */
@Composable
fun FeedReactionOverlay(
    isVisible: Boolean,
    onReaction: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .background(CardBg, RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val reactions = listOf("bolt", "fire", "laugh", "heart", "broken", "question")
                reactions.forEach { reaction ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Gray5)
                            .clickable { onReaction(reaction) },
                        contentAlignment = Alignment.Center
                    ) {
                        MessageReactionIcon(reaction = reaction)
                    }
                }
            }
        }
    }
}

/**
 * Voice reply overlay — record, preview, and send a voice reply.
 */
@Composable
fun VoiceReplyOverlay(
    isVisible: Boolean,
    communityColor: Color = Purple1,
    onSend: (filePath: String, durationMs: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + expandVertically(),
        exit = fadeOut(tween(200)) + shrinkVertically()
    ) {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }
        var hasRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        var isSending by remember { mutableStateOf(false) }
        var elapsedSeconds by remember { mutableIntStateOf(0) }
        var recordedPath by remember { mutableStateOf<String?>(null) }
        var recordedDuration by remember { mutableLongStateOf(0L) }
        var audioLevels by remember { mutableStateOf(listOf<Float>()) }

        val recorder = remember { com.squadsports.sdk.components.SquadAudioRecorder(context) }
        val player = remember { com.squadsports.sdk.components.SquadAudioPlayer() }

        LaunchedEffect(isRecording) {
            if (isRecording) {
                elapsedSeconds = 0
                while (isRecording && elapsedSeconds < 60) {
                    delay(1000)
                    elapsedSeconds++
                    audioLevels = audioLevels + ((Math.random().toFloat() * 0.7f) + 0.15f)
                }
                if (elapsedSeconds >= 60 && isRecording) {
                    val result = recorder.stopRecording()
                    isRecording = false
                    if (result != null) {
                        recordedPath = result.first
                        recordedDuration = result.second
                        hasRecording = true
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        "\u2715",
                        color = GrayText,
                        fontSize = 24.sp,
                        modifier = Modifier.clickable {
                            recorder.cancelRecording()
                            player.stop()
                            onDismiss()
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Voice Reply",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                RecordingTimer(isRecording = isRecording, elapsedSeconds = elapsedSeconds)

                Spacer(Modifier.height(16.dp))

                WaveformView(
                    levels = audioLevels,
                    progress = if (hasRecording && !isRecording) player.progress else (elapsedSeconds / 60f),
                    filledColor = communityColor,
                    emptyColor = Gray5
                )

                Spacer(Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasRecording && !isRecording) {
                        PlaybackButton(isPlaying = isPlaying, onToggle = {
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                recordedPath?.let { path ->
                                    player.play(path)
                                    player.onStateChange = { state ->
                                        isPlaying = state == com.squadsports.sdk.components.SquadAudioPlayer.State.PLAYING
                                    }
                                    isPlaying = true
                                }
                            }
                        })
                    }

                    RecordButton(isRecording = isRecording, communityColor = communityColor, onToggle = {
                        if (isRecording) {
                            val result = recorder.stopRecording()
                            isRecording = false
                            if (result != null) {
                                recordedPath = result.first
                                recordedDuration = result.second
                                hasRecording = true
                            }
                        } else {
                            player.stop()
                            isPlaying = false
                            hasRecording = false
                            audioLevels = emptyList()
                            recorder.startRecording()
                            isRecording = true
                        }
                    })

                    if (hasRecording && !isRecording) {
                        SendButton(isLoading = isSending, communityColor = communityColor, onClick = {
                            isSending = true
                            recordedPath?.let { path ->
                                onSend(path, recordedDuration)
                            }
                        })
                    }
                }
            }
        }
    }
}

/**
 * Message data class for the feed list.
 */
data class MessageItem(
    val id: String,
    val text: String,
    val senderName: String,
    val senderInitial: String,
    val isMine: Boolean,
    val timestamp: String,
    val reactions: List<String> = emptyList(),
)

/**
 * LazyColumn (reversed) of messages with empty-state placeholder.
 */
@Composable
fun MessageFeedList(
    messages: List<MessageItem>,
    communityColor: Color = Purple1,
    onReaction: (messageId: String, reaction: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var reactionTargetId by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
        if (messages.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("\uD83D\uDCAC", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "No messages yet",
                    color = GrayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Start the conversation!",
                    color = GrayText,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages.reversed(), key = { it.id }) { message ->
                    Column {
                        if (message.isMine) {
                            MessageBubbleMine(
                                text = message.text,
                                timestamp = message.timestamp,
                                communityColor = communityColor
                            )
                        } else {
                            MessageBubbleTheirs(
                                text = message.text,
                                avatarInitial = message.senderInitial,
                                senderName = message.senderName,
                                timestamp = message.timestamp,
                                communityColor = communityColor
                            )
                        }

                        // Reaction chips
                        if (message.reactions.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(
                                    start = if (message.isMine) 64.dp else 48.dp,
                                    top = 2.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                message.reactions.forEach { reaction ->
                                    Box(
                                        Modifier
                                            .background(Gray5, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        MessageReactionIcon(reaction = reaction, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reaction overlay
        FeedReactionOverlay(
            isVisible = reactionTargetId != null,
            onReaction = { reaction ->
                reactionTargetId?.let { id -> onReaction(id, reaction) }
                reactionTargetId = null
            },
            onDismiss = { reactionTargetId = null }
        )
    }
}

// ============================================================
// SOTD COMPOSABLES
// ============================================================

/**
 * SotdButton — Locked state (gray, lock icon, "Get X more") or
 * Unlocked state (gold border with pulse animation).
 */
@Composable
fun SotdButton(
    isUnlocked: Boolean,
    remainingCount: Int = 0,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sotdPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUnlocked) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "sotdPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = if (isUnlocked) 1f else 0.7f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "sotdPulseAlpha"
    )

    Box(
        modifier = Modifier
            .scale(if (isUnlocked) pulseScale else 1f)
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (isUnlocked) Modifier.border(2.dp, Gold.copy(alpha = pulseAlpha), RoundedCornerShape(24.dp))
                else Modifier.border(1.dp, Gray5, RoundedCornerShape(24.dp))
            )
            .background(if (isUnlocked) Gold.copy(alpha = 0.15f) else Gray5.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isUnlocked) {
                // Lock icon placeholder
                Box(
                    Modifier
                        .size(16.dp)
                        .background(Gray6, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(CardBg, CircleShape)
                    )
                }
                Text(
                    text = "Get $remainingCount more",
                    color = Gray6,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "\u2605",
                    color = Gold,
                    fontSize = 16.sp
                )
                Text(
                    text = "SOTD",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * CurrentSotdUser — Avatar with gold border + star badge + name.
 */
@Composable
fun CurrentSotdUser(
    userName: String,
    avatarUrl: String? = null,
    avatarSize: Int = 72
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(avatarSize.dp)
                    .border(3.dp, Gold, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Gray5),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = (avatarSize / 3).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SOTDTag(size = 22)
        }
        Text(
            text = userName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * AnimatedUserPlus — Dashed circle with "+" and breathing scale animation.
 */
@Composable
fun AnimatedUserPlus(
    size: Int = 56,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .scale(breatheScale)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Gray6,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                        0f
                    )
                )
            )
        }
        Text(
            text = "+",
            color = Gray6,
            fontSize = (size / 2.5).sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * SOTDTag — Gold circle with star, offset positioned.
 */
@Composable
fun SOTDTag(size: Int = 20) {
    Box(
        modifier = Modifier
            .offset(x = 2.dp, y = (-2).dp)
            .size(size.dp)
            .background(Gold, CircleShape)
            .border(1.5.dp, DarkBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u2605",
            color = DarkBg,
            fontSize = (size / 2).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * SotdIntroSheet — ModalBottomSheet: "Introducing Squaddie of the Day".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SotdIntroSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onGetStarted: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Gold.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\u2605", color = Gold, fontSize = 32.sp)
            }

            Text(
                text = "Introducing Squaddie\nof the Day",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Each day, one squad member gets crowned Squaddie of the Day! " +
                        "Earn it by being active, engaging with your squad, and showing up.",
                color = Gray6,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("Get Started", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * SotdSelectingSheet — Member selection carousel for SOTD.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SotdSelectingSheet(
    isVisible: Boolean,
    members: List<JSONObject>,
    onDismiss: () -> Unit,
    onSelect: (JSONObject) -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select Squaddie of the Day",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose who deserves today\u2019s crown",
                color = Gray6,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(members) { member ->
                    val name = member.optString("name", "?")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelect(member) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Gray5)
                                .border(2.dp, Gold.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 72.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * SotdBlockedSheet — "Not a Squaddie party yet".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SotdBlockedSheet(
    isVisible: Boolean,
    requiredCount: Int = 3,
    currentCount: Int = 1,
    onDismiss: () -> Unit,
    onInvite: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Gray5, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDD12", fontSize = 28.sp)
            }

            Text(
                text = "Not a Squaddie\nparty yet",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You need at least $requiredCount squad members to unlock SOTD. " +
                        "You currently have $currentCount. Invite friends to get started!",
                color = Gray6,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onInvite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple1)
            ) {
                Text("Invite Friends", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ============================================================
// WALLET COMPOSABLES
// ============================================================

/**
 * CouponCard — Brand logo, discount, expiry, reveal/copy/share actions.
 */
@Composable
fun CouponCard(
    brandName: String,
    discountText: String,
    expiryDate: String,
    couponCode: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Gray5, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brandName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = brandName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = discountText,
                    color = Gold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray5, RoundedCornerShape(8.dp))
                    .clickable { if (!isRevealed) onReveal() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRevealed) couponCode else "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022  Tap to reveal",
                    color = if (isRevealed) Color.White else Gray6,
                    fontSize = 14.sp,
                    fontWeight = if (isRevealed) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = if (isRevealed) 2.sp else 0.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exp: $expiryDate",
                    color = Gray6,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isRevealed) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Gray5, CircleShape)
                                .clickable(onClick = onCopy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("\uD83D\uDCCB", fontSize = 14.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Gray5, CircleShape)
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\uD83D\uDD17", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

/**
 * CouponShareOverlay — Full-screen overlay for sharing a coupon.
 */
@Composable
fun CouponShareOverlay(
    isVisible: Boolean,
    brandName: String,
    discountText: String,
    couponCode: String,
    onDismiss: () -> Unit,
    onShareAction: (String) -> Unit
) {
    if (!isVisible) return

    var shareAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shareAppeared = true }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (shareAppeared) 1f else 0f,
        animationSpec = tween(300),
        label = "shareOverlayFade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(DarkBg.copy(alpha = 0.92f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Share this deal",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$discountText off at $brandName",
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray5, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = couponCode,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }

                val shareOptions = listOf("Message", "Copy Link", "More")
                shareOptions.forEach { option ->
                    Button(
                        onClick = { onShareAction(option) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (option == "Message") Purple1 else Gray5
                        )
                    ) {
                        Text(
                            text = option,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * RedeemSheet — Conditions checklist + confirm button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemSheet(
    isVisible: Boolean,
    brandName: String,
    conditions: List<String>,
    onDismiss: () -> Unit,
    onConfirmRedeem: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val checkedItems = remember { mutableStateListOf<Int>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Redeem at $brandName",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Please confirm the following conditions:",
                color = Gray6,
                fontSize = 14.sp
            )

            conditions.forEachIndexed { index, condition ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (checkedItems.contains(index)) checkedItems.remove(index)
                            else checkedItems.add(index)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                2.dp,
                                if (checkedItems.contains(index)) Purple1 else Gray6,
                                RoundedCornerShape(6.dp)
                            )
                            .then(
                                if (checkedItems.contains(index))
                                    Modifier.background(Purple1, RoundedCornerShape(6.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checkedItems.contains(index)) {
                            Text("\u2713", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = condition,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val allChecked = checkedItems.size == conditions.size

            Button(
                onClick = onConfirmRedeem,
                enabled = allChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allChecked) Purple1 else Gray5,
                    disabledContainerColor = Gray5
                )
            ) {
                Text(
                    text = "Confirm Redeem",
                    color = if (allChecked) Color.White else Gray6,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * WalletHeader — "Use any code in your 741 Wallet below."
 */
@Composable
fun WalletHeader(
    walletName: String = "741 Wallet",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Use any code in your\n$walletName below.",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )
    }
}

/**
 * WalletContainer — Horizontal LazyRow with snap for coupons.
 */
@Composable
fun WalletContainer(
    coupons: List<JSONObject>,
    onReveal: (Int) -> Unit,
    onCopy: (Int) -> Unit,
    onShare: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(coupons) { index, coupon ->
            val brand = coupon.optString("brand", "Brand")
            val discount = coupon.optString("discount", "10%")
            val expiry = coupon.optString("expiry", "N/A")
            val code = coupon.optString("code", "XXXX")
            val revealed = coupon.optBoolean("revealed", false)

            CouponCard(
                brandName = brand,
                discountText = discount,
                expiryDate = expiry,
                couponCode = code,
                isRevealed = revealed,
                onReveal = { onReveal(index) },
                onCopy = { onCopy(index) },
                onShare = { onShare(index) }
            )
        }
    }
}

/**
 * EventsAttendeesSheet — LazyColumn of attendees.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsAttendeesSheet(
    isVisible: Boolean,
    attendees: List<JSONObject>,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Attendees (${attendees.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(attendees) { attendee ->
                    val name = attendee.optString("name", "Unknown")
                    val status = attendee.optString("status", "going")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Gray5),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            color = when (status) {
                                "going" -> SquadGreen
                                "maybe" -> Orange1
                                else -> Gray6
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (attendees.last() != attendee) {
                        Divider(color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ============================================================
// DIALOG COMPOSABLES
// ============================================================

/**
 * BlockConfirmationDialog — "Block this user?" AlertDialog.
 */
@Composable
fun BlockConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text("Block $userName?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "They won\u2019t be able to see your profile, send you messages, or interact with you in squads. You can unblock them later from settings.",
                color = Gray6,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Block", color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray6)
            }
        }
    )
}

/**
 * DeleteConfirmationDialog — Account deletion warning.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text("Delete Account?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "This action is permanent and cannot be undone. All your data will be deleted including:",
                    color = Gray6,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                listOf("Your profile and settings", "Squad memberships", "Chat history", "Wallet and coupons").forEach {
                    Text(
                        text = "\u2022  $it",
                        color = Gray6,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete Permanently", color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray6)
            }
        }
    )
}

/**
 * FlagConfirmationDialog — Report/flag a user or content.
 */
@Composable
fun FlagConfirmationDialog(
    targetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text("Report $targetName?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "This will flag the content for review by our moderation team. " +
                        "We take reports seriously and will take appropriate action.",
                color = Gray6,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Report", color = Orange1, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray6)
            }
        }
    )
}

/**
 * RemoveFromSquadDialog — Remove member confirmation.
 */
@Composable
fun RemoveFromSquadDialog(
    userName: String,
    squadName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text("Remove $userName?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Are you sure you want to remove $userName from $squadName? " +
                        "They will no longer have access to this squad\u2019s content and chats.",
                color = Gray6,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray6)
            }
        }
    )
}

/**
 * PermissionType enum for PermissionDialog.
 */
enum class PermissionType(val title: String, val description: String, val icon: String) {
    Camera(
        "Camera Access",
        "Allow Squad to access your camera to take photos and record videos for your squad.",
        "\uD83D\uDCF7"
    ),
    Mic(
        "Microphone Access",
        "Allow Squad to access your microphone for voice messages and video calls.",
        "\uD83C\uDF99\uFE0F"
    ),
    Contacts(
        "Contacts Access",
        "Allow Squad to access your contacts to find friends who are already on Squad.",
        "\uD83D\uDCDA"
    ),
    Photos(
        "Photos Access",
        "Allow Squad to access your photo library to share images with your squad.",
        "\uD83D\uDDBC\uFE0F"
    ),
    Notifications(
        "Notification Access",
        "Allow Squad to send you notifications about squad activity, messages, and updates.",
        "\uD83D\uDD14"
    )
}

/**
 * PermissionDialog — Generic permission request dialog.
 */
@Composable
fun PermissionDialog(
    permissionType: PermissionType,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        containerColor = CardBg,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Purple1.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(permissionType.icon, fontSize = 28.sp)
            }
        },
        title = {
            Text(
                permissionType.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                permissionType.description,
                color = Gray6,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text("Allow", color = Purple1, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Don\u2019t Allow", color = Gray6)
            }
        }
    )
}

/**
 * NoConnectionDialog — No internet + refresh action.
 */
@Composable
fun NoConnectionDialog(
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Gray5, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDCF6", fontSize = 28.sp)
            }
        },
        title = {
            Text(
                "No Connection",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "It looks like you\u2019re not connected to the internet. Please check your connection and try again.",
                color = Gray6,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onRefresh) {
                Text("Refresh", color = Purple1, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Gray6)
            }
        }
    )
}

/**
 * ProgressCongratsDialog — Milestone celebration with fade-in animation.
 */
@Composable
fun ProgressCongratsDialog(
    milestoneTitle: String,
    milestoneDescription: String,
    onDismiss: () -> Unit
) {
    var progressAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { progressAppeared = true }
    val congratsAlpha by animateFloatAsState(
        targetValue = if (progressAppeared) 1f else 0f,
        animationSpec = tween(600),
        label = "congratsFade"
    )
    val congratsScale by animateFloatAsState(
        targetValue = if (progressAppeared) 1f else 0.8f,
        animationSpec = tween(600, easing = EaseOutBack),
        label = "congratsScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .alpha(congratsAlpha)
                .scale(congratsScale)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Gold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83C\uDF89", fontSize = 36.sp)
                }

                Text(
                    text = "Congratulations!",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = milestoneTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = milestoneDescription,
                    color = Gray6,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("Awesome!", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * SophiaIntroDialog — AI assistant intro dialog.
 */
@Composable
fun SophiaIntroDialog(
    onGetStarted: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Purple1.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Purple1,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Meet Sophia",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your AI Squad Assistant",
                    color = Purple1,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Sophia can help you discover events, manage your squad, " +
                            "find deals, and keep up with everything happening in your community.",
                    color = Gray6,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple1)
                ) {
                    Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = Gray6, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * VersionUpgradeDialog — Update available dialog.
 */
@Composable
fun VersionUpgradeDialog(
    currentVersion: String,
    newVersion: String,
    isForced: Boolean = false,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isForced) onDismiss() },
        containerColor = CardBg,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Purple1.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\u2B06\uFE0F", fontSize = 28.sp)
            }
        },
        title = {
            Text(
                "Update Available",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "A new version of Squad is available ($newVersion). " +
                            if (isForced) "This update is required to continue using the app."
                            else "Update now for the latest features and improvements.",
                    color = Gray6,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Text(
                    "Current: $currentVersion",
                    color = Gray6.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("Update Now", color = Purple1, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!isForced) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = Gray6)
                }
            }
        }
    )
}

// ============================================================
// HOME DISPLAY COMPOSABLES
// ============================================================

/**
 * SquaddyTimestamp — "5m ago" relative time display.
 */
@Composable
fun SquaddyTimestamp(
    timestampMs: Long,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val diff = now - timestampMs
    val relativeText = when {
        diff < 60_000L -> "now"
        diff < 3_600_000L -> "${diff / 60_000L}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        diff < 604_800_000L -> "${diff / 86_400_000L}d ago"
        else -> "${diff / 604_800_000L}w ago"
    }

    Text(
        text = relativeText,
        color = Gray6,
        fontSize = 10.sp,
        modifier = modifier
    )
}

/**
 * ChatBubbleOverlay — Orange bubble sized per member count.
 */
@Composable
fun ChatBubbleOverlay(
    unreadCount: Int,
    memberCount: Int,
    modifier: Modifier = Modifier
) {
    if (unreadCount <= 0) return

    val bubbleSize = when {
        memberCount <= 2 -> 22.dp
        memberCount <= 4 -> 18.dp
        else -> 16.dp
    }
    val bubbleFontSize = when {
        memberCount <= 2 -> 11.sp
        memberCount <= 4 -> 9.sp
        else -> 8.sp
    }

    Box(
        modifier = modifier
            .size(bubbleSize)
            .background(Orange1, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
            color = Color.White,
            fontSize = bubbleFontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * SquaddyImageComposable — Avatar with optional unread chat bubble overlay.
 */
@Composable
fun SquaddyImageComposable(
    name: String,
    avatarUrl: String? = null,
    size: Int = 56,
    unreadCount: Int = 0,
    memberCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Gray5),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = Color.White,
                fontSize = (size / 2.5).sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (unreadCount > 0) {
            ChatBubbleOverlay(
                unreadCount = unreadCount,
                memberCount = memberCount,
                modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

/**
 * CircleDisplayTopRow — 1 member centered (top of circle layout).
 */
@Composable
fun CircleDisplayTopRow(
    member: JSONObject?,
    memberCount: Int = 5,
    modifier: Modifier = Modifier
) {
    if (member == null) return

    val name = member.optString("name", "?")
    val unread = member.optInt("unreadCount", 0)
    val timestamp = member.optLong("lastActive", 0L)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquaddyImageComposable(
                name = name,
                size = 56,
                unreadCount = unread,
                memberCount = memberCount
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (timestamp > 0L) {
                SquaddyTimestamp(timestampMs = timestamp)
            }
        }
    }
}

/**
 * CircleDisplayCenterRow — 2 members spaced evenly (middle of circle layout).
 */
@Composable
fun CircleDisplayCenterRow(
    leftMember: JSONObject?,
    rightMember: JSONObject?,
    memberCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOfNotNull(leftMember, rightMember).forEach { member ->
            val name = member.optString("name", "?")
            val unread = member.optInt("unreadCount", 0)
            val timestamp = member.optLong("lastActive", 0L)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SquaddyImageComposable(
                    name = name,
                    size = 52,
                    unreadCount = unread,
                    memberCount = memberCount
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (timestamp > 0L) {
                    SquaddyTimestamp(timestampMs = timestamp)
                }
            }
        }
    }
}

/**
 * CircleDisplayBottomRow — 2 members conditional (bottom of circle layout).
 * Only renders members that are non-null.
 */
@Composable
fun CircleDisplayBottomRow(
    leftMember: JSONObject?,
    rightMember: JSONObject?,
    memberCount: Int = 5,
    modifier: Modifier = Modifier
) {
    val bottomMembers = listOfNotNull(leftMember, rightMember)
    if (bottomMembers.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (bottomMembers.size == 2) Arrangement.SpaceEvenly else Arrangement.Center
    ) {
        bottomMembers.forEach { member ->
            val name = member.optString("name", "?")
            val unread = member.optInt("unreadCount", 0)
            val timestamp = member.optLong("lastActive", 0L)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SquaddyImageComposable(
                    name = name,
                    size = 48,
                    unreadCount = unread,
                    memberCount = memberCount
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (timestamp > 0L) {
                    SquaddyTimestamp(timestampMs = timestamp)
                }
            }
        }
    }
}
