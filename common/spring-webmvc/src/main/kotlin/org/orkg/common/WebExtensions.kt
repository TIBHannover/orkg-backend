package org.orkg.common

import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.Unauthorized
import org.orkg.common.exceptions.UnknownSortingProperty
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

fun <T : Any> T.withCacheControl(duration: Duration): ResponseEntity<T> =
    ResponseEntity.ok().cacheControl(CacheControl.maxAge(duration)).body(this)

fun Pageable.withSort(sort: Sort): Pageable =
    PageRequest.of(pageNumber, pageSize, sort)

fun Pageable.remapSort(mapping: Map<String, String>) =
    if (sort.isUnsorted) this else withSort(sort.remap(mapping))

fun Sort.remap(mapping: Map<String, String>): Sort =
    Sort.by(map { order -> Sort.Order(order.direction, mapping[order.property] ?: throw UnknownSortingProperty(order.property)) }.toList())

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

fun Authentication?.contributorId(): ContributorId =
    this?.name?.let(::ContributorId) ?: throw Unauthorized()
