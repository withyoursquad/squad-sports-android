package com.squadsports.sdk.utils

/**
 * Structured logger for the Squad Sports SDK.
 *
 * Supports pluggable log handlers so partners can route SDK logs
 * to their own analytics / crash-reporting systems.
 *
 * Default handler uses Android `android.util.Log` when available,
 * falling back to `println` (e.g. in unit-test JVM environments).
 */
object SquadLogger {

    enum class Level { DEBUG, INFO, WARN, ERROR }

    /**
     * Pluggable handler. Each invocation receives the level, tag, formatted message,
     * and optional throwable.  Replace this to route logs to Crashlytics / DataDog / etc.
     */
    var handler: ((Level, String, String, Throwable?) -> Unit)? = null

    /** Minimum level that will be dispatched. */
    var minLevel: Level = Level.DEBUG

    // --- Context enrichment ---
    var partnerId: String? = null
    var userId: String? = null
    var environment: String? = null

    private const val TAG = "SquadSDK"

    // Detect whether we are running on Android (vs plain JVM in tests)
    private val hasAndroidLog: Boolean by lazy {
        try {
            Class.forName("android.util.Log")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    fun debug(message: String, tag: String = TAG, throwable: Throwable? = null) =
        log(Level.DEBUG, tag, message, throwable)

    fun info(message: String, tag: String = TAG, throwable: Throwable? = null) =
        log(Level.INFO, tag, message, throwable)

    fun warn(message: String, tag: String = TAG, throwable: Throwable? = null) =
        log(Level.WARN, tag, message, throwable)

    fun error(message: String, tag: String = TAG, throwable: Throwable? = null) =
        log(Level.ERROR, tag, message, throwable)

    // ------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------

    private fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
        if (level.ordinal < minLevel.ordinal) return

        val enriched = buildString {
            partnerId?.let { append("[partner=$it] ") }
            userId?.let { append("[user=$it] ") }
            environment?.let { append("[env=$it] ") }
            append(message)
        }

        // Custom handler takes priority
        val customHandler = handler
        if (customHandler != null) {
            customHandler(level, tag, enriched, throwable)
            return
        }

        // Default: Android Log or println
        if (hasAndroidLog) {
            androidLog(level, tag, enriched, throwable)
        } else {
            val prefix = "${level.name}/$tag"
            if (throwable != null) {
                println("$prefix: $enriched — ${throwable.message}")
            } else {
                println("$prefix: $enriched")
            }
        }
    }

    /**
     * Delegates to `android.util.Log` via reflection-free direct call.
     * This method is only reached when [hasAndroidLog] is true.
     */
    private fun androidLog(level: Level, tag: String, message: String, throwable: Throwable?) {
        // We call through android.util.Log directly.
        // The hasAndroidLog guard ensures this code path is never hit on plain JVM.
        when (level) {
            Level.DEBUG -> android.util.Log.d(tag, message, throwable)
            Level.INFO  -> android.util.Log.i(tag, message, throwable)
            Level.WARN  -> android.util.Log.w(tag, message, throwable)
            Level.ERROR -> android.util.Log.e(tag, message, throwable)
        }
    }
}
