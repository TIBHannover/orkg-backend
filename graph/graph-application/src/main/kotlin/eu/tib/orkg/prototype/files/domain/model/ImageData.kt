package eu.tib.orkg.prototype.files.domain.model

data class ImageData(val bytes: ByteArray) {
    fun isEmpty() = bytes.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode() = bytes.contentHashCode()
}
