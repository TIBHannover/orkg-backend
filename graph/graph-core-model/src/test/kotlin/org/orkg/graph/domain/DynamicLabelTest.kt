package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.graph.domain.DynamicLabel.PlaceholderComponent
import org.orkg.graph.domain.DynamicLabel.SectionComponent
import org.orkg.graph.domain.DynamicLabel.TextComponent

private const val PERSON_TRAVELS_BY_TRANSPORTATION_METHOD_FROM_LOCATION_TO_LOCATION_ON_DATETIME =
    """[ {0} ]travels[by {1} ][from {2} ][to {3} ][on {4} ]"""

class DynamicLabelTest {
    @Test
    fun `Given a dynamic label template, when parsing a simple string, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("simple string")
        dynamicLabel.components shouldBe listOf(TextComponent("simple string"))
    }

    @Test
    fun `Given a dynamic label template, when parsing an empty string, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("")
        dynamicLabel.components shouldBe emptyList()
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that starts with an escaped curly brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""\{simple string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""{simple string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that starts with an escaped square brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""\[simple string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[simple string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that starts with an escaped backslash, it gets parsed correctly and escapes the backslash`() {
        val dynamicLabel = DynamicLabel("""\\simple string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""\simple string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an escaped curly brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""simple\{string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple{string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an escaped square brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""simple\[string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple[string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an escaped backslash, it gets parsed correctly and escapes the backslash`() {
        val dynamicLabel = DynamicLabel("""simple\\string""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple\string"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that ends with an escaped curly brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""simple string\{""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple string{"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that ends with an escaped square brackets, it gets parsed correctly and escapes the bracket`() {
        val dynamicLabel = DynamicLabel("""simple string\[""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple string["""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that ends with an escaped backslash, it gets parsed correctly and escapes the backslash`() {
        val dynamicLabel = DynamicLabel("""simple string\\""")
        dynamicLabel.components shouldBe listOf(TextComponent("""simple string\"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains a backslash, it only escapes opening brackets and backslashes`() {
        val dynamicLabel = DynamicLabel("""\{ \} \[ \] \\ \a""")
        dynamicLabel.components shouldBe listOf(TextComponent("""{ \} [ \] \ \a"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an invalid placeholder, it only parses a single text component`() {
        val dynamicLabel = DynamicLabel("""prefix {} postfix""")
        dynamicLabel.components shouldBe listOf(TextComponent("""prefix {} postfix"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an invalid section, it only parses a single text component`() {
        val dynamicLabel = DynamicLabel("""[prefix {} postfix]""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[prefix {} postfix]"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an escaped placeholder, it only parses a single text component`() {
        val dynamicLabel = DynamicLabel("""\{key}""")
        dynamicLabel.components shouldBe listOf(TextComponent("""{key}"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a simple string that contains an escaped section, it only parses a single text component`() {
        val dynamicLabel = DynamicLabel("""\[prefix \{key} postfix]""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[prefix {key} postfix]"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a placeholder, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""{abc}""")
        dynamicLabel.components shouldBe listOf(PlaceholderComponent("abc"))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a placeholder, it trims the placeholder key`() {
        val dynamicLabel = DynamicLabel("""{ abc  }""")
        dynamicLabel.components shouldBe listOf(PlaceholderComponent("abc"))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a placeholder, it parses escaped characters correctly`() {
        val dynamicLabel = DynamicLabel("""{abc\}def}""")
        dynamicLabel.components shouldBe listOf(PlaceholderComponent("""abc}def"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[prefix {key} postfix]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "prefix", "postfix"))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section without a preposition, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[{key} postfix]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "", "postfix"))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section without a postposition, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[prefix {key}]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "prefix", ""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section that contains just a key, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[{key}]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "", ""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section with a blank preposition, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[ ${'\t'} {key} postfix]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "", "postfix"))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section with a blank postposition, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[prefix {key} ${'\t'} ]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "prefix", ""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section with a blank preposition and postposition, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""[ ${'\t'} {key} ${'\t'} ]""")
        dynamicLabel.components shouldBe listOf(SectionComponent("key", "", ""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a nested section, it does not parse nested sections`() {
        val dynamicLabel = DynamicLabel("""[ [ pre1 {key}post1] pre2 {key}post2]""")
        dynamicLabel.components shouldBe listOf(
            SectionComponent("key", "[ pre1", "post1"),
            TextComponent(" pre2 "),
            PlaceholderComponent("key"),
            TextComponent("post2]"),
        )
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that multiple components, it gets parsed correctly`() {
        val dynamicLabel = DynamicLabel("""text [ pre {key}post] {placeholder}""")
        dynamicLabel.components shouldBe listOf(
            TextComponent("text "),
            SectionComponent("key", "pre", "post"),
            TextComponent(" "),
            PlaceholderComponent("placeholder")
        )
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished placeholder, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""{unfinished""")
        dynamicLabel.components shouldBe listOf(TextComponent("""{unfinished"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished section without a preposition, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[{unfinished""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[{unfinished"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished section with a preposition, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[preposition{unfinished""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[preposition{unfinished"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished section without a preposition and without a position, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[{unfinished}""")
        dynamicLabel.components shouldBe listOf(
            TextComponent("["),
            PlaceholderComponent("unfinished")
        )
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished section with a preposition but without a postposition, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[preposition{unfinished}""")
        dynamicLabel.components shouldBe listOf(
            TextComponent("""[preposition"""),
            PlaceholderComponent("unfinished")
        )
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains an unfinished section with a preposition and a postposition, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[preposition{unfinished}postposition""")
        dynamicLabel.components shouldBe listOf(
            TextComponent("""[preposition"""),
            PlaceholderComponent("unfinished"),
            TextComponent("postposition"),
        )
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section without a key, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[text]""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[text]"""))
    }

    @Test
    fun `Given a dynamic label template, when parsing a string that contains a section with an empty key, it just parses it as text`() {
        val dynamicLabel = DynamicLabel("""[{}]""")
        dynamicLabel.components shouldBe listOf(TextComponent("""[{}]"""))
    }

    @Test
    fun `Given a dynamic label template, when redering, it arranges whitespaces correctly`() {
        val dynamicLabel = DynamicLabel(PERSON_TRAVELS_BY_TRANSPORTATION_METHOD_FROM_LOCATION_TO_LOCATION_ON_DATETIME)
        val valueMap = mapOf(
            "0" to listOf("Person"),
            "1" to listOf("train"),
            "2" to listOf("Hanover"),
            "3" to listOf("Berlin"),
            "4" to listOf("29.07.2024")
        )
        dynamicLabel.render(valueMap) shouldBe """Person travels by train from Hanover to Berlin on 29.07.2024"""
    }

    @Test
    fun `Given a dynamic label template, when redering, it does not format missing sections`() {
        val dynamicLabel = DynamicLabel(PERSON_TRAVELS_BY_TRANSPORTATION_METHOD_FROM_LOCATION_TO_LOCATION_ON_DATETIME)
        val valueMap = mapOf(
            "0" to listOf("Person"),
            "1" to listOf("train"),
            "2" to listOf("Hanover")
        )
        dynamicLabel.render(valueMap) shouldBe """Person travels by train from Hanover"""
    }

    @Test
    fun `Given a dynamic label template, when redering, it formats placeholders correctly`() {
        val dynamicLabel = DynamicLabel("""{0} {1}""")
        val valueMap = mapOf(
            "0" to listOf("Person")
        )
        dynamicLabel.render(valueMap) shouldBe """Person {1}"""
    }

    @Test
    fun `Given a dynamic label template, when redering, it formats multiple values correctly`() {
        val dynamicLabel = DynamicLabel("""{0} travel [by {1}] [to {2}] on {3}""")
        val valueMap = mapOf(
            "0" to listOf("Person 1", "Person 2", "Person 3"),
            "1" to listOf("Train", "Bus", "Bike"),
            "2" to listOf("Berlin", "Hannover"),
            "3" to listOf("29.07.2024", "30.07.2024")
        )
        dynamicLabel.render(valueMap) shouldBe """Person 1, Person 2 and Person 3 travel by Train, Bus and Bike to Berlin and Hannover on 29.07.2024 and 30.07.2024"""
    }
}
