package com.squadsports.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.squadsports.sdk.navigation.SquadNavHost
import com.squadsports.sdk.theme.SquadTheme
import com.squadsports.sdk.theme.parseColor

/**
 * Main entry Activity for the Squad Sports experience.
 *
 * ```kotlin
 * SquadExperienceActivity.launch(context)
 * ```
 */
class SquadExperienceActivity : ComponentActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(newIntent(context))
        }

        fun newIntent(context: Context): Intent {
            return Intent(context, SquadExperienceActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sdk = SquadSportsSDK.shared

        setContent {
            SquadTheme(primaryColor = parseColor(sdk.config.community.primaryColor)) {
                SquadNavHost(sdk)
            }
        }
    }
}
