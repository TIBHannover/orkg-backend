package org.orkg.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class HandleTest {
    @ParameterizedTest
    @MethodSource("validHandles")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid handle, it gets parsed correctly`(handle: String, uri: String) {
        Handle.of(handle).value shouldBe handle
    }

    @ParameterizedTest
    @MethodSource("validHandles")
    fun `Given a valid handle uri, it gets parsed correctly`(handle: String, uri: String) {
        Handle.of(uri).value shouldBe handle
    }

    @ParameterizedTest
    @MethodSource("validHandles")
    fun `Given a valid handle, it returns the correct uri`(handle: String, uri: String) {
        Handle.of(handle).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidHandles")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid handle, it throws an exception`(handle: String, uri: String) {
        assertThrows<IllegalArgumentException> { Handle.of(handle) }
    }

    @ParameterizedTest
    @MethodSource("invalidHandles")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid handle uri, it throws an exception`(handle: String, uri: String) {
        assertThrows<IllegalArgumentException> { Handle.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validHandles(): Stream<Arguments> = Stream.of(
            Arguments.of("20.1000/112", "https://hdl.handle.net/20.1000/112"),
            Arguments.of("20.1000/112aa", "https://hdl.handle.net/20.1000/112aa"),
            Arguments.of("20.10aa/112aa", "https://hdl.handle.net/20.10aa/112aa"),
            Arguments.of("abc20.10aa/112aa", "https://hdl.handle.net/abc20.10aa/112aa"),
        )

        @JvmStatic
        fun invalidHandles(): Stream<Arguments> = Stream.of(
            // Missing suffix
            Arguments.of("20.15641", "https://hdl.handle.net/11.48366"),
            // Suffix too short
            Arguments.of("20.15641/1", "https://hdl.handle.net/11.48366/1"),
            // Illegal characters in prefix
            Arguments.of("20.15641.5/1", "https://hdl.handle.net/20.15641.5/1"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
