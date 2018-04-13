package eu.tib.orkg.prototype.authentication.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("A user id")
class UserIdSpec {

    @Test
    @DisplayName("is generated when created without providing an ID")
    fun generatesNewUUID() {
        val userId = UserId()

        assertThat(userId.value).isNotNull()
    }

    @Test
    @DisplayName("does not generate the same ID when called twice")
    fun generatingTwoIdsGeneratesDifferentValues() {
        val id1 = UserId()
        val id2 = UserId()

        assertThat(id1.value).isNotEqualTo(id2.value)
    }

    @Test
    @DisplayName("preserves equality when cloned")
    fun clonedIdsAreEqual() {
        val userId = UserId()
        val clonedId = userId.copy()

        assertSoftly { softly ->
            softly.assertThat(clonedId).isNotSameAs(userId)
            softly.assertThat(clonedId).isEqualTo(userId)
        }
    }
}
