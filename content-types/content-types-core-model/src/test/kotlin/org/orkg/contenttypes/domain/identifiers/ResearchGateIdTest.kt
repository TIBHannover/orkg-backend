package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ResearchGateIdTest {
    @ParameterizedTest
    @MethodSource("validResearchGateIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid research gate id, it gets parsed correctly`(id: String, uri: String) {
        ResearchGateId.of(id).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validResearchGateIds")
    fun `Given a valid research gate id uri, it gets parsed correctly`(id: String, uri: String) {
        ResearchGateId.of(uri).value shouldBe id
    }

    @ParameterizedTest
    @MethodSource("validResearchGateIds")
    fun `Given a valid research gate id, it returns the correct uri`(id: String, uri: String) {
        ResearchGateId.of(id).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidResearchGateIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid research gate id, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ResearchGateId.of(id) }
    }

    @ParameterizedTest
    @MethodSource("invalidResearchGateIds")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid research gate id uri, it throws an exception`(id: String, uri: String) {
        assertThrows<IllegalArgumentException> { ResearchGateId.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validResearchGateIds(): Stream<Arguments> = Stream.of(
            Arguments.of("FirstName-LastName-5", "https://researchgate.net/profile/FirstName-LastName-5"),
            Arguments.of("Some_Author", "https://researchgate.net/profile/Some_Author"),
        )

        @JvmStatic
        fun invalidResearchGateIds(): Stream<Arguments> = Stream.of(
            // Illegal character #
            Arguments.of("FirstName-LastName-#", "https://researchgate.net/profile/FirstName-LastName-#"),
            // Does not follow scheme at all
            Arguments.of("example~", "https://example.com/")
        )
    }
}
