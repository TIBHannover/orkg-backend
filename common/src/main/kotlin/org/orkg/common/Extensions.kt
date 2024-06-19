package org.orkg.common

import java.net.URI
import java.time.Duration
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.util.regex.Pattern
import org.springframework.http.CacheControl
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

fun String.isValidURI(): Boolean {
    try {
        URI(this)
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

fun String.isValidDuration(): Boolean {
    try {
        Duration.parse(this)
    } catch (e: Exception) {
        return false
    }
    return true
}

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
