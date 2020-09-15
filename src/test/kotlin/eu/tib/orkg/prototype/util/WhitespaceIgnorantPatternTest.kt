package eu.tib.orkg.prototype.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("WhitespaceIgnorantPattern")
class WhitespaceIgnorantPatternTest {
    @Test
    @DisplayName("A string with spaces should be transformed into a regex")
    fun aStringWithSpacesShouldBeTransformedIntoARegex() {
        val pattern = WhitespaceIgnorantPattern("a string with whitespace").toString()
        assertThat(pattern).isEqualTo("""a\s+string\s+with\s+whitespace""")
    }

    @Test
    @DisplayName("A string with whitespace characters should not be transformed into a regex")
    fun aStringWithWhitespaceCharactersShouldBeTransformedIntoARegex() {
        val pattern = WhitespaceIgnorantPattern("a string\twith\n    whitespace").toString()
        assertThat(pattern).isNotEqualTo("""a\s+string\s+with\s+whitespace""")
    }

    @Test
    @DisplayName("should compose with other helpers")
    fun shouldComposeWithOtherHelpers() {
        assertThat(WhitespaceIgnorantPattern(WhitespaceIgnorantPattern("foo")).toString()).isEqualTo("foo")
    }
}
