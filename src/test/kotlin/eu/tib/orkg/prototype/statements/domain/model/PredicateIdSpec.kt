package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("A predicate id")
class PredicateIdSpec {

    @Nested
    @DisplayName("starting with \"P\"")
    inner class Prefix {

        @Test
        @DisplayName("should continue with lower-case alpha-numeric characters")
        fun shouldAcceptLowerCaseAlphaNumericCharacters() {
            val subjectId = PredicateId("Pc0ffee")

            assertThat(subjectId.toString()).isEqualTo("Pc0ffee")
        }

        @Test
        @DisplayName("should not accept non-alpha-numeric characters")
        fun illegalCharacters() {
            assertThatIllegalArgumentException()
                .isThrownBy { PredicateId("PnotAlphaNumeric") }
                .withMessage("""Value starts with "P" but is not alpha-numeric afterwards""")
        }
    }

    @Test
    @DisplayName("should fail if not prefixed with \"P\"")
    fun alphaNumericCharactersButNoPrefix() {
        assertThatIllegalArgumentException()
            .isThrownBy { PredicateId("c0ffee") }
            .withMessage("""Value needs to start with "P"""")
    }

    @Test
    @DisplayName("should not be empty")
    fun shouldNotBeEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy { PredicateId("") }
            .withMessage("Value cannot be empty")
    }

    @Test
    @DisplayName("should not be blank")
    fun shouldNotBeBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy { PredicateId("  \t") }
            .withMessage("Value cannot be blank")
    }
}
