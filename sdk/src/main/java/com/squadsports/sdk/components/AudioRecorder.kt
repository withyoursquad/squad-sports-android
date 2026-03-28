package com.squadsports.sdk.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Native audio recorder for freestyles and voice messages.
 */
class SquadAudioRecorder(private val context: Context) {

    enum class State { IDLE, RECORDING, RECORDED, ERROR }

    var state: State = State.IDLE
        private set

    var onStateChange: ((State) -> Unit)? = null
    var recordedFilePath: String? = null
        private set
    var recordedDurationMs: Long = 0
        private set

    private var recorder: MediaRecorder? = null
    private var startTimeMs: Long = 0

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "squad_recording_${System.currentTimeMillis()}.m4a")
            recordedFilePath = file.absolutePath

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            startTimeMs = System.currentTimeMillis()
            updateState(State.RECORDING)
        } catch (e: Exception) {
            updateState(State.ERROR)
        }
    }

    fun stopRecording(): Pair<String, Long>? {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            recordedDurationMs = System.currentTimeMillis() - startTimeMs
            updateState(State.RECORDED)
            recordedFilePath?.let { it to recordedDurationMs }
        } catch (e: Exception) {
            updateState(State.ERROR)
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        recordedFilePath?.let { File(it).delete() }
        recordedFilePath = null
        recordedDurationMs = 0
        updateState(State.IDLE)
    }

    fun getRecordedFileBytes(): ByteArray? {
        return recordedFilePath?.let { File(it).readBytes() }
    }

    val currentDurationMs: Long
        get() = if (state == State.RECORDING) System.currentTimeMillis() - startTimeMs else recordedDurationMs

    private fun updateState(newState: State) {
        state = newState
        onStateChange?.invoke(newState)
    }
}

/**
 * Simple audio player for playback.
 */
class SquadAudioPlayer {

    enum class State { IDLE, PLAYING, PAUSED, FINISHED }

    var state: State = State.IDLE
        private set
    var onStateChange: ((State) -> Unit)? = null

    private var player: MediaPlayer? = null

    fun play(filePath: String) {
        stop()
        try {
            player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    state = State.FINISHED
                    onStateChange?.invoke(state)
                }
            }
            state = State.PLAYING
            onStateChange?.invoke(state)
        } catch (e: Exception) {
            println("[AudioPlayer] Error: ${e.message}")
        }
    }

    fun playUrl(url: String) {
        stop()
        try {
            player = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    state = State.PLAYING
                    onStateChange?.invoke(state)
                }
                setOnCompletionListener {
                    state = State.FINISHED
                    onStateChange?.invoke(state)
                }
            }
        } catch (e: Exception) {
            println("[AudioPlayer] Error: ${e.message}")
        }
    }

    fun pause() {
        player?.pause()
        state = State.PAUSED
        onStateChange?.invoke(state)
    }

    fun resume() {
        player?.start()
        state = State.PLAYING
        onStateChange?.invoke(state)
    }

    fun stop() {
        player?.release()
        player = null
        state = State.IDLE
        onStateChange?.invoke(state)
    }

    val progress: Float
        get() {
            val p = player ?: return 0f
            return if (p.duration > 0) p.currentPosition.toFloat() / p.duration else 0f
        }

    val durationMs: Int
        get() = player?.duration ?: 0
}
