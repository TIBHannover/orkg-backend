package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*

@DisplayName("A subject id")
class ResourceIdSpec {
    @Test
    @DisplayName("can not be zero")
    fun canNotBeZero() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId(0) }
            .withMessage("Value must be greater than zero")
    }

    @Test
    @DisplayName("can not be less than zero")
    fun canNotBeLessThanZero() {
        assertThatIllegalArgumentException()
            .isThrownBy { ResourceId(-1) }
            .withMessage("Value must be greater than zero")
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
}
