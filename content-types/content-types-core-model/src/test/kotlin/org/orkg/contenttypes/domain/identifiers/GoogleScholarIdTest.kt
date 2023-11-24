package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class GoogleScholarIdTest {
    @ParameterizedTest
    @MethodSource("validGoogleScholarIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid google scholar id, it gets parsed correctly`(id: String, uri: String) {
        GoogleScholarId.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validGoogleScholarIds")
    fun `Given a valid google scholar id uri, it gets parsed correctly`(id: String, uri: String) {
        GoogleScholarId.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validGoogleScholarIds")
    fun `Given a valid google scholar id, it returns the correct uri`(id: String, uri: String) {
        GoogleScholarId.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidGoogleScholarIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid google scholar id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { GoogleScholarId.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidGoogleScholarIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid google scholar id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { GoogleScholarId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validGoogleScholarIds(): Stream<Arguments> = Stream.of(
            Arguments.of("CUO0vDcAAAAJ", "https://scholar.google.com/citations?user=CUO0vDcAAAAJ"),
            Arguments.of("QPdLuj8AAAAJ", "https://scholar.google.com/citations?user=QPdLuj8AAAAJ")
        )

        @JvmStatic
        fun invalidGoogleScholarIds(): Stream<Arguments> = Stream.of(
            // Illegal character #
            Arguments.of("#UO0vDcAAAAJ", "https://scholar.google.com/citations?user=#UO0vDcAAAAJ"),
            // Longer than 12 characters
            Arguments.of("QPdLuj8AAAAJA", "https://scholar.google.com/citations?user=QPdLuj8AAAAJA"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
