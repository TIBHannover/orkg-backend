package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("A class id")
class ClassIdTest {
    @Test
    @DisplayName("can not be less than zero")
    fun canNotBeLessThanZero() {
        assertThatIllegalArgumentException()
            .isThrownBy { ClassId(-1) }
            .withMessage("Value must be greater than or equal to zero")
    }

    @Test
    @DisplayName("should be greater than zero")
    fun shouldBeGreaterThanZero() {
        assertThatCode { ClassId(1) }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("should accept MAX_VALUE")
    fun shouldAcceptMaxLong() {
        assertThatCode { ClassId(Long.MAX_VALUE) }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("should be equal to another instance of the same id")
    fun twoInstancesShouldBeEqual() {
        val one = ClassId(1)
        val other = ClassId(1)

        assertThat(one).isNotSameAs(other)
        assertThat(one).isEqualTo(other)
    }

    @Test
    @DisplayName("should be equal to a clone of the same id")
    fun twoClonesShouldBeEqual() {
        val one = ClassId(1)
        val clone = one.copy()

        assertThat(one).isNotSameAs(clone)
        assertThat(one).isEqualTo(clone)
    }

    @Test
    @DisplayName("should represent internal value when given long")
    fun shouldRepresentInternalValueWhenGivenLong() {
        assertThat(ClassId(42).toString()).isEqualTo("C42")
    }
}
