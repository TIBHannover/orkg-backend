package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SearchStringTest {
    @Nested
    inner class Fuzzy {
        @Test
        fun `Normalizes the input`() {
            FuzzySearchString("""  some  label ${'\t'} """).input shouldBe """some label"""
        }
    }

    @Nested
    inner class Exact {
        @Test
        fun `Normalizes the input`() {
            FuzzySearchString("""  some  label ${'\t'} """).input shouldBe """some label"""
        }
    }
}
