package com.innosage.android.audiorecorderengine

sealed class AudioEvent {
    data class PcmData(val data: ByteArray, val size: Int) : AudioEvent() {
        // Overriding equals and hashCode for ByteArray content comparison
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PcmData

            if (!data.contentEquals(other.data)) return false
            if (size != other.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + size
            return result
        }
    }
    object SwitchChunk : AudioEvent()
}