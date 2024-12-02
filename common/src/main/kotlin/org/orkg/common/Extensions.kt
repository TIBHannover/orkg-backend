package org.orkg.common

import java.io.IOException
import java.math.BigInteger
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Duration
import java.time.MonthDay
import java.time.YearMonth
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.*
import java.util.regex.Pattern
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.exceptions.ServiceUnavailable
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun String.toSnakeCase(): String =
    if (this.isEmpty()) this else StringBuilder().also {
        this.forEach { c ->
            when (c) {
                in 'A'..'Z' -> {
                    it.append("_")
                    it.append(c.lowercase())
                }

                else -> {
                    it.append(c)
                }
            }
        }
    }.toString()

fun <T> T.withCacheControl(duration: Duration): ResponseEntity<T> =
    ResponseEntity.ok().cacheControl(CacheControl.maxAge(duration)).body(this)

/**
 * Checks whether the given string is a [RFC 3987](https://www.ietf.org/rfc/rfc3987.txt) compliant IRI.
 */
fun String.isValidIRI(): Boolean {
    try {
        ParsedIRI(this)
    } catch (e: Exception) {
        return false
    }
    return true
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

private val DAY_TIME_DURATION_MATCHER =
    Pattern.compile("""^-?P((\d+H)(\d+M)?(\d+(\.\d+)?S)?|(\d+M)(\d+(\.\d+)?S)?|(\d+(\.\d+)?S))$""").asMatchPredicate()

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

fun <T> HttpClient.send(httpRequest: HttpRequest, serviceName: String, successCallback: (String) -> T): T? {
    try {
        val response = send(httpRequest, HttpResponse.BodyHandlers.ofString())
        return when (response.statusCode()) {
            HttpStatus.OK.value() -> successCallback(response.body())
            HttpStatus.NOT_FOUND.value() -> null
            else -> throw ServiceUnavailable.create(serviceName, response.statusCode(), response.body())
        }
    } catch (e: IOException) {
        throw ServiceUnavailable.create(serviceName, e)
    }
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
        ParsedIRI(this)
    } catch (e: Exception) {
        null
    }

fun String.isValidBoolean(): Boolean =
    when (this) {
        "true", "1", "false", "0" -> true
        else -> false
    }

/**
 * Calculate the MD5 of a string.
 *
 * @return The MD5 in hexadecimal, zero-prefixed to 32 characters.
 */
val String.md5: String
    get() = BigInteger(1, MessageDigest.getInstance("MD5").digest(this.toByteArray()))
        .toString(16)
        .padStart(32, '0')
