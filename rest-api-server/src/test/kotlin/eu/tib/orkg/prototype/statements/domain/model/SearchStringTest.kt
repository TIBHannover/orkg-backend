package eu.tib.orkg.prototype.statements.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class SearchStringTest {
    @Test
    fun `Special characters escaped correctly when not escaped`() {
        " ~ [ ] { } ^ : ".escapeFuzzySearchString() shouldBe """ \~ \[ \] \{ \} \^ \: """
        "a~b[c]d{e}f^g:h".escapeFuzzySearchString() shouldBe """a\~b\[c\]d\{e\}f\^g\:h"""
        "~[]{}^:".escapeFuzzySearchString() shouldBe """\~\[\]\{\}\^\:"""
    }

    @Test
    fun `Already escaped special characters are not escaped again`() {
        """ \~ \[ \] \{ \} \^ \: """.escapeFuzzySearchString() shouldBe """ \~ \[ \] \{ \} \^ \: """
        """a\~b\[c\]d\{e\}f\^g\:h""".escapeFuzzySearchString() shouldBe """a\~b\[c\]d\{e\}f\^g\:h"""
        """\~\[\]\{\}\^\:""".escapeFuzzySearchString() shouldBe """\~\[\]\{\}\^\:"""
    }

    @Test
    fun `Leading special characters are escaped but are not escaped inside the string`() {
        """???""".escapeFuzzySearchString() shouldBe """\???"""
        """***""".escapeFuzzySearchString() shouldBe """\***"""
    }
}
