package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ResearcherIdTest {
    @ParameterizedTest
    @MethodSource("validResearcherIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid researcher id, it gets parsed correctly`(id: String, uri: String) {
        ResearcherId.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validResearcherIds")
    fun `Given a valid researcher id uri, it gets parsed correctly`(id: String, uri: String) {
        ResearcherId.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validResearcherIds")
    fun `Given a valid researcher id, it returns the correct uri`(id: String, uri: String) {
        ResearcherId.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidResearcherIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid researcher id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ResearcherId.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidResearcherIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid researcher id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ResearcherId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validResearcherIds(): Stream<Arguments> = Stream.of(
            Arguments.of("K-8011-2013", "https://www.researcherid.com/rid/K-8011-2013"),
            Arguments.of("JAA-3403-2017", "https://www.researcherid.com/rid/JAA-3403-2017")
        )

        @JvmStatic
        fun invalidResearcherIds(): Stream<Arguments> = Stream.of(
            // Illegal character #
            Arguments.of("#-3403-2017", "https://www.researcherid.com/rid/#-3403-2017"),
            // Too many characters
            Arguments.of("JQKH-3403-2017", "https://www.researcherid.com/rid/JQKH-3403-2017"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
