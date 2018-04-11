package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("A subject id")
class SubjectIdSpec {
    @Test
    @DisplayName("should accept lower-case alpha-numeric characters")
    fun shouldAcceptLowerCaseAlphaNumericCharacters() {
        val subjectId = SubjectId("c0ffee")

        assertThat(subjectId.value).isEqualTo("c0ffee")
    }

    @Test
    @DisplayName("should not accept input with non-alpha-numeric characters")
    fun illegalCharacters() {
        assertThatIllegalArgumentException()
            .isThrownBy { SubjectId("notAlphaNumeric") }
            .withMessage("Value needs to be alpha-numeric")
    }

    @Test
    @DisplayName("should not be empty")
    fun shouldNotBeEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy { SubjectId("") }
            .withMessage("Value cannot be empty")
    }

    @Test
    @DisplayName("should not be blank")
    fun shouldNotBeBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy { SubjectId("  \t") }
            .withMessage("Value cannot be blank")
    }
}
