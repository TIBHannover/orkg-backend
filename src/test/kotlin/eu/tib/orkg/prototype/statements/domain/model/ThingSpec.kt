package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("A Thing")
class ThingSpec {
    @Nested
    @DisplayName("when constructed")
    inner class Constructor {
        @Test
        @DisplayName("fails on an empty string")
        fun withEmptyString() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy {
                    Thing("")
                }
                .withMessage("The value cannot be empty")
        }

        @Test
        @DisplayName("fails on a blank string")
        fun withBlankString() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy {
                    Thing(" \t")
                }
                .withMessage("The value cannot be blank")
        }
    }
}
