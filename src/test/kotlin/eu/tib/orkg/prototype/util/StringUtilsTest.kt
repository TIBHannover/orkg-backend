package eu.tib.orkg.prototype.util

import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

@DisplayName("Regex utils")
class StringUtilsTest {

    @ParameterizedTest(name = """{0} operator ("{1}") is correctly escaped""")
    @ArgumentsSource(SpecialCharsArgumentsProvider::class)
    @DisplayName("should escape special chars")
    @Suppress("UNUSED_PARAMETER") // because "name" is unused in the test, but used for improving the description
    fun shouldEscapeSpecialChar(name: String, input: String, expected: String) {
        assertThat(EscapedRegex(input).toString()).isEqualTo(expected)
    }

    @Test
    @DisplayName("shouldn't escape any char")
    fun shouldNotEscapeAnything() {
        assertThat(EscapedRegex("http://clean string & needs_nothing").toString())
            .isEqualTo("http://clean string & needs_nothing")
    }

    /**
     * Helper class to generate arguments for the parameterized test.
     *
     * Note: This was created because [org.junit.jupiter.params.provider.CsvSource] turned out to be unreadable.
     */
    internal class SpecialCharsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments>? =
            Stream.of(
                // name, input, expected
                Arguments.of("left named-capturing group", """<""", """\<"""),
                Arguments.of("right named-capturing group", """>""", """\>"""),
                Arguments.of("left group", """(""", """\("""),
                Arguments.of("right group", """)""", """\)"""),
                Arguments.of("left character class", """[""", """\["""),
                Arguments.of("right character class", """]""", """\]"""),
                Arguments.of("left counter", """{""", """\{"""),
                Arguments.of("right counter", """}""", """\}"""),
                Arguments.of("quotation", """\""", """\\"""),
                Arguments.of("negation/beginning of line", """^""", """\^"""),
                Arguments.of("range", """-""", """\-"""),
                Arguments.of("assigment", """=""", """\="""),
                Arguments.of("substitution/end of line", """$""", """\$"""),
                Arguments.of("negation", """!""", """\!"""),
                Arguments.of("alternative", """|""", """\|"""),
                Arguments.of("zero or once quantification", """?""", """\?"""),
                Arguments.of("zero or more quantification", """*""", """\*"""),
                Arguments.of("one or more quantification", """+""", """\+"""),
                Arguments.of("any character", """.""", """\.""")
            )
    }
}
