package org.orkg.testing.spring.restdocs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import java.net.URI
import java.time.OffsetDateTime
import kotlin.reflect.KClass

fun pageableDetailedFieldParameters(
    schemaClass: KClass<*>? = null,
    additionalContentArrayAttributes: Map<String, Any?>? = null,
): List<FieldDescriptor> = listOf(
    if (schemaClass != null) {
        subsectionWithPath("content[]").references(schemaClass)
    } else {
        fieldWithPath("content[]")
    }.also {
        if (additionalContentArrayAttributes != null) {
            it.attributes += additionalContentArrayAttributes
        }
    }.description("The result of the request as a (sorted) array."),
    subsectionWithPath("page").description("Paging information."),
    fieldWithPath("page.number").description("The number of the current page.").format("int32").size(),
    fieldWithPath("page.size").description("The size of the current page.").format("int32").size(),
    fieldWithPath("page.total_elements").description("The total amounts of elements.").format("int64").size(),
    fieldWithPath("page.total_pages").description("The total number of pages.").format("int32").size(),
)

fun pagedResponseFields(vararg fieldDescriptor: FieldDescriptor): ResponseFieldsSnippet =
    responseFields(pagedResponseFields(fieldDescriptor.asList()))

fun pagedResponseFields(
    fieldDescriptors: List<FieldDescriptor>,
    schemaClass: KClass<*>? = null,
    ignorePageFields: Boolean = true,
): List<FieldDescriptor> {
    val pageResponseFields = pageableDetailedFieldParameters(
        schemaClass = schemaClass,
        additionalContentArrayAttributes = fieldDescriptors.singleOrNull { it.path == "content[]" }?.attributes,
    )
    if (ignorePageFields) {
        pageResponseFields.forEach { it.ignored() }
    }
    return pageResponseFields + applyPathPrefix("content[].", fieldDescriptors.filter { it.path != "content[]" })
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
    .type<OffsetDateTime>()

fun exceptionResponseFields(type: String) = listOf(
    fieldWithPath("type").description("A URI reference that identifies the problem type. Always `$type` for this error.").type<URI>(),
    fieldWithPath("status").description("The HTTP status code of the error. This is equal to the status code of the request itself and MUST only be used for display purposes.").type<Int>(),
    fieldWithPath("title").description("A short, human-readable summary of the problem type."),
    fieldWithPath("detail").description("A human-readable explanation specific to this occurrence of the problem."),
    fieldWithPath("instance").description("A URI reference that identifies the specific occurrence of the problem.").type<URI>(),
    // legacy fields
    fieldWithPath("error").description("The human-readable error description of the status code, e.g. \"Bad Request\" for code 400.").deprecated("title"),
    fieldWithPath("message").description("A human-readable, and hopefully helpful message that explains the error.").deprecated("title"),
    fieldWithPath("path").description("The path to which the request was made that caused the error.").type<URI>().deprecated("instance"),
    fieldWithPath("timestamp").description("The <<timestamp-representation,timestamp>> of when the error happened.").type<OffsetDateTime>().deprecated(),
)

fun exceptionResponseFieldsWithoutDetail(type: String) =
    exceptionResponseFields(type).filter { it.path != "detail" && it.path != "message" }

fun validationExceptionResponseFields(type: String) = listOf(
    fieldWithPath("type").description("A URI reference that identifies the problem type. Always `$type` for this error.").type<URI>(),
    fieldWithPath("status").description("The HTTP status code of the error. This is equal to the status code of the request itself and MUST only be used for display purposes.").type<Int>(),
    fieldWithPath("title").description("A short, human-readable summary of the problem type."),
    fieldWithPath("instance").description("A URI reference that identifies the specific occurrence of the problem.").type<URI>(),
    fieldWithPath("errors").description("An array that describes the details of each validation error."),
    fieldWithPath("errors[].detail").description("A description of the issue.").type("string").optional(),
    fieldWithPath("errors[].pointer").description("A JSON Pointer that describes the location of the problem within the request's content."),
    // legacy fields
    fieldWithPath("error").description("The human-readable error description of the status code, e.g. \"Bad Request\" for code 400.").deprecated("title"),
    fieldWithPath("path").description("The path to which the request was made that caused the error.").type<URI>().deprecated("instance"),
    fieldWithPath("timestamp").description("The <<timestamp-representation,timestamp>> of when the error happened.").type<OffsetDateTime>().deprecated(),
    fieldWithPath("errors[].message").description("A description of the issue.").type("string").optional().deprecated("detail"),
    fieldWithPath("errors[].field").description("A JSON path that describes the location of the problem within the request's content.").deprecated("pointer"),
)

fun polymorphicResponseFields(vararg responseFields: List<FieldDescriptor>): List<FieldDescriptor> =
    responseFields.flatMap { it }.groupBy { it.path }
        .map { (_, samePathDescriptors) ->
            if (samePathDescriptors.size == 1) {
                samePathDescriptors.single().optional()
            } else {
                samePathDescriptors.first()
            }
        }
