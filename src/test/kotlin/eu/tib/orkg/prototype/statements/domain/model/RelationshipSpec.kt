package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("A Relationship")
class RelationshipSpec {
    @Nested
    @DisplayName("when constructed")
    inner class Constructor {
        @Test
        @DisplayName("fails on an empty string")
        fun withEmptyString() {
            Assertions.assertThatExceptionOfType(
                IllegalArgumentException::class.java
            )
                .isThrownBy {
                    Relationship("")
                }
                .withMessage("The value cannot be empty")
        }

        @Test
        @DisplayName("fails on a blank string")
        fun withBlankString() {
            Assertions.assertThatExceptionOfType(
                IllegalArgumentException::class.java
            )
                .isThrownBy {
                    Relationship(" \t")
                }
                .withMessage("The value cannot be blank")
        }
    }
}
