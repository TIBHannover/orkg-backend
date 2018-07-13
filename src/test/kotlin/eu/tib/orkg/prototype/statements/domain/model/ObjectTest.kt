package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ObjectTest {

    @Test
    fun objectOfResourceType_WhenDifferent_ShouldCompareById() {
        val smaller = Object.Resource(ResourceId("1"))
        val greater = Object.Resource(ResourceId("2"))

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun objectOfResourceType_WhenEqual_ShouldCompareById() {
        val one = Object.Resource(ResourceId("1"))
        val other = Object.Resource(ResourceId("1"))

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun objectOfLiteralType_WhenDifferent_ShouldCompareByValue() {
        val smaller = Object.Literal("aaa")
        val greater = Object.Literal("zzz")

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun objectOfLiteralType_WhenEqual_ShouldCompareByValue() {
        val one = Object.Literal("same value")
        val other = Object.Literal("same value")

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun objectOfDifferentTypes_ResourceShouldCompareLessThanLiterals() {
        val resource: Object = Object.Resource(ResourceId("1"))
        val literal: Object = Object.Literal("some value")

        assertThat(resource).isLessThan(literal)
        assertThat(literal).isGreaterThan(resource)
    }
}
