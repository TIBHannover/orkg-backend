package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("A subject id")
class ResourceIdSpec {
    @Test
    @DisplayName("can not be less than zero")
    fun canNotBeLessThanZero() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId(-1) }
            .withMessage("ID must be greater than or equal to zero")
    }

    @Test
    @DisplayName("should be greater than zero")
    fun shouldBeGreaterThanZero() {
        assertThatCode { ResourceId(1) }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("should accept MAX_VALUE")
    fun shouldAcceptMaxLong() {
        assertThatCode { ResourceId(Long.MAX_VALUE) }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("should be equal to a clone of the same id")
    fun twoClonesShouldBeEqual() {
        val one = ResourceId(1)
        val clone = one.copy()

        assertThat(one).isNotSameAs(clone)
        assertThat(one).isEqualTo(clone)
    }

    @Test
    @DisplayName("should represent internal value when given long")
    fun shouldRepresentInternalValueWhenGivenLong() {
        assertThat(ResourceId(42).toString()).isEqualTo("R42")
    }

    @Test
    fun `should forbid injection attacks`() {
        assertThrows<IllegalArgumentException> {
            ResourceId("{id: '\\'}; MATCH ( n ) DETACH DELETE n")
        }
    }

    @Test
    fun `should accept alphanumeric, dashes and underscores`() {
        assertDoesNotThrow {
            ResourceId("iua:sdne98798-qdas-a-__2eqw8a:sBUAD")
        }
    }
}
