package eu.tib.orkg.prototype.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Whitespace sanitation")
internal class StringUtilsWhitespaceTest {
    @Test
    @DisplayName("should trim whitespace from the margins")
    fun shouldTrimWhitespaceFromTheMargins() {
        val sanitized = SanitizedWhitespace(" \t  whitespace-left-and-right\n   ").toString()
        assertThat(sanitized).isEqualTo("whitespace-left-and-right")
    }

    @Test
    @DisplayName("should replace multiple whitespace with space")
    fun shouldReplaceMultipleWhitespaceWithSpace() {
        val sanitized = SanitizedWhitespace("string  with \n multiple\twhitespace ").toString()
        assertThat(sanitized).isEqualTo("string with multiple whitespace")
    }
}
