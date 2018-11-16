package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*

@DisplayName("A predicate id")
class PredicateIdSpec {

    @Test
    @DisplayName("should fail if not prefixed with \"P\"")
    fun alphaNumericCharactersButNoPrefix() {
        assertThatIllegalArgumentException()
            .isThrownBy { PredicateId("1234") }
            .withMessage("""Value must start with "P"""")
    }

    @Test
    @DisplayName("should maintain value as long")
    fun shouldMaintainValueAsLong() {
        assertThat(PredicateId("P1234").value).isEqualTo(1234)
    }

    @Test
    @DisplayName("should maintain value when passed")
    fun shouldMaintainValueWhenPassed() {
        assertThat(PredicateId(1234).value).isEqualTo(1234)
    }
}
