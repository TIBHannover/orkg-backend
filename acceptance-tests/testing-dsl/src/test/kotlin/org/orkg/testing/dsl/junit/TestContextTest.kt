package org.orkg.testing.dsl.junit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.orkg.world.World
import java.lang.reflect.Method
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TestContextTest {
    @Test
    fun `produces different usernames when called from different test methods in the same class`() {
        @Suppress("unused")
        class FakeTest {
            fun dummyTestMethod() = Unit

            fun anotherDummyTestMethod() = Unit
        }

        val testInfoOne = FakeTestInfo(testClass = FakeTest::class.java, testMethod = "dummyTestMethod")
        val testInfoTwo = FakeTestInfo(testClass = FakeTest::class.java, testMethod = "anotherDummyTestMethod")
        assertThat(testInfoOne.getTestMethod().get()).isNotEqualTo(testInfoTwo.getTestMethod().get())
        val world = World.controlledSystem()
        // This simulates two different tests, with their own test contexts, but the same test class, by reusing the same RNG
        val contextOne = TestContext(testInfoOne, world)
        val contextTwo = TestContext(testInfoTwo, world)

        // Ask each context for a username
        val usernameOne = contextOne.lookupOrCreateUsername("alice")
        val usernameTwo = contextTwo.lookupOrCreateUsername("alice")

        assertThat(usernameOne).isNotEqualTo(usernameTwo)
    }

    @Test
    fun `produces different usernames when called from a test method with the same name in different test classes`() {
        class DummyTestClassOne {
            @Suppress("unused")
            fun dummyTestMethod() = Unit
        }

        class DummyTestClassTwo {
            @Suppress("unused")
            fun dummyTestMethod() = Unit
        }

        val testInfoOne = FakeTestInfo(testClass = DummyTestClassOne::class.java)
        val testInfoTwo = FakeTestInfo(testClass = DummyTestClassTwo::class.java)
        assertThat(testInfoOne.getTestClass().get()).isNotEqualTo(testInfoTwo.getTestClass().get())
        assertThat(testInfoOne.getTestMethod().get()).isNotEqualTo(testInfoTwo.getTestMethod().get())
        // This simulates two different tests in two different test classes, with different RNGs
        val contextOne = TestContext(testInfoOne, World.controlledSystem())
        val contextTwo = TestContext(testInfoTwo, World.controlledSystem())

        // Ask each context for a username
        val usernameOne = contextOne.lookupOrCreateUsername("alice")
        val usernameTwo = contextTwo.lookupOrCreateUsername("alice")

        assertThat(usernameOne).isNotEqualTo(usernameTwo)
    }

    internal data class FakeTestInfo(
        private val testMethod: String = "dummyTestMethod",
        private val testClass: Class<*>? = null,
        private val tags: Set<String> = emptySet(),
    ) : TestInfo {
        override fun getDisplayName(): String? =
            // This does not simulate the full signature, but we also do not need it.
            getTestMethod().getOrNull()?.let { "${it.name}()" }

        override fun getTags(): Set<String?>? = tags

        override fun getTestClass(): Optional<Class<*>> = Optional.ofNullable(testClass)

        override fun getTestMethod(): Optional<Method> {
            val method: Method = testClass?.methods?.find { it.name == testMethod }
                ?: error("Unable to find test method")
            return Optional.of(method)
        }
    }
}
