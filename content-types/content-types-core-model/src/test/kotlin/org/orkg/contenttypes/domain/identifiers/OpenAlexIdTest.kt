package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@Suppress("HttpUrlsUsage")
class OpenAlexIdTest {
    @ParameterizedTest
    @MethodSource("validOpenAlexIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid open alex id, it gets parsed correctly`(openAlexId: String, uri: String) {
        OpenAlexId.of(openAlexId).value shouldBe openAlexId
    }

    @ParameterizedTest
    @MethodSource("validOpenAlexIds")
    fun `Given a valid open alex id uri, it gets parsed correctly`(openAlexId: String, uri: String) {
        OpenAlexId.of(uri).value shouldBe openAlexId
    }

    @ParameterizedTest
    @MethodSource("validOpenAlexIds")
    fun `Given a valid open alex id, it returns the correct uri`(openAlexId: String, uri: String) {
        OpenAlexId.of(openAlexId).uri shouldBe "https://openalex.org/$openAlexId"
    }

    @ParameterizedTest
    @MethodSource("invalidOpenAlexIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid open alex id, it throws an exception`(openAlexId: String, uri: String) {
        assertThrows<IllegalArgumentException> { OpenAlexId.of(openAlexId) }
    }

    @ParameterizedTest
    @MethodSource("invalidOpenAlexIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid open alex id uri, it throws an exception`(openAlexId: String, uri: String) {
        assertThrows<IllegalArgumentException> { OpenAlexId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validOpenAlexIds(): Stream<Arguments> = Stream.of(
            Arguments.of("A5071765665", "https://openalex.org/authors/A5071765665"),
            Arguments.of("C5071765665", "https://openalex.org/authors/C5071765665"),
            Arguments.of("I5071765665", "https://openalex.org/authors/I5071765665"),
            Arguments.of("W5071765665", "https://openalex.org/authors/W5071765665"),
            Arguments.of("T5071765665", "https://openalex.org/authors/T5071765665"),
            Arguments.of("S5071765665", "https://openalex.org/authors/S5071765665"),
            Arguments.of("P5071765665", "https://openalex.org/authors/P5071765665"),
            Arguments.of("F5071765665", "https://openalex.org/authors/F5071765665"),
            Arguments.of("a5071765665", "https://openalex.org/authors/a5071765665"),
            Arguments.of("c5071765665", "https://openalex.org/authors/c5071765665"),
            Arguments.of("i5071765665", "https://openalex.org/authors/i5071765665"),
            Arguments.of("w5071765665", "https://openalex.org/authors/w5071765665"),
            Arguments.of("t5071765665", "https://openalex.org/authors/t5071765665"),
            Arguments.of("s5071765665", "https://openalex.org/authors/s5071765665"),
            Arguments.of("p5071765665", "https://openalex.org/authors/p5071765665"),
            Arguments.of("f5071765665", "https://openalex.org/authors/f5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/concepts/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/institutions/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/venues/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/works/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/sources/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/publishers/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/funders/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/topics/A5071765665"),
            Arguments.of("A5071765665", "http://openalex.org/authors/A5071765665"),
            Arguments.of("A5071765665", "https://openalex.org/A5071765665"),
            Arguments.of("A5071765665", "http://explore.openalex.org/authors/A5071765665"),
            Arguments.of("A5071765665", "https://explore.openalex.org/authors/A5071765665"),
            Arguments.of("A5071765665", "http://explore.openalex.org/A5071765665"),
            Arguments.of("A5071765665", "https://explore.openalex.org/A5071765665"),
        )

        @JvmStatic
        fun invalidOpenAlexIds(): Stream<Arguments> = Stream.of(
            // Letters used
            Arguments.of("A50717656AB", "https://openalex.org/authors/A50717656AB"),
            // Invalid first letter
            Arguments.of("X5071765665", "https://openalex.org/authors/X5071765665"),
            // More than 10 digits
            Arguments.of("A50717656650", "https://openalex.org/authors/A50717656650"),
            // Less than 4 digits
            Arguments.of("A507", "https://openalex.org/authors/A507"),
            // First digit is a 0
            Arguments.of("A0071765665", "https://openalex.org/authors/A0071765665"),
            // empty
            Arguments.of("", "https://openalex.org/authors/")
        )
    }
}
