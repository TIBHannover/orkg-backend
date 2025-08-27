package org.orkg.common

import org.eclipse.rdf4j.common.net.ParsedIRI
import java.time.MonthDay
import java.time.YearMonth
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale
import java.util.regex.Pattern

/**
 * Checks whether the given string is a [RFC 3987](https://www.ietf.org/rfc/rfc3987.txt) compliant IRI.
 */
fun String.isValidIRI(): Boolean {
    try {
        ParsedIRI.create(this)
    } catch (_: Exception) {
        return false
    }
    return true
}

fun String.isValidAbsoluteIRI(): Boolean {
    try {
        return ParsedIRI(this).isAbsolute
    } catch (_: Exception) {
        return false
    }
}

fun String.isValidDate(): Boolean {
    try {
        DateTimeFormatter.ISO_DATE.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

private val DECIMAL_MATCHER = Pattern.compile("""([+-])?([0-9]+(\.[0-9]*)?|\.[0-9]+)""").asMatchPredicate()

fun String.isValidDecimal(): Boolean = DECIMAL_MATCHER.test(this)

private val INTEGER_MATCHER = Pattern.compile("""[+-]?[0-9]+""").asMatchPredicate()

fun String.isValidInteger(): Boolean = INTEGER_MATCHER.test(this)

private val DURATION_MATCHER =
    Pattern.compile("""^-?P(((\d+Y(\d+M)?(\d+D)?|(\d+M)(\d+D)?|(\d+D))(T((\d+H)(\d+M)?(\d+(\.\d+)?S)?|(\d+M)(\d+(\.\d+)?S)?|(\d+(\.\d+)?S)))?)|(T((\d+H)(\d+M)?(\d+(\.\d+)?S)?|(\d+M)(\d+(\.\d+)?S)?|(\d+(\.\d+)?S))))$""")
        .asMatchPredicate()

fun String.isValidDuration(): Boolean = DURATION_MATCHER.test(this)

private val STRICT_ISO_8601_DATE_TIME = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidDateTime(): Boolean {
    try {
        STRICT_ISO_8601_DATE_TIME.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

fun String.isValidTime(): Boolean {
    try {
        DateTimeFormatter.ISO_TIME.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

private val GREGORIAN_YEAR_MONTH = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidGregorianYearMonth(): Boolean {
    try {
        YearMonth.parse(this, GREGORIAN_YEAR_MONTH)
    } catch (e: Exception) {
        return false
    }
    return true
}

private val GREGORIAN_YEAR = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidGregorianYear(): Boolean {
    try {
        GREGORIAN_YEAR.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

private val GREGORIAN_MONTH_DAY = DateTimeFormatterBuilder()
    .appendLiteral("--")
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidGregorianMonthDay(): Boolean {
    try {
        MonthDay.parse(this, GREGORIAN_MONTH_DAY)
    } catch (e: Exception) {
        return false
    }
    return true
}

private val GREGORIAN_DAY = DateTimeFormatterBuilder()
    .appendLiteral("---")
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidGregorianDay(): Boolean {
    try {
        val temporalAccessor = GREGORIAN_DAY.parse(this)
        val dayOfMonth = temporalAccessor.get(ChronoField.DAY_OF_MONTH)
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth.toLong())
    } catch (e: Exception) {
        return false
    }
    return true
}

private val GREGORIAN_MONTH = DateTimeFormatterBuilder()
    .appendLiteral("--")
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidGregorianMonth(): Boolean {
    try {
        val temporalAccessor = GREGORIAN_MONTH.parse(this)
        val monthOfYear = temporalAccessor.get(ChronoField.MONTH_OF_YEAR)
        ChronoField.MONTH_OF_YEAR.checkValidValue(monthOfYear.toLong())
    } catch (e: Exception) {
        return false
    }
    return true
}

private val HEX_BINARY_MATCHER = Pattern.compile("""^([0-9a-fA-F]{2})*$""").asMatchPredicate()

fun String.isValidHexBinary(): Boolean = HEX_BINARY_MATCHER.test(this)

private val BASE_64_BINARY_MATCHER =
    Pattern.compile("""^((([A-Za-z0-9+/] ?){4})*(([A-Za-z0-9+/] ?){3}[A-Za-z0-9+/]|([A-Za-z0-9+/] ?){2}[AEIMQUYcgkosw048] ?=|[A-Za-z0-9+/] ?[AQgw] ?= ?=))?$""")
        .asMatchPredicate()

fun String.isValidBase64(): Boolean = BASE_64_BINARY_MATCHER.test(this)

fun String.isNormalized(): Boolean {
    for (c in this) {
        if (c == '\n' || c == '\r' || c == '\t') {
            return false
        }
    }
    return true
}

fun String.isValidToken(): Boolean =
    isNormalized() && isNotBlank() && trim().replace(Regex(" +"), " ") == this

fun String.isValidLanguageTag(): Boolean =
    Locale.forLanguageTag(this).let { it != null && it.toLanguageTag() != "und" }

private val POSITIVE_INTEGER_MATCHER = Pattern.compile("""^\+?\d*[1-9]\d*$""").asMatchPredicate()

fun String.isValidPositiveInteger(): Boolean = POSITIVE_INTEGER_MATCHER.test(this)

private val NON_POSITIVE_INTEGER_MATCHER = Pattern.compile("""^-\d+|[+-]?0+$""").asMatchPredicate()

fun String.isValidNonPositiveInteger(): Boolean = NON_POSITIVE_INTEGER_MATCHER.test(this)

private val NEGATIVE_INTEGER_MATCHER = Pattern.compile("""^-\d*[1-9]\d*$""").asMatchPredicate()

fun String.isValidNegativeInteger(): Boolean = NEGATIVE_INTEGER_MATCHER.test(this)

private val NON_NEGATIVE_INTEGER_MATCHER = Pattern.compile("""^\+?\d+|[+-]?0+$""").asMatchPredicate()

fun String.isValidNonNegativeInteger(): Boolean = NON_NEGATIVE_INTEGER_MATCHER.test(this)

private val MINUS_ZERO_REGEX = Regex("^-0+$")

fun String.isValidUnsignedLong(): Boolean =
    // Kotlin does not accept '-' signs for zero values
    replaceFirst(MINUS_ZERO_REGEX, "0").toULongOrNull() != null

fun String.isValidUnsignedInt(): Boolean =
    // Kotlin does not accept '-' signs for zero values
    replaceFirst(MINUS_ZERO_REGEX, "0").toUIntOrNull() != null

fun String.isValidUnsignedShort(): Boolean =
    // Kotlin does not accept '-' signs for zero values
    replaceFirst(MINUS_ZERO_REGEX, "0").toUShortOrNull() != null

fun String.isValidUnsignedByte(): Boolean =
    // Kotlin does not accept '-' signs for zero values
    replaceFirst(MINUS_ZERO_REGEX, "0").toUByteOrNull() != null

private val YEAR_MONTH_DURATION_MATCHER =
    Pattern.compile("""^-?P(((\d+Y)(\d+M)?)|(\d+M))$""").asMatchPredicate()

fun String.isValidYearMonthDuration(): Boolean = YEAR_MONTH_DURATION_MATCHER.test(this)

private const val DU_TIME_FRAG = """(T((\d+H)(\d+M)?(\d+(\.\d+)?S)?|(\d+M)(\d+(\.\d+)?S)?|(\d+(\.\d+)?S)))"""
private val DAY_TIME_DURATION_MATCHER =
    Pattern.compile("""^-?P((\d+D)$DU_TIME_FRAG?|$DU_TIME_FRAG$)""").asMatchPredicate()

fun String.isValidDayTimeDuration(): Boolean = DAY_TIME_DURATION_MATCHER.test(this)

private val DATE_TIME_STAMP = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOffsetId()
    .toFormatter()
    .withChronology(IsoChronology.INSTANCE)
    .withResolverStyle(ResolverStyle.STRICT)

fun String.isValidDateTimeStamp(): Boolean {
    try {
        DATE_TIME_STAMP.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

fun <T : Any> mutableSetOfNotNull(vararg elements: T?): MutableSet<T> {
    val result = mutableSetOf<T>()
    elements.forEach {
        if (it != null) {
            result.add(it)
        }
    }
    return result
}

fun String.toIRIOrNull(): ParsedIRI? =
    try {
        ParsedIRI.create(this)
    } catch (_: Exception) {
        null
    }

fun String.isValidBoolean(): Boolean =
    when (this) {
        "true", "1", "false", "0" -> true
        else -> false
    }
