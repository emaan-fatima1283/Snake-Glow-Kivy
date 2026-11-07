package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEffectManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isSoundEnabled: Boolean = true

    @Volatile
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playEatRegular() {
        if (isSoundEnabled) {
            audioScope.launch {
                synthesizeChime(frequencies = floatArrayOf(587.33f, 880.0f), toneDurationsMs = floatArrayOf(45f, 75f))
            }
        }
        if (isHapticsEnabled) {
            triggerVibration(25, 120)
        }
    }

    fun playEatBonus() {
        if (isSoundEnabled) {
            audioScope.launch {
                synthesizeChime(
                    frequencies = floatArrayOf(659.25f, 880.0f, 1174.66f),
                    toneDurationsMs = floatArrayOf(40f, 40f, 90f)
                )
            }
        }
        if (isHapticsEnabled) {
            triggerVibration(50, 200)
        }
    }

    fun playGameOver() {
        if (isSoundEnabled) {
            audioScope.launch {
                synthesizeGameOverSweep()
            }
        }
        if (isHapticsEnabled) {
            triggerVibration(160, 240)
        }
    }

    fun playButtonClick() {
        if (isSoundEnabled) {
            audioScope.launch {
                synthesizeChime(frequencies = floatArrayOf(980.0f), toneDurationsMs = floatArrayOf(18f))
            }
        }
        if (isHapticsEnabled) {
            triggerVibration(12, 80)
        }
    }

    private fun triggerVibration(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        try {
            vibrator?.let { vib ->
                if (vib.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                        vib.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if device restricts vibration
        }
    }

    private fun synthesizeChime(frequencies: FloatArray, toneDurationsMs: FloatArray) {
        try {
            val sampleRate = 22050
            var totalSamples = 0
            for (dur in toneDurationsMs) {
                totalSamples += (dur * sampleRate / 1000f).toInt()
            }
            if (totalSamples <= 0) return

            val pcm = ShortArray(totalSamples)
            var sampleOffset = 0

            for (i in frequencies.indices) {
                val freq = frequencies[i]
                val toneSamples = (toneDurationsMs[i] * sampleRate / 1000f).toInt()

                for (s in 0 until toneSamples) {
                    val t = s.toFloat() / sampleRate.toFloat()
                    val phase = 2.0 * PI * freq * t
                    // Sine wave with subtle 2nd harmonic
                    val rawWave = 0.8 * sin(phase) + 0.2 * sin(phase * 2.0)
                    // Smooth decay envelope
                    val envelope = exp(-3.5 * (s.toDouble() / toneSamples.toDouble()))
                    val sampleVal = (rawWave * envelope * 24000.0).toInt().coerceIn(-32767, 32767)

                    if (sampleOffset + s < totalSamples) {
                        pcm[sampleOffset + s] = sampleVal.toShort()
                    }
                }
                sampleOffset += toneSamples
            }

            playPcm(pcm, sampleRate)
        } catch (_: Exception) {
            // Audio output device unavailable or busy
        }
    }

    private fun synthesizeGameOverSweep() {
        try {
            val sampleRate = 22050
            val durationMs = 380f
            val totalSamples = (durationMs * sampleRate / 1000f).toInt()
            val pcm = ShortArray(totalSamples)

            var phase = 0.0
            for (s in 0 until totalSamples) {
                val progress = s.toFloat() / totalSamples.toFloat()
                // Frequency descends from 420 Hz down to 85 Hz
                val currentFreq = 420.0 - (335.0 * progress)
                phase += 2.0 * PI * currentFreq / sampleRate

                // Slight square distortion + subtle vibrato for retro arcade game-over feel
                val sinVal = sin(phase)
                val squareVal = if (sinVal > 0.0) 0.6 else -0.6
                val mixed = 0.6 * sinVal + 0.4 * squareVal

                val decay = exp(-2.8 * progress.toDouble())
                val sampleVal = (mixed * decay * 22000.0).toInt().coerceIn(-32767, 32767)
                pcm[s] = sampleVal.toShort()
            }

            playPcm(pcm, sampleRate)
        } catch (_: Exception) {
            // Silent fallback
        }
    }

    private fun playPcm(pcm: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            val bufferSize = pcm.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize.coerceAtLeast(1024))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()

            // Wait until playback ends, then release
            val playDurationMs = (pcm.size * 1000L) / sampleRate + 50L
            Thread.sleep(playDurationMs)
        } catch (_: Exception) {
            // Ignore
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
        }
    }
}
