package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.graph.domain.Literals.XSD.DATE
import org.orkg.graph.domain.Literals.XSD.DECIMAL
import org.orkg.graph.domain.Literals.XSD.INT
import org.orkg.graph.domain.Literals.XSD.STRING
import org.orkg.graph.domain.Literals.XSD.BOOLEAN
import org.orkg.graph.domain.Literals.XSD.FLOAT
import org.orkg.graph.domain.Literals.XSD.URI

class XSDValidationTest {
    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "563", "*/--5678", "\n"])
    fun `Given a string, when tested, it is always valid`(value: String) {
        STRING.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "451", "-0", "-5"])
    fun `Given an integer, when tested, it is valid`(value: String) {
        INT.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n"])
    fun `Given a malformed integer, when tested, it is not valid`(value: String) {
        INT.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["451", "451.7", "-451.7", "0", "-0", "-5", "-0.5", "-5.5", ".5", "-.5"])
    fun `Given a decimal, when tested, it is valid`(value: String) {
        DECIMAL.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n"])
    fun `Given a malformed decimal, when tested, it is not valid`(value: String) {
        DECIMAL.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["2011-12-03+01:00", "2011-12-03"])
    fun `Given a date, when tested, it is valid`(value: String) {
        DATE.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "10:15:30+01:00", "2011-12-03T10:15:30+01:00"])
    fun `Given a malformed date, when tested, it is not valid`(value: String) {
        DATE.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "false"])
    fun `Given a boolean, when tested, it is valid`(value: String) {
        BOOLEAN.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "\n", "1", "0", "TRUE", "FALSE", "True", "False"])
    fun `Given a malformed boolean, when tested, it is not valid`(value: String) {
        BOOLEAN.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["451", "451.7", "-451.7", "0", "-0", "-5", "-0.5", "-5.5", ".5", "-.5"])
    fun `Given a float, when tested, it is valid`(value: String) {
        FLOAT.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n"])
    fun `Given a malformed float, when tested, it is not valid`(value: String) {
        FLOAT.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["http://orkg.org", "https://orkg.org", "https://orkg.org/sub/path", "http://example.com", "https://example.com"])
    fun `Given a uri, when tested, it is valid`(value: String) {
        URI.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["http://orkg.org/resources?q=^"])
    fun `Given a malformed uri, when tested, it is not valid`(value: String) {
        URI.canParse(value) shouldBe false
    }
}
