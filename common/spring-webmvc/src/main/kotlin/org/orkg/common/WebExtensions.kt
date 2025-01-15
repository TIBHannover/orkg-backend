package org.orkg.common

import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.orkg.common.exceptions.ServiceUnavailable
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> T.withCacheControl(duration: Duration): ResponseEntity<T> =
    ResponseEntity.ok().cacheControl(CacheControl.maxAge(duration)).body(this)

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

internal fun Pageable.toRepresentation(): PageableRepresentation =
    if (isPaged) PagedPageableRepresentation(pageNumber, pageSize, sort.toRepresentation(), isPaged, isUnpaged, offset)
    else UnpagedPageableRepresentation()

internal fun Sort.toRepresentation(): SortRepresentation =
    SortRepresentation(isEmpty, isSorted, isUnsorted)
