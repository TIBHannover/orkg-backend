package eu.tib.orkg.prototype.auth.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// All hashes have been pre-computed on the command line with "echo -n INPUT | md5 -".
@DisplayName("A Gravatar ID")
internal class GravatarIdTest {

    @Test
    @DisplayName("should remove trailing whitespaces")
    fun shouldRemoveTrailingWhitespaces() {
        val id = GravatarId("\tuser@example.org \n ")
        assertThat(id.toString()).isEqualTo("572c3489ea700045927076136a969e27")
    }

    @Test
    @DisplayName("should convert to lowercase")
    fun shouldConvertToLowercase() {
        val id = GravatarId("UseR@example.org") // no hidden message here :P
        assertThat(id.toString()).isEqualTo("572c3489ea700045927076136a969e27")
    }

    @Test
    @DisplayName("should return hash as string representation")
    fun shouldReturnHashAsStringRepresentation() {
        val id = GravatarId("user@example.org")
        assertThat(id.toString()).isEqualTo("572c3489ea700045927076136a969e27")
    }

    @Test
    @DisplayName("should expand hash to 32 characters, prefixed with zero(s)")
    fun shouldExpandHashTo32CharactersPrefixedWithZeroS() {
        val id = GravatarId("user14@example.org") // carefully chosen to start with 4 zero-bits
        assertThat(id.toString()).hasSize(32)
        assertThat(id.toString()).isEqualTo("0f355fa8237e411a325dad6ebf8a85a3")
    }

    @Test
    @DisplayName("""should provide the hashed value via the "id" property""")
    fun shouldProvideTheHashedValueViaTheIdProperty() {
        val id = GravatarId("user@example.org")
        assertThat(id.id).isEqualTo("572c3489ea700045927076136a969e27")
    }

    @Test
    @DisplayName("should provide the URL to the Gravatar image")
    fun shouldProvideTheUrlToTheGravatarImage() {
        val id = GravatarId("user@example.org")
        assertThat(id.imageURL()).isEqualTo("https://www.gravatar.com/avatar/572c3489ea700045927076136a969e27")
    }

    @Test
    @DisplayName("should force a default if email is not available")
    fun shouldForceADefaultIfEmailIsNotAvailable() {
        val id = GravatarId()
        assertThat(id.imageURL()).isEqualTo("https://www.gravatar.com/avatar/?d=mp&f=y")
    }
}
