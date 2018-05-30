package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("A subject id")
class ResourceIdSpec {
    @Test
    @DisplayName("should accept lower-case alpha-numeric characters")
    fun shouldAcceptLowerCaseAlphaNumericCharacters() {
        val subjectId = ResourceId("c0ffee")

        assertThat(subjectId.toString()).isEqualTo("c0ffee")
    }

    @Test
    @DisplayName("should not accept input with non-alpha-numeric characters")
    fun illegalCharacters() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId("notAlphaNumeric") }
            .withMessage("Value needs to be alpha-numeric")
    }

    @Test
    @DisplayName("should not be empty")
    fun shouldNotBeEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId("") }
            .withMessage("Value cannot be empty")
    }

    @Test
    @DisplayName("should not be blank")
    fun shouldNotBeBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId("  \t") }
            .withMessage("Value cannot be blank")
    }


    @Test
    @DisplayName("should be equal to another instance of the same id")
    fun twoInstancesShouldBeEqual() {
        val one = ResourceId("c0ffee")
        val other = ResourceId("c0ffee")

        assertThat(one).isNotSameAs(other)
        assertThat(one).isEqualTo(other)
    }

    @Test
    @DisplayName("should be equal to a clone of the same id")
    fun twoClonesShouldBeEqual() {
        val one = ResourceId("c0ffee")
        val clone = one.copy()

        assertThat(one).isNotSameAs(clone)
        assertThat(one).isEqualTo(clone)
    }
}
