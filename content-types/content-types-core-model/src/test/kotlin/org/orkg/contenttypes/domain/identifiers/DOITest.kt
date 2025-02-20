package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DOITest {
    @ParameterizedTest
    @MethodSource("validDOIs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid doi, it gets parsed correctly`(doi: String, uri: String) {
        DOI.of(doi).value shouldBe doi
    }

    @ParameterizedTest
    @MethodSource("validDOIs")
    fun `Given a valid doi uri, it gets parsed correctly`(doi: String, uri: String) {
        DOI.of(uri).value shouldBe doi
    }

    @ParameterizedTest
    @MethodSource("validDOIs")
    fun `Given a valid doi, it returns the correct uri`(doi: String, uri: String) {
        DOI.of(doi).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidDOIs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid doi, it throws an exception`(doi: String, uri: String) {
        assertThrows<IllegalArgumentException> { DOI.of(doi) }
    }

    @ParameterizedTest
    @MethodSource("invalidDOIs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid doi uri, it throws an exception`(doi: String, uri: String) {
        assertThrows<IllegalArgumentException> { DOI.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validDOIs(): Stream<Arguments> = Stream.of(
            Arguments.of("10.48366/r609337", "https://doi.org/10.48366/r609337"),
            Arguments.of("10.1093/ajae/aaq063", "https://doi.org/10.1093/ajae/aaq063"),
            Arguments.of("10.1080/10509585.2015.1092083", "https://doi.org/10.1080/10509585.2015.1092083")
        )

        @JvmStatic
        fun invalidDOIs(): Stream<Arguments> = Stream.of(
            // Illegal start of prefix (must be "10.")
            Arguments.of("11.48366/r609337", "https://doi.org/11.48366/r609337"),
            // Missing suffix
            Arguments.of("10.1093", "https://doi.org/10.1093"),
            // Letters used in prefix
            Arguments.of("10.XYZ/10509585.2015.1092083", "https://doi.org/10.XYZ/10509585.2015.1092083"),
            // Does not follow scheme at all
            Arguments.of("example", "https://example.com/")
        )
    }
}
