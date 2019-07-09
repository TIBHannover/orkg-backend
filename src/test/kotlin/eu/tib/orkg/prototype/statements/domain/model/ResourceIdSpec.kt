package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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
    @DisplayName("should be equal to another instance of the same id")
    fun twoInstancesShouldBeEqual() {
        val one = ResourceId(1)
        val other = ResourceId(1)

        assertThat(one).isNotSameAs(other)
        assertThat(one).isEqualTo(other)
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
}
