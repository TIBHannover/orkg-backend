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
    fieldWithPath("content").description("The result of the request as a (sorted) array."),
    subsectionWithPath("page").description("Paging information."),
    fieldWithPath("page.number").description("The number of the current page."),
    fieldWithPath("page.size").description("The size of the current page."),
    fieldWithPath("page.total_elements").description("The total amounts of elements."),
    fieldWithPath("page.total_pages").description("The total number of pages."),
)

fun pagedResponseFields(vararg fieldDescriptor: FieldDescriptor): ResponseFieldsSnippet {
    val responseFields = applyPathPrefix("content[].", fieldDescriptor.asList())
    responseFields += listOf(
        "page",
        "page.number",
        "page.size",
        "page.total_elements",
        "page.total_pages",
    ).map { fieldWithPath(it).ignored() }
    return responseFields(responseFields)
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
    .type(OffsetDateTime::class.simpleName)

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(): T =
    description(listOfNotNull("*Deprecated*", description).joinToString(" "))

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(replaceWith: String): T =
    description(listOfNotNull("*Deprecated*. See `$replaceWith` for replacement.", description).joinToString(" "))
