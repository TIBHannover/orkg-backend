package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ISSNTest {
    @ParameterizedTest
    @MethodSource("validISSNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid issn, it gets parsed correctly`(issn: String, uri: String) {
        ISSN.of(issn).value shouldBe issn
    }

    @ParameterizedTest
    @MethodSource("validISSNs")
    fun `Given a valid issn uri, it gets parsed correctly`(issn: String, uri: String) {
        ISSN.of(uri).value shouldBe issn
    }

    @ParameterizedTest
    @MethodSource("validISSNs")
    fun `Given a valid issn, it returns the correct uri`(issn: String, uri: String) {
        ISSN.of(issn).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidISSNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid issn, it throws an exception`(issn: String, uri: String) {
        assertThrows<IllegalArgumentException> { ISSN.of(issn) }
    }

    @ParameterizedTest
    @MethodSource("invalidISSNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid issn uri, it throws an exception`(issn: String, uri: String) {
        assertThrows<IllegalArgumentException> { ISSN.of(uri) }
    }

    companion object {
        @JvmStatic
        @Suppress("HttpUrlsUsage")
        fun validISSNs(): Stream<Arguments> = Stream.of(
            Arguments.of("1245-6549", "https://portal.issn.org/resource/ISSN/1245-6549"),
            Arguments.of("1564-561X", "https://portal.issn.org/resource/ISSN/1564-561X")
        )

        @JvmStatic
        fun invalidISSNs(): Stream<Arguments> = Stream.of(
            // Missing dash
            Arguments.of("12456549", "https://portal.issn.org/resource/ISSN/12456549"),
            // Letters used
            Arguments.of("1245-654A", "https://portal.issn.org/resource/ISSN/1245-654A"),
            // More than 8 digits
            Arguments.of("124565490", "https://portal.issn.org/resource/ISSN/124565490"),
            // Less than 8 digits
            Arguments.of("1245654", "https://portal.issn.org/resource/ISSN/1245654"),
            // empty
            Arguments.of("", "https://portal.issn.org/resource/ISSN/")
        )
    }
}
