package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.graph.domain.Literals.XSD.BASE_64_BINARY
import org.orkg.graph.domain.Literals.XSD.BOOLEAN
import org.orkg.graph.domain.Literals.XSD.BYTE
import org.orkg.graph.domain.Literals.XSD.DATE
import org.orkg.graph.domain.Literals.XSD.DATE_TIME
import org.orkg.graph.domain.Literals.XSD.DATE_TIME_STAMP
import org.orkg.graph.domain.Literals.XSD.DAY_TIME_DURATION
import org.orkg.graph.domain.Literals.XSD.DECIMAL
import org.orkg.graph.domain.Literals.XSD.DOUBLE
import org.orkg.graph.domain.Literals.XSD.DURATION
import org.orkg.graph.domain.Literals.XSD.FLOAT
import org.orkg.graph.domain.Literals.XSD.GREGORIAN_DAY
import org.orkg.graph.domain.Literals.XSD.GREGORIAN_MONTH
import org.orkg.graph.domain.Literals.XSD.GREGORIAN_MONTH_DAY
import org.orkg.graph.domain.Literals.XSD.GREGORIAN_YEAR
import org.orkg.graph.domain.Literals.XSD.GREGORIAN_YEAR_MONTH
import org.orkg.graph.domain.Literals.XSD.HEX_BINARY
import org.orkg.graph.domain.Literals.XSD.INT
import org.orkg.graph.domain.Literals.XSD.INT32
import org.orkg.graph.domain.Literals.XSD.LANGUAGE
import org.orkg.graph.domain.Literals.XSD.LONG
import org.orkg.graph.domain.Literals.XSD.NEGATIVE_INTEGER
import org.orkg.graph.domain.Literals.XSD.NON_NEGATIVE_INTEGER
import org.orkg.graph.domain.Literals.XSD.NON_POSITIVE_INTEGER
import org.orkg.graph.domain.Literals.XSD.NORMALIZED_STRING
import org.orkg.graph.domain.Literals.XSD.POSITIVE_INTEGER
import org.orkg.graph.domain.Literals.XSD.SHORT
import org.orkg.graph.domain.Literals.XSD.STRING
import org.orkg.graph.domain.Literals.XSD.TIME
import org.orkg.graph.domain.Literals.XSD.TOKEN
import org.orkg.graph.domain.Literals.XSD.UNSIGNED_BYTE
import org.orkg.graph.domain.Literals.XSD.UNSIGNED_INT
import org.orkg.graph.domain.Literals.XSD.UNSIGNED_LONG
import org.orkg.graph.domain.Literals.XSD.UNSIGNED_SHORT
import org.orkg.graph.domain.Literals.XSD.URI
import org.orkg.graph.domain.Literals.XSD.YEAR_MONTH_DURATION

@Suppress("HttpUrlsUsage")
internal class XSDValidationTest {
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

    @Test
    fun `Given a duration, when tested, it is valid`() {
        validDurations().forEach { value ->
            DURATION.canParse(value) shouldBe true
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "", "   ", "abc", "+-5", "\n", "PT15Z", "AT10H", "+-PT-6H+3M", "--P10M", "p10M",
            "P10m", "P10h", "P10s", "P.5S", "P1.S", "P-10H", "P-10M", "P-10S", "P", "PHMS",
            "PH", "PM", "PS", "P5H", "P5S", "P.5S", "P0.5S", "PT"
        ]
    )
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

    @ParameterizedTest
    @ValueSource(strings = ["2011-12Z", "2011-12+01:00", "2011-12", "2011-12+14:00"])
    fun `Given a gregorian year month, when tested, it is valid`(value: String) {
        GREGORIAN_YEAR_MONTH.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "10:15:30+01:00", "2011-12-03T10:15:30+01:00", "2011 12", "2011-13"])
    fun `Given a malformed gregorian year month, when tested, it is not valid`(value: String) {
        GREGORIAN_YEAR_MONTH.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["2011Z", "2011+01:00", "2011", "2011+14:00"])
    fun `Given a gregorian year, when tested, it is valid`(value: String) {
        GREGORIAN_YEAR.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "10:15:30+01:00", "2011-12-03T10:15:30+01:00", "2011 12", "2014A"])
    fun `Given a malformed gregorian year, when tested, it is not valid`(value: String) {
        GREGORIAN_YEAR.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["--07-15Z", "--07-15+01:00", "--07-15", "--07-15+14:00"])
    fun `Given a gregorian month day, when tested, it is valid`(value: String) {
        GREGORIAN_MONTH_DAY.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "- 07 15+01:00", "--13-01", "--01-35"])
    fun `Given a malformed gregorian month day, when tested, it is not valid`(value: String) {
        GREGORIAN_MONTH_DAY.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["---15Z", "---15+01:00", "---15", "---15+14:00"])
    fun `Given a gregorian day, when tested, it is valid`(value: String) {
        GREGORIAN_DAY.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "- - 15+01:00", "---35", "31", "0", "---0", "---15+30:00"])
    fun `Given a malformed gregorian day, when tested, it is not valid`(value: String) {
        GREGORIAN_DAY.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["--07", "--07+01:00", "--07", "--07+14:00", "--12"])
    fun `Given a gregorian month, when tested, it is valid`(value: String) {
        GREGORIAN_MONTH.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "-- 07+01:00", "--13", "--00", "--1"])
    fun `Given a malformed gregorian month, when tested, it is not valid`(value: String) {
        GREGORIAN_MONTH.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "a71b", "a6b0d60c", "A71B", "A6B0D60C", "a71B", "a6B0A60c", "aa", "00"])
    fun `Given a hex binary string, when tested, it is valid`(value: String) {
        HEX_BINARY.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["   ", "g0", "abc", "\n", "a7 1b"])
    fun `Given a malformed hex binary string, when tested, it is not valid`(value: String) {
        HEX_BINARY.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["MQ==", "MTI=", "MTIz", "b3JrZzE=", "b3JrZzEy"])
    fun `Given a base 64 binary string, when tested, it is valid`(value: String) {
        BASE_64_BINARY.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["A   ", "A\n", "ABC~", "A", "A1Z2A="])
    fun `Given a malformed base 64 binary string, when tested, it is not valid`(value: String) {
        BASE_64_BINARY.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["ABC", "abc", "abc def", "  ", "", "+-5"])
    fun `Given a normalized string, when tested, it is valid`(value: String) {
        NORMALIZED_STRING.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["\n", "\t", "\r", "a\n", "abc   \n", "\n\r\t"])
    fun `Given a malformed normalized string, when tested, it is not valid`(value: String) {
        NORMALIZED_STRING.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["ABC", "abc", "abc def", "+-5"])
    fun `Given a token, when tested, it is valid`(value: String) {
        TOKEN.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["\n", "\t", "\r", "a\n", "abc   \n", "\n\r\t", "abc  def", " ", "  ", "", " leading whitespace", "trailing whitespace "])
    fun `Given a malformed token, when tested, it is not valid`(value: String) {
        TOKEN.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = [
            "de",
            "de-CH",
            "de-DE-1901",
            "es-419",
            "sl-IT-nedis",
            "en-US-boont",
            "mn-Cyrl-MN",
            "x-fr-CH",
            "en-GB-boont-r-extended-sequence-x-private",
            "sr-Cyrl",
            "sr-Latn",
            "hy-Latn-IT-arevela",
            "zh-TW"
        ]
    )
    fun `Given a language tag, when tested, it is valid`(value: String) {
        LANGUAGE.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "563", "*/--5678", "\n"])
    fun `Given a malformed language tag, when tested, it is not valid`(value: String) {
        LANGUAGE.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "451", "-0", "-5", "2147483647", "-2147483648", "00", "-00", "+00", "05", "+05", "-05"])
    fun `Given an int, when tested, it is valid`(value: String) {
        INT32.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "2147483648", "-2147483649"])
    fun `Given a malformed int, when tested, it is not valid`(value: String) {
        INT32.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "-451", "-0", "-5", "+0", "-4294967297", "00", "-00", "+00", "-05"])
    fun `Given a non positive integer, when tested, it is valid`(value: String) {
        NON_POSITIVE_INTEGER.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "1"])
    fun `Given a malformed non positive integer, when tested, it is not valid`(value: String) {
        NON_POSITIVE_INTEGER.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-451", "-5", "-4294967297", "-05"])
    fun `Given a negative integer, when tested, it is valid`(value: String) {
        NEGATIVE_INTEGER.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "1", "0", "+0", "-0"])
    fun `Given a malformed negative integer, when tested, it is not valid`(value: String) {
        NEGATIVE_INTEGER.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "0", "+0", "-0", "-451", "-5", "9223372036854775807", "-9223372036854775808", "00", "-00", "+00", "05", "+05", "-05"])
    fun `Given a long, when tested, it is valid`(value: String) {
        LONG.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "9223372036854775808", "-9223372036854775809"])
    fun `Given a malformed long, when tested, it is not valid`(value: String) {
        LONG.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "0", "+0", "-0", "-451", "-5", "32767", "-32768", "00", "-00", "+00", "05", "+05", "-05"])
    fun `Given a short, when tested, it is valid`(value: String) {
        SHORT.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "327678", "-32769"])
    fun `Given a malformed short, when tested, it is not valid`(value: String) {
        SHORT.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "0", "+0", "-0", "-5", "127", "-128", "00", "-00", "+00", "05", "+05", "-05"])
    fun `Given a byte, when tested, it is valid`(value: String) {
        BYTE.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "128", "-129"])
    fun `Given a malformed byte, when tested, it is not valid`(value: String) {
        BYTE.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "451", "+5", "4294967297", "00", "-00", "+00", "05", "+05"])
    fun `Given a non negative integer, when tested, it is valid`(value: String) {
        NON_NEGATIVE_INTEGER.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1"])
    fun `Given a malformed non negative integer, when tested, it is not valid`(value: String) {
        NON_NEGATIVE_INTEGER.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "451", "+5", "18446744073709551615", "00", "-00", "+00", "05", "+05"])
    fun `Given an unsigned long, when tested, it is valid`(value: String) {
        UNSIGNED_LONG.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1", "18446744073709551616", "-02"])
    fun `Given a malformed unsigned long, when tested, it is not valid`(value: String) {
        UNSIGNED_LONG.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "451", "+5", "4294967295", "00", "-00", "+00", "05", "+05"])
    fun `Given an unsigned int, when tested, it is valid`(value: String) {
        UNSIGNED_INT.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1", "4294967296", "-02"])
    fun `Given a malformed unsigned int, when tested, it is not valid`(value: String) {
        UNSIGNED_INT.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "451", "+5", "65535", "00", "-00", "+00", "05", "+05"])
    fun `Given an unsigned short, when tested, it is valid`(value: String) {
        UNSIGNED_SHORT.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1", "65536", "-02"])
    fun `Given a malformed unsigned short, when tested, it is not valid`(value: String) {
        UNSIGNED_SHORT.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "64", "+5", "255", "00", "-00", "+00", "05", "+05"])
    fun `Given an unsigned byte, when tested, it is valid`(value: String) {
        UNSIGNED_BYTE.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1", "256", "-02"])
    fun `Given a malformed unsigned byte, when tested, it is not valid`(value: String) {
        UNSIGNED_BYTE.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["451", "5", "4294967297", "05", "+05"])
    fun `Given a positive integer, when tested, it is valid`(value: String) {
        POSITIVE_INTEGER.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["-0", "0", "+0", "00", "-00", "+00", "", "   ", "+-5", "\n", "-1", "-05"])
    fun `Given a malformed positive integer, when tested, it is not valid`(value: String) {
        POSITIVE_INTEGER.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "P1Y1M", "-P1Y1M", "P0Y1M", "-P0Y1M",
            "P1Y0M", "-P1Y0M", "P0Y0M", "-P0Y0M",
            "P1Y", "-P1Y", "P0Y", "-P0Y",
            "P1M", "-P1M", "P0M", "-P0M",
            "P12Y34M", "-P12Y34M", "P0Y12M", "-P0Y12M",
            "P12Y0M", "-P12Y0M",
            "P12Y", "-P12Y",
            "P17M", "-P17M"
        ]
    )
    fun `Given a year month duration, when tested, it is valid`(value: String) {
        YEAR_MONTH_DURATION.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "+-5", "\n", "-1", "-5", "P1y", "p1Y", "P1Y5m", "P1YT5M"])
    fun `Given a malformed year month duration, when tested, it is not valid`(value: String) {
        YEAR_MONTH_DURATION.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @MethodSource("validDayTimeDuration")
    fun `Given a day time duration, when tested, it is valid`(value: String) {
        DAY_TIME_DURATION.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\n", "--P10M", "p10M", "P10m", "P10h", "P10s", "P.5S", "P1.S", "P-10H", "P-10M", "P-10S", "P", "PHMS", "PH", "PM", "PS"])
    fun `Given a malformed day time duration, when tested, it is not valid`(value: String) {
        DAY_TIME_DURATION.canParse(value) shouldBe false
    }

    @ParameterizedTest
    @ValueSource(strings = ["2002-10-10T12:00:00-05:00", "2002-10-10T17:00:00Z", "2011-12-03T10:15:30+01:00"])
    fun `Given a date time stamp, when tested, it is valid`(value: String) {
        DATE_TIME_STAMP.canParse(value) shouldBe true
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "abc", "+-5", "\n", "2011-12-03T10:15:30+01:00[Europe/Paris]", "2002-25-10T12:00:00-05:00", "2002-10-10T12:00:00-25:00", "2002-10-10T12:00:00-05:70", "2002-10-10T28:00:00-05:00", "2011-12-03T10:15:30"])
    fun `Given a malformed date time stamp, when tested, it is not valid`(value: String) {
        DATE_TIME_STAMP.canParse(value) shouldBe false
    }

    companion object {
        @JvmStatic
        fun validDurations(): Iterator<String> = iterator {
            val signs = listOf("", "-")
            val ys = listOf(null, "0", "00", "2", "17", "25")
            val ws = listOf(null, "0", "00", "2", "41", "65")
            val ds = listOf(null, "0", "00", "2", "41", "65")
            val hs = listOf(null, "0", "00", "2", "17", "25")
            val ms = listOf(null, "0", "00", "2", "41", "65")
            val ss = listOf(null, "0", "00", "2", "41", "65")
            val fs = listOf(null, "0", "00", "2", "54", "78956")

            for (sign in signs) {
                for (y in ys) {
                    for (w in ws) {
                        for (d in ds) {
                            for (h in hs) {
                                for (m in ms) {
                                    for (s in ss) {
                                        for (f in fs) {
                                            val duration = buildString {
                                                append(sign)
                                                append("P")

                                                if (y != null) {
                                                    append(y)
                                                    append("Y")
                                                }
                                                if (w != null) {
                                                    append(w)
                                                    append("M")
                                                }
                                                if (d != null) {
                                                    append(d)
                                                    append("D")
                                                }

                                                if (h != null || m != null || s != null) {
                                                    append("T")
                                                }

                                                if (h != null) {
                                                    append(h)
                                                    append("H")
                                                }
                                                if (m != null) {
                                                    append(m)
                                                    append("M")
                                                }
                                                if (s != null) {
                                                    append(s)
                                                    if (f != null) {
                                                        append(".")
                                                        append(f)
                                                    }
                                                    append("S")
                                                }
                                            }
                                            if (!duration.endsWith("P")) {
                                                yield(duration)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun validDayTimeDuration(): Iterator<String> = iterator {
            val signs = listOf("", "-")
            val ds = listOf(null, "0", "00", "2", "17", "25")
            val hs = listOf(null, "0", "00", "2", "17", "25")
            val ms = listOf(null, "0", "00", "2", "41", "65")
            val ss = listOf(null, "0", "00", "2", "41", "65")
            val fs = listOf(null, "0", "00", "2", "54", "78956")

            for (sign in signs) {
                for (d in ds) {
                    for (h in hs) {
                        for (m in ms) {
                            for (s in ss) {
                                for (f in fs) {
                                    val duration = buildString {
                                        append(sign)
                                        append("P")
                                        if (d != null) {
                                            append(d)
                                            append("D")
                                        }
                                        append("T")
                                        if (h != null) {
                                            append(h)
                                            append("H")
                                        }
                                        if (m != null) {
                                            append(m)
                                            append("M")
                                        }
                                        if (s != null) {
                                            append(s)
                                            if (f != null) {
                                                append(".")
                                                append(f)
                                            }
                                            append("S")
                                        }
                                    }
                                    if (!duration.endsWith("PT") && !duration.endsWith("DT")) {
                                        yield(duration)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
