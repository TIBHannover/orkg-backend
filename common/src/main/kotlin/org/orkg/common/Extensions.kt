package org.orkg.common

import java.net.URI
import java.time.Duration
import java.time.format.DateTimeFormatter
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
