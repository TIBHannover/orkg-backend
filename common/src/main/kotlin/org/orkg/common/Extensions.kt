package org.orkg.common

import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
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

fun String.toIRIOrNull(): ParsedIRI? {
    try {
        return ParsedIRI(this)
    } catch (e: Exception) {
        return null
    }
}

fun String.isValidBoolean(): Boolean =
    when (this) {
        "true", "1", "false", "0" -> true
        else -> false
    }
