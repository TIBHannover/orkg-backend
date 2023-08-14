package eu.tib.orkg.prototype.testing.spring.restdocs

import java.time.OffsetDateTime
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

/**
 * Template field descriptor for timestamps.
 *
 * Using this template will link to the documentation of the timestamp representation.
 * It will also add a type description to [OffsetDateTime].
 *
 * @param path the path of the field
 * @param suffix a suffix for description, finishing the string "The timestamp when …"
 */
fun timestampFieldWithPath(path: String, suffix: String): FieldDescriptor = fieldWithPath(path)
    .description(
        "The <<timestamp-representation,timestamp>> when $suffix. " +
        "(Also see https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/OffsetDateTime.html[JavaDoc])."
    )
    .type(OffsetDateTime::class.simpleName)

fun documentedGetRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
    RestDocumentationRequestBuilders.get(urlTemplate, *uriValues)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding("utf-8")

fun documentedDeleteRequestTo(
    urlTemplate: String,
    vararg uriValues: Any,
    accept: String = MediaType.APPLICATION_JSON_VALUE,
    contentType: String = MediaType.APPLICATION_JSON_VALUE
): MockHttpServletRequestBuilder =
    RestDocumentationRequestBuilders.delete(urlTemplate, *uriValues)
        .accept(accept)
        .contentType(contentType)
        .characterEncoding("utf-8")

fun documentedPutRequestTo(
    urlTemplate: String,
    vararg uriValues: Any,
    accept: String = MediaType.APPLICATION_JSON_VALUE,
    contentType: String = MediaType.APPLICATION_JSON_VALUE
): MockHttpServletRequestBuilder =
    RestDocumentationRequestBuilders.put(urlTemplate, *uriValues)
        .accept(accept)
        .contentType(contentType)
        .characterEncoding("utf-8")
