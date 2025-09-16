package org.orkg.testing.spring.restdocs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.snippet.AbstractDescriptor
import java.time.OffsetDateTime

fun pageableDetailedFieldParameters(): List<FieldDescriptor> = listOf(
    fieldWithPath("content[]").description("The result of the request as a (sorted) array."),
    subsectionWithPath("page").description("Paging information."),
    fieldWithPath("page.number").description("The number of the current page."),
    fieldWithPath("page.size").description("The size of the current page."),
    fieldWithPath("page.total_elements").description("The total amounts of elements."),
    fieldWithPath("page.total_pages").description("The total number of pages."),
)

fun pagedResponseFields(vararg fieldDescriptor: FieldDescriptor): ResponseFieldsSnippet =
    responseFields(pagedResponseFields(fieldDescriptor.asList()))

fun pagedResponseFields(fieldDescriptors: List<FieldDescriptor>, ignorePageFields: Boolean = true): List<FieldDescriptor> {
    val pageResponseFields = pageableDetailedFieldParameters()
    if (ignorePageFields) {
        pageResponseFields.forEach { it.ignored() }
    }
    return pageResponseFields + applyPathPrefix("content[].", fieldDescriptors)
}

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
    .type("String")

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(): T =
    description(listOfNotNull("*Deprecated*", description).joinToString(" "))

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(replaceWith: String): T =
    description(listOfNotNull("*Deprecated*. See `$replaceWith` for replacement.", description).joinToString(" "))

fun exceptionResponseFields(type: String) = listOf(
    fieldWithPath("type").description("A URI reference that identifies the problem type. Always `$type` for this error."),
    fieldWithPath("status").description("The HTTP status code of the error. This is equal to the status code of the request itself and MUST only be used for display purposes."),
    fieldWithPath("title").description("A short, human-readable summary of the problem type."),
    fieldWithPath("detail").description("A human-readable explanation specific to this occurrence of the problem."),
    fieldWithPath("instance").description("A URI reference that identifies the specific occurrence of the problem."),
    // legacy fields
    fieldWithPath("error").description("The human-readable error description of the status code, e.g. \"Bad Request\" for code 400.").deprecated("title"),
    fieldWithPath("message").description("A human-readable, and hopefully helpful message that explains the error.").deprecated("title"),
    fieldWithPath("path").description("The path to which the request was made that caused the error.").deprecated("instance"),
    fieldWithPath("timestamp").description("The <<timestamp-representation,timestamp>> of when the error happened.").deprecated(),
)

fun exceptionResponseFieldsWithoutDetail(type: String) =
    exceptionResponseFields(type).filter { it.path != "detail" && it.path != "message" }

fun validationExceptionResponseFields(type: String) = listOf(
    fieldWithPath("type").description("A URI reference that identifies the problem type. Always `$type` for this error."),
    fieldWithPath("status").description("The HTTP status code of the error. This is equal to the status code of the request itself and MUST only be used for display purposes."),
    fieldWithPath("title").description("A short, human-readable summary of the problem type."),
    fieldWithPath("instance").description("A URI reference that identifies the specific occurrence of the problem."),
    fieldWithPath("errors").description("An array that describes the details of each validation error."),
    fieldWithPath("errors[].detail").description("A description of the issue."),
    fieldWithPath("errors[].pointer").description("A JSON Pointer that describes the location of the problem within the request's content."),
    // legacy fields
    fieldWithPath("error").description("The human-readable error description of the status code, e.g. \"Bad Request\" for code 400.").deprecated("title"),
    fieldWithPath("path").description("The path to which the request was made that caused the error.").deprecated("instance"),
    fieldWithPath("timestamp").description("The <<timestamp-representation,timestamp>> of when the error happened.").deprecated(),
    fieldWithPath("errors[].message").description("A description of the issue.").deprecated("detail"),
    fieldWithPath("errors[].field").description("A JSON path that describes the location of the problem within the request's content.").deprecated("pointer"),
)
