package org.orkg.testing.spring.restdocs

import java.time.OffsetDateTime
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath

fun createdResponseHeaders(): ResponseHeadersSnippet =
    responseHeaders(
        headerWithName("Location").description("Location to the created statement")
    )

fun pageableDetailedFieldParameters(): List<FieldDescriptor> = listOf(
    fieldWithPath("content[]").description("The content"),
    fieldWithPath("pageable").description("The attribute pageable"),
    fieldWithPath("pageable.sort").description("The attribute sort below pageable"),
    fieldWithPath("pageable.sort.sorted").description("The attribute sorted below sort"),
    fieldWithPath("pageable.sort.unsorted").description("The attribute unsorted below sort"),
    fieldWithPath("pageable.sort.empty").description("The attribute empty below sort"),
    fieldWithPath("pageable.offset").description("The offset of the results"),
    fieldWithPath("pageable.pageNumber").description("The page number of the results"),
    fieldWithPath("pageable.pageSize").description("The page size of the results"),
    fieldWithPath("pageable.paged").description("The paged attribute in pageable"),
    fieldWithPath("pageable.unpaged").description("The unpaged attribute in pageable"),
    fieldWithPath("totalPages").description("The total number of pages"),
    fieldWithPath("totalElements").description("The total number of elements"),
    fieldWithPath("last").description("The last attribute"),
    fieldWithPath("size").description("The size attribute"),
    fieldWithPath("number").description("The number attribute"),
    fieldWithPath("sort").description("The sort attribute"),
    fieldWithPath("sort.sorted").description("The sorted attribute inside sort"),
    fieldWithPath("sort.unsorted").description("The unsorted attribute inside sort"),
    fieldWithPath("sort.empty").description("The empty attribute inside sort"),
    fieldWithPath("numberOfElements").description("The number of elements"),
    fieldWithPath("first").description("The first attribute"),
    fieldWithPath("empty").description("The empty attribute")
)

fun ignorePageableFieldsExceptContent(): Array<FieldDescriptor> = arrayOf(
    subsectionWithPath("pageable").ignored(),
    *(listOf(
        "empty",
        "first",
        "last",
        "number",
        "numberOfElements",
        "size",
        "sort",
        "sort.empty",
        "sort.sorted",
        "sort.unsorted",
        "totalElements",
        "totalPages"
    ).map { fieldWithPath(it).ignored() }.toTypedArray())
)

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
