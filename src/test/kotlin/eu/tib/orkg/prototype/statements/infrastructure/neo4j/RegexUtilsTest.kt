package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Regex utils")
class RegexUtilsTest {

    @Test
    @DisplayName("should escape all the chars")
    fun shouldEscapeAll() {
        assertThat(escapeRegexString("""<(\[{\\^\-=$!|\]})?*+.>"""))
            .isEqualTo("""\<\(\\\[\{\\\\\^\\\-\=\$\!\|\\\]\}\)\?\*\+\.\>""")
    }

    @Test
    @DisplayName("shouldn't escape any char")
    fun shouldNotEscapeAnything() {
        assertThat(escapeRegexString("http://clean string & needs_nothing"))
            .isEqualTo("http://clean string & needs_nothing")
    }
}
