package org.orkg.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class WikidataIdTest {
    @ParameterizedTest
    @MethodSource("validWikidataIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid wikidata id, it gets parsed correctly`(id: String, uri: String) {
        WikidataId.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validWikidataIds")
    fun `Given a valid wikidata id uri, it gets parsed correctly`(id: String, uri: String) {
        WikidataId.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validWikidataIds")
    fun `Given a valid wikidata id, it returns the correct uri`(id: String, uri: String) {
        WikidataId.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidWikidataIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid wikidata id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { WikidataId.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidWikidataIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid wikidata id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { WikidataId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validWikidataIds(): Stream<Arguments> = Stream.of(
            Arguments.of("Q416565", "https://www.wikidata.org/wiki/Q416565"),
            Arguments.of("Q45", "https://www.wikidata.org/wiki/Q45")
        )

        @JvmStatic
        fun invalidWikidataIds(): Stream<Arguments> = Stream.of(
            // Illegal character #
            Arguments.of("Q41656#", "https://www.wikidata.org/wiki/Q41656#"),
            // Illegal prefix
            Arguments.of("P41656#", "https://www.wikidata.org/wiki/P416565"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
