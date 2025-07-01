package com.innosage.android.audiorecorderengine

object AudioConstants {
    const val AUDIO_FILE_EXTENSION = ".m4a"

    /**
     * 5-second chunk
     */
    const val DEFAULT_CHUNK_DURATION_MILLIS = 5_000L

    // AudioRecord configuration
    const val SAMPLE_RATE_HZ = 44100
    const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
    const val AUDIO_BIT_RATE = 128000 // 128 kbps for AAC
}
