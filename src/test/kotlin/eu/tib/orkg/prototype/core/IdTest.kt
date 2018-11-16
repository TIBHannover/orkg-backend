package eu.tib.orkg.prototype.core

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*

interface IdTest<T : Any> {

    /**
     * Create a new ID instance.
     *
     * @return a new instance.
     */
    fun createId(): Id<T>

    /**
     * Create a new ID instance with the same value as in [createId].
     *
     * @return a new instance.
     */
    fun createEqualId(): Id<T>

    /**
     * Create a new ID instance with a different value as in [createId].
     *
     * @return a new instance.
     */
    fun createDifferentId(): Id<T>

    @Test
    @DisplayName("ID should be equal to another instance with the same number")
    fun idShouldBeEqualToAnotherInstanceWithTheSameNumber() {
        val instance = createId()
        val other = createEqualId()

        assertThat(instance).isNotSameAs(other)
        assertThat(instance).isEqualTo(other)
    }

    @Test
    @DisplayName("ID should not be equal to another instance with a different number")
    fun idShouldNotBeEqualToAnotherInstanceWithADifferentNumber() {
        val instance = createId()
        val other = createDifferentId()

        assertThat(instance).isNotEqualTo(other)
    }

    @Test
    @DisplayName("ID should have the same hashcode as another instance with the same number")
    fun idShouldHaveTheSameHashcodeAsAnotherInstanceWithTheSameNumber() {
        val instance = createId()
        val other = createEqualId()

        assertThat(instance).isNotSameAs(other)
        assertThat(instance.hashCode()).isEqualTo(other.hashCode())
    }

    @Test
    @DisplayName("ID should not have the same hashcode as another instance with a different value")
    fun idShouldNotHaveTheSameHashcodeAsAnotherInstanceWithADifferentValue() {
        val instance = createId()
        val other = createDifferentId()

        assertThat(instance.hashCode()).isNotEqualTo(other.hashCode())
    }

    @Test
    @DisplayName("ID should have the same string representation if they are equal")
    fun idShouldHaveTheSameStringRepresentationIfTheyAreEqual() {
        val instance = createId()
        val other = createEqualId()

        assertThat(instance).isEqualTo(other)
        assertThat(instance.toString()).isEqualTo(other.toString())
    }
}
