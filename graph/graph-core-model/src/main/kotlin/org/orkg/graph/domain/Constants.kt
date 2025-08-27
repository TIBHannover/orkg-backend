package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.common.isNormalized
import org.orkg.common.isValidAbsoluteIRI
import org.orkg.common.isValidBase64
import org.orkg.common.isValidBoolean
import org.orkg.common.isValidDate
import org.orkg.common.isValidDateTime
import org.orkg.common.isValidDateTimeStamp
import org.orkg.common.isValidDayTimeDuration
import org.orkg.common.isValidDecimal
import org.orkg.common.isValidDuration
import org.orkg.common.isValidGregorianDay
import org.orkg.common.isValidGregorianMonth
import org.orkg.common.isValidGregorianMonthDay
import org.orkg.common.isValidGregorianYear
import org.orkg.common.isValidGregorianYearMonth
import org.orkg.common.isValidHexBinary
import org.orkg.common.isValidIRI
import org.orkg.common.isValidInteger
import org.orkg.common.isValidLanguageTag
import org.orkg.common.isValidNegativeInteger
import org.orkg.common.isValidNonNegativeInteger
import org.orkg.common.isValidNonPositiveInteger
import org.orkg.common.isValidPositiveInteger
import org.orkg.common.isValidTime
import org.orkg.common.isValidToken
import org.orkg.common.isValidUnsignedByte
import org.orkg.common.isValidUnsignedInt
import org.orkg.common.isValidUnsignedLong
import org.orkg.common.isValidUnsignedShort
import org.orkg.common.isValidYearMonthDuration

val reservedClassIds = setOf(
    Classes.literal,
    Classes.`class`,
    Classes.predicate,
    Classes.resource,
    Classes.list,
    Classes.rosettaStoneStatement,
    Classes.thing
)

object Literals {
    enum class XSD(
        fragment: String,
        val `class`: ThingId,
        val isNumber: Boolean,
        private val predicate: (String) -> Boolean,
    ) {
        STRING("string", Classes.string, false, { true }),
        INT("integer", Classes.integer, true, { it.isValidInteger() }),
        DECIMAL("decimal", Classes.decimal, true, { it.isValidDecimal() }),
        DATE("date", Classes.date, false, { it.isValidDate() }),
        BOOLEAN("boolean", Classes.boolean, false, { it.isValidBoolean() }),
        FLOAT("float", Classes.float, true, { it.toFloatOrNull() != null }),
        DOUBLE("double", Classes.double, true, { it.toDoubleOrNull() != null }),
        URI("anyURI", Classes.uri, false, { it.isValidIRI() }),
        DURATION("duration", Classes.duration, false, { it.isValidDuration() }),
        DATE_TIME("dateTime", Classes.dateTime, false, { it.isValidDateTime() }),
        TIME("time", Classes.time, false, { it.isValidTime() }),
        GREGORIAN_YEAR_MONTH("gYearMonth", Classes.gregorianYearMonth, false, { it.isValidGregorianYearMonth() }),
        GREGORIAN_YEAR("gYear", Classes.gregorianYear, false, { it.isValidGregorianYear() }),
        GREGORIAN_MONTH_DAY("gMonthDay", Classes.gregorianMonthDay, false, { it.isValidGregorianMonthDay() }),
        GREGORIAN_DAY("gDay", Classes.gregorianDay, false, { it.isValidGregorianDay() }),
        GREGORIAN_MONTH("gMonth", Classes.gregorianMonth, false, { it.isValidGregorianMonth() }),
        HEX_BINARY("hexBinary", Classes.hexBinary, false, { it.isValidHexBinary() }),
        BASE_64_BINARY("base64Binary", Classes.base64Binary, false, { it.isValidBase64() }),
        NORMALIZED_STRING("normalizedString", Classes.normalizedString, false, { it.isNormalized() }),
        TOKEN("token", Classes.token, false, { it.isValidToken() }),
        LANGUAGE("language", Classes.language, false, { it.isValidLanguageTag() }),
        INT32("int", Classes.int32, true, { it.toIntOrNull() != null }),
        NON_POSITIVE_INTEGER("nonPositiveInteger", Classes.nonPositiveInteger, true, { it.isValidNonPositiveInteger() }),
        NEGATIVE_INTEGER("negativeInteger", Classes.negativeInteger, true, { it.isValidNegativeInteger() }),
        LONG("long", Classes.long, true, { it.toLongOrNull() != null }),
        SHORT("short", Classes.short, true, { it.toShortOrNull() != null }),
        BYTE("byte", Classes.byte, true, { it.toByteOrNull() != null }),
        NON_NEGATIVE_INTEGER("nonNegativeInteger", Classes.nonNegativeInteger, true, { it.isValidNonNegativeInteger() }),
        UNSIGNED_LONG("unsignedLong", Classes.unsignedLong, true, { it.isValidUnsignedLong() }),
        UNSIGNED_INT("unsignedInt", Classes.unsignedInt, true, { it.isValidUnsignedInt() }),
        UNSIGNED_SHORT("unsignedShort", Classes.unsignedShort, true, { it.isValidUnsignedShort() }),
        UNSIGNED_BYTE("unsignedByte", Classes.unsignedByte, true, { it.isValidUnsignedByte() }),
        POSITIVE_INTEGER("positiveInteger", Classes.positiveInteger, true, { it.isValidPositiveInteger() }),
        YEAR_MONTH_DURATION("yearMonthDuration", Classes.yearMonthDuration, false, { it.isValidYearMonthDuration() }),
        DAY_TIME_DURATION("dayTimeDuration", Classes.dayTimeDuration, false, { it.isValidDayTimeDuration() }),
        DATE_TIME_STAMP("dateTimeStamp", Classes.dateTimeStamp, false, { it.isValidDateTimeStamp() }),
        ;

        val prefixedUri: String = "xsd:$fragment"
        val uri: String = "http://www.w3.org/2001/XMLSchema#$fragment"

        fun canParse(value: String): Boolean = predicate(value)

        companion object {
            fun fromClass(`class`: ThingId): XSD? =
                XSD.entries.singleOrNull { it.`class` == `class` }

            fun fromString(string: String): XSD? =
                XSD.entries.singleOrNull { it.prefixedUri == string || it.uri == string }

            fun fromValue(value: String): XSD = when {
                value.isBlank() -> STRING
                INT.canParse(value) -> INT
                DECIMAL.canParse(value) -> DECIMAL
                BOOLEAN.canParse(value) -> BOOLEAN
                DATE.canParse(value) -> DATE
                DURATION.canParse(value) -> DURATION
                TIME.canParse(value) -> TIME
                DATE_TIME.canParse(value) -> DATE_TIME
                value.isValidAbsoluteIRI() -> URI
                else -> STRING
            }
        }
    }
}

object Resources {
    val sustainableDevelopmentGoals = (1..17).map { ThingId("SDG_$it") }
}
