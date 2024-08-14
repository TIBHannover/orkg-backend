package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.graph.domain.Literals.XSD.BOOLEAN
import org.orkg.graph.domain.Literals.XSD.DATE
import org.orkg.graph.domain.Literals.XSD.DATE_TIME
import org.orkg.graph.domain.Literals.XSD.DECIMAL
import org.orkg.graph.domain.Literals.XSD.DOUBLE
import org.orkg.graph.domain.Literals.XSD.DURATION
import org.orkg.graph.domain.Literals.XSD.FLOAT
import org.orkg.graph.domain.Literals.XSD.INT
import org.orkg.graph.domain.Literals.XSD.STRING
import org.orkg.graph.domain.Literals.XSD.TIME
import org.orkg.graph.domain.Literals.XSD.URI

class XSDValidationTest {
    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "563", "*/--5678", "\n"])
    fun `Given a string, when tested, it is always valid`(value: String) {
        STRING.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "451", "-0", "-5", "4294967297", "-4294967297"])
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
    @ValueSource(strings = ["true", "false", "1", "0"])
    fun `Given a boolean, when tested, it is valid`(value: String) {
        BOOLEAN.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "\n", "TRUE", "FALSE", "True", "False"])
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
    @ValueSource(strings = ["451", "451.7", "-451.7", "0", "-0", "-5", "-0.5", "-5.5", ".5", "-.5"])
    fun `Given a double, when tested, it is valid`(value: String) {
        DOUBLE.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n"])
    fun `Given a malformed double, when tested, it is not valid`(value: String) {
        DOUBLE.canParse(value) shouldBe false
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

    @ParameterizedTest
    @ValueSource(strings = ["PT20.345S", "PT15M", "PT10H", "P2D", "P2DT3H4M", "PT-6H3M", "-PT6H3M", "-PT-6H+3M"])
    fun `Given a duration, when tested, it is valid`(value: String) {
        DURATION.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "PT15Z", "AT10H", "+-PT-6H+3M"])
    fun `Given a malformed duration, when tested, it is not valid`(value: String) {
        DURATION.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["2002-10-10T12:00:00-05:00", "2002-10-10T17:00:00Z", "2011-12-03T10:15:30", "2011-12-03T10:15:30+01:00"])
    fun `Given a date time, when tested, it is valid`(value: String) {
        DATE_TIME.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "2011-12-03T10:15:30+01:00[Europe/Paris]", "2002-25-10T12:00:00-05:00", "2002-10-10T12:00:00-25:00", "2002-10-10T12:00:00-05:70", "2002-10-10T28:00:00-05:00"])
    fun `Given a malformed date time, when tested, it is not valid`(value: String) {
        DATE_TIME.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["10:15:30+01:00", "10:15:30", "10:15:30.4587+01:00", "10:15:30.1565", "15:30+01:00"])
    fun `Given a time, when tested, it is valid`(value: String) {
        TIME.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "10:15:30+01:00[Europe/Paris]", "10:89:30+01:00", "10:89:30+40:00"])
    fun `Given a malformed time, when tested, it is not valid`(value: String) {
        TIME.canParse(value) shouldBe false
    }
}
