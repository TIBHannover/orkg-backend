package org.orkg.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class PubMedIDTest {
    @ParameterizedTest
    @MethodSource("validPubMedIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid wikidata id, it gets parsed correctly`(id: String, uri: String) {
        PubMedID.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validPubMedIDs")
    fun `Given a valid wikidata id uri, it gets parsed correctly`(id: String, uri: String) {
        PubMedID.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validPubMedIDs")
    fun `Given a valid wikidata id, it returns the correct uri`(id: String, uri: String) {
        PubMedID.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidPubMedIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid wikidata id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { PubMedID.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidPubMedIDs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid wikidata id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { PubMedID.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validPubMedIDs(): Stream<Arguments> = Stream.of(
            Arguments.of("17803225", "https://pubmed.ncbi.nlm.nih.gov/17803225/"),
            Arguments.of("52135", "https://pubmed.ncbi.nlm.nih.gov/52135/"),
        )

        @JvmStatic
        fun invalidPubMedIDs(): Stream<Arguments> = Stream.of(
            // Too long
            Arguments.of("178032257", "https://pubmed.ncbi.nlm.nih.gov/178032257/"),
            // Missing trailing slash
            Arguments.of("178032257", "https://pubmed.ncbi.nlm.nih.gov/178032257"),
            // Illegal character A
            Arguments.of("1780322A", "https://pubmed.ncbi.nlm.nih.gov/1780322A/"),
            // Empty
            Arguments.of("", "https://pubmed.ncbi.nlm.nih.gov/"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/"),
        )
    }
}
