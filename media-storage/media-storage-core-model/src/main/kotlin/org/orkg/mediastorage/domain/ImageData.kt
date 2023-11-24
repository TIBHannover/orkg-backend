package org.orkg.mediastorage.domain

data class ImageData(val bytes: ByteArray) {
    fun isEmpty() = bytes.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode() = bytes.contentHashCode()
}
