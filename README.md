# Squad Sports SDK for Android

[![Platform](https://img.shields.io/badge/platform-Android%207.0+-lightgrey.svg)](https://docs.squadforsports.com)
[![License: Proprietary](https://img.shields.io/badge/license-Proprietary-red.svg)](LICENSE)

Precompiled AAR for the Squad Sports SDK. Add fan engagement features to your sports app in minutes — messaging, polls, freestyles, voice calling, sponsorship inventory, and real-time updates.

## Installation

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.withyoursquad:squad-sports-android:1.5.0")
}
```

## Quick Start

```kotlin
import com.squadsports.sdk.SquadSportsSDK
import com.squadsports.sdk.SquadExperienceActivity

// Initialize
SquadSportsSDK.setup(
    context = this,
    partnerId = "your-partner-id",
    apiKey = "your-api-key",
)

// Launch
SquadExperienceActivity.launch(this)
```

### With Partner Auth (No Login Screen)

```kotlin
SquadSportsSDK.setup(
    context = this,
    partnerId = "your-partner-id",
    apiKey = "your-api-key",
    userData = PartnerUserData(
        email = user.email,
        displayName = user.name,
        externalUserId = user.id,
    ),
)
```

### With Ticketmaster SSO

```kotlin
SquadSportsSDK.setup(
    context = this,
    partnerId = "your-partner-id",
    apiKey = "your-api-key",
    ssoToken = ticketmasterAccessToken,
    ssoProvider = SSOProvider.TICKETMASTER,
)
```

## Features

- **Messaging** — 1:1 with audio messages and reactions
- **Polls** — Interactive polls with live results and branded sponsor polls
- **Freestyles** — Audio posts with community-wide sharing
- **Squad Line** — Real-time voice calls via Twilio
- **Events** — Event attendance and check-ins
- **Wallet** — Rewards, coupons, and sponsor promotions
- **Sponsorship** — In-app sponsorship inventory with impression tracking
- **Analytics** — Pluggable event adapters (Mixpanel, Amplitude, Firebase)
- **SSO** — Ticketmaster, OAuth2, and custom providers
- **Security** — EncryptedSharedPreferences, ProGuard rules included

## Requirements

- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+
- Jetpack Compose (Material3)

## ProGuard

ProGuard / R8 consumer rules are bundled in the AAR and applied automatically.

## Documentation

**[docs.squadforsports.com](https://docs.squadforsports.com)**

## License

Proprietary. See [LICENSE](LICENSE).
