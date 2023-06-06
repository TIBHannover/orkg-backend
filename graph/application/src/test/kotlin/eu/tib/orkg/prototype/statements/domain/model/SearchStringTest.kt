package eu.tib.orkg.prototype.statements.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SearchStringTest {
    @Nested
    inner class Fuzzy {
        @Nested
        inner class Query {
            @Test
            fun `Escapes special characters`() {
                FuzzySearchString("""+-&|!(){}[]^"~*?:\""").query shouldBe """\+\-\&\|\!\(\)\{\}\[\]\^\"\~\*\?\:\\"""
            }

            @Test
            fun `Allows + and - when used as a word prefix`() {
                FuzzySearchString("""+abc.""").query shouldBe """+abc."""
                FuzzySearchString("""-def.""").query shouldBe """-def."""
            }

            @Test
            fun `Inserts AND operator between words`() {
                FuzzySearchString("""some. label.""").query shouldBe """some. AND label."""
            }

            @Test
            fun `Inserts wildcards after words`() {
                FuzzySearchString("""some label""").query shouldBe """some* AND label*"""
            }

            @Test
            fun `Removes dashes when used within a word`() {
                FuzzySearchString("""few-shot""").query shouldBe """few* AND shot*"""
            }
        }

        @Nested
        inner class Input {
            @Test
            fun `Normalizes the input`() {
                FuzzySearchString("""  some  label ${'\t'} """).input shouldBe """some label"""
            }
        }
    }

    @Nested
    inner class Exact {
        @Nested
        inner class Query {
            @Test
            fun `Escapes special characters`() {
                ExactSearchString("""+-&|!(){}[]^"~*?:\""").query shouldBe """\+\-\&\|\!\(\)\{\}\[\]\^\"\~\*\?\:\\"""
            }
        }

        @Nested
        inner class Input {
            @Test
            fun `Normalizes the input`() {
                FuzzySearchString("""  some  label ${'\t'} """).input shouldBe """some label"""
            }
        }
    }
}
