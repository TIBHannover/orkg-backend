package org.orkg.community.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// All hashes have been pre-computed on the command line with `echo -n INPUT | sha256sum`.
@DisplayName("A Gravatar ID")
internal class GravatarIdTest {
    @Test
    @DisplayName("should remove trailing whitespaces")
    fun shouldRemoveTrailingWhitespaces() {
        val id = GravatarId("\tuser@example.org \n ")
        assertThat(id.toString()).isEqualTo("d159ef624ed86697b4f1f3ff086aacddfdfd42d463a8003694f775e1e2d95e2c")
    }

    @Test
    @DisplayName("should convert to lowercase")
    fun shouldConvertToLowercase() {
        val id = GravatarId("UseR@example.org") // no hidden message here :P
        assertThat(id.toString()).isEqualTo("d159ef624ed86697b4f1f3ff086aacddfdfd42d463a8003694f775e1e2d95e2c")
    }

    @Test
    @DisplayName("should return hash as string representation")
    fun shouldReturnHashAsStringRepresentation() {
        val id = GravatarId("user@example.org")
        assertThat(id.toString()).isEqualTo("d159ef624ed86697b4f1f3ff086aacddfdfd42d463a8003694f775e1e2d95e2c")
    }

    @Test
    @DisplayName("should expand hash to 64 characters, prefixed with zero(s)")
    fun shouldExpandHashTo64CharactersPrefixedWithZeroS() {
        val id = GravatarId("user26@example.org") // carefully chosen to start with 4 zero-bits
        assertThat(id.toString()).hasSize(64)
        assertThat(id.toString()).isEqualTo("0c82608a4345bdb15ed45560c4fcb1bad9bd3799055ec3b783f9d46fa88c16cb")
    }

    @Test
    @DisplayName("""should provide the hashed value via the "id" property""")
    fun shouldProvideTheHashedValueViaTheIdProperty() {
        val id = GravatarId("user@example.org")
        assertThat(id.id).isEqualTo("d159ef624ed86697b4f1f3ff086aacddfdfd42d463a8003694f775e1e2d95e2c")
    }

    @Test
    @DisplayName("should provide the URL to the Gravatar image")
    fun shouldProvideTheUrlToTheGravatarImage() {
        val id = GravatarId("user@example.org")
        assertThat(id.imageURL()).isEqualTo("https://www.gravatar.com/avatar/d159ef624ed86697b4f1f3ff086aacddfdfd42d463a8003694f775e1e2d95e2c")
    }

    @Test
    @DisplayName("should force a default if email is not available")
    fun shouldForceADefaultIfEmailIsNotAvailable() {
        val id = GravatarId()
        assertThat(id.imageURL()).isEqualTo("https://www.gravatar.com/avatar/?d=mp&f=y")
    }
}
