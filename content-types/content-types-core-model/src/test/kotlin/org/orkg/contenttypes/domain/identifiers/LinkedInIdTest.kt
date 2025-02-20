package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class LinkedInIdTest {
    @ParameterizedTest
    @MethodSource("validLinkedInIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid linkedin id, it gets parsed correctly`(id: String, uri: String) {
        LinkedInId.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validLinkedInIds")
    fun `Given a valid linkedin id uri, it gets parsed correctly`(id: String, uri: String) {
        LinkedInId.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validLinkedInIds")
    fun `Given a valid linkedin id, it returns the correct uri`(id: String, uri: String) {
        LinkedInId.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidLinkedInIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid linkedin id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { LinkedInId.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidLinkedInIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid linkedin id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { LinkedInId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validLinkedInIds(): Stream<Arguments> = Stream.of(
            Arguments.of("author", "https://www.linkedin.com/in/author/"),
            Arguments.of("author-with-dashes-and-numb3rs_", "https://www.linkedin.com/in/author-with-dashes-and-numb3rs_/")
        )

        @JvmStatic
        fun invalidLinkedInIds(): Stream<Arguments> = Stream.of(
            // Illegal character #
            Arguments.of("author#", "https://www.linkedin.com/in/author#/"),
            // Does not follow scheme at all
            Arguments.of("example~", "https://example.com/")
        )
    }
}
