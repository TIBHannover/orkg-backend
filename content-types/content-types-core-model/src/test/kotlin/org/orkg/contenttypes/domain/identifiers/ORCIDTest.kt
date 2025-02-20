package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ORCIDTest {
    @ParameterizedTest
    @MethodSource("validORCIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid orcid, it gets parsed correctly`(id: String, uri: String) {
        ORCID.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validORCIDs")
    fun `Given a valid orcid uri, it gets parsed correctly`(id: String, uri: String) {
        ORCID.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validORCIDs")
    fun `Given a valid orcid, it returns the correct uri`(id: String, uri: String) {
        ORCID.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidORCIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid orcid, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ORCID.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidORCIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid orcid uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ORCID.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validORCIDs(): Stream<Arguments> = Stream.of(
            Arguments.of("0000-0001-5109-3700", "https://orcid.org/0000-0001-5109-3700"),
            Arguments.of("0000-0001-5109-370X", "https://orcid.org/0000-0001-5109-370X")
        )

        @JvmStatic
        fun invalidORCIDs(): Stream<Arguments> = Stream.of(
            // Illegal character A
            Arguments.of("A000-0001-5109-3700", "https://orcid.org/A000-0001-5109-3700"),
            // Illegal character A
            Arguments.of("0000-0001-5109-370A", "https://orcid.org/0000-0001-5109-370A"),
            // Longer than 19 characters
            Arguments.of("0000-0001-5109-37000", "https://orcid.org/A000-0001-5109-37000"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
