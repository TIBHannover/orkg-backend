package org.orkg.graph.adapter.output.neo4j

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString

internal class SearchStringQueryTest {
    @Nested
    inner class Fuzzy {
        @Test
        fun `Parses empty strings as wildcard`() {
            FuzzySearchString("").query shouldBe "*"
        }

        @Test
        fun `Removes special characters`() {
            FuzzySearchString("""+-&|!(){}[]^"~*?:\""").query shouldBe "*"
        }

        @Test
        fun `Allows + and - when used as a word prefix`() {
            FuzzySearchString("""+abc""").query shouldBe """+abc"""
            FuzzySearchString("""-def""").query shouldBe """-def"""
        }

        @Test
        fun `Inserts AND operator between words`() {
            FuzzySearchString("""some label""").query shouldBe """*some* AND *label*"""
        }

        @Test
        fun `Inserts wildcards before and after words`() {
            FuzzySearchString("""some label""").query shouldBe """*some* AND *label*"""
        }

        @Test
        fun `converts the given string correctly`() {
            FuzzySearchString("""* ?irus few/zero-shot - abc +def: -ghi""").query shouldBe "*irus* AND *few* AND *zero* AND *shot* AND *abc* AND +def AND -ghi"
        }

        @Test
        fun `Parses single character strings correctly`() {
            FuzzySearchString("""a""").query shouldBe "*a*"
        }

        @Test
        fun `Parses blank blank strings correctly`() {
            FuzzySearchString("""    """).query shouldBe "*"
        }
    }

    @Nested
    inner class Exact {
        @Test
        fun `Parses empty strings as wildcard`() {
            ExactSearchString("").query shouldBe "*"
        }

        @Test
        fun `Escapes special characters`() {
            ExactSearchString("""+-&|!(){}[]^"~*?:\""").query shouldBe """\+\-\&\|\!\(\)\{\}\[\]\^\"\~\*\?\:\\"""
        }
    }
}
