package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ObjectTest {

    @Test
    fun objectOfResourceType_WhenDifferent_ShouldCompareById() {
        val smaller = Object.Resource(ResourceId("R1"))
        val greater = Object.Resource(ResourceId("R2"))

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun objectOfResourceType_WhenEqual_ShouldCompareById() {
        val one = Object.Resource(ResourceId("R1"))
        val other = Object.Resource(ResourceId("R1"))

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun objectOfLiteralType_WhenDifferent_ShouldCompareByValue() {
        val smaller = Object.Literal(LiteralId("L1"))
        val greater = Object.Literal(LiteralId("L2"))

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun objectOfLiteralType_WhenEqual_ShouldCompareByValue() {
        val one = Object.Literal(LiteralId("L1"))
        val other = Object.Literal(LiteralId("L1"))

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun objectOfDifferentTypes_ResourceShouldCompareLessThanLiterals() {
        val resource: Object = Object.Resource(ResourceId("R1"))
        val literal: Object = Object.Literal(LiteralId("L1"))

        assertThat(resource).isLessThan(literal)
        assertThat(literal).isGreaterThan(resource)
    }
}
