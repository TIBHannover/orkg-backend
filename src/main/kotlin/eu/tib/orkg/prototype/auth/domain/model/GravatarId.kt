package eu.tib.orkg.prototype.auth.domain.model

import java.math.BigInteger
import java.security.MessageDigest

/**
 * A ID that can be used to obtain data from Gravatar.
 *
 * Implemented as described in the section "[Creating the Hash](https://en.gravatar.com/site/implement/hash/)" in the
 * Gravatar documentation.
 *
 * Converting the ID to a [String] will return the zero-padded hash.
 *
 * @property email The email address to calculate the ID from.
 * @constructor Creates a new Gravatar ID for the given email address.
 */
data class GravatarId(private val email: String) {
    private val hashed: String by lazy {
        val processed = email.trim().toLowerCase()
        val md = MessageDigest.getInstance("MD5")
        BigInteger(1, md.digest(processed.toByteArray())).toString(16).padStart(32, '0')
    }

    /**
     * Convenience property to access the hashed value, instead of calling [toString].
     *
     * @return The hashed email address.
     */
    val id: String
        get() = hashed

    /**
     * Provides the URL to the Gravatar image for use in `<img>` tags.
     *
     * @return The URL to the image.
     */
    fun imageURL() = "https://www.gravatar.com/avatar/$hashed"

    override fun toString() = hashed
}
