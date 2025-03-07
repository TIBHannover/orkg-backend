package org.orkg.testing.spring.restdocs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.snippet.AbstractDescriptor
import java.time.OffsetDateTime

fun pageableDetailedFieldParameters(): List<FieldDescriptor> = listOf(
    fieldWithPath("content").description("The result of the request as a (sorted) array."),
    subsectionWithPath("page").description("Paging information."),
    fieldWithPath("page.number").description("The number of the current page."),
    fieldWithPath("page.size").description("The size of the current page."),
    fieldWithPath("page.total_elements").description("The total amounts of elements."),
    fieldWithPath("page.total_pages").description("The total number of pages."),
    // Deprecated fields
    subsectionWithPath("pageable").deprecated("page"),
    fieldWithPath("pageable.pageNumber").description("The number of the current page.").deprecated("page.number"),
    fieldWithPath("pageable.pageSize").description("The size of the current page.").deprecated("page.size"),
    fieldWithPath("pageable.sort").description("The sorting parameters for this page.").deprecated(),
    fieldWithPath("pageable.sort.empty").description("Determines if the sort object is empty.").deprecated(),
    fieldWithPath("pageable.sort.sorted").description("Determines if the page is sorted. Inverse of `unsorted`.").deprecated(),
    fieldWithPath("pageable.sort.unsorted").description("Determines if the page is unsorted. Inverse of `sorted`.").deprecated(),
    fieldWithPath("pageable.offset").description("The offset of the first element of the page. Equal to `pageNumber * pageSize`.").deprecated(),
    fieldWithPath("pageable.paged").description("Determines if the request is paged.").deprecated(),
    fieldWithPath("pageable.unpaged").description("Determines if the request is unpaged. Inverse of `paged`.").deprecated(),
    fieldWithPath("empty").description("Determines if the current page is empty.").deprecated(),
    fieldWithPath("first").description("Determines if the current page is the first one.").deprecated(),
    fieldWithPath("last").description("Determines if the current page is the last one.").deprecated(),
    fieldWithPath("number").description("The number of the current page.").deprecated("page.number"),
    fieldWithPath("numberOfElements").description("The number of elements currently on this page.").deprecated(),
    fieldWithPath("size").description("The size of the current page.").deprecated("page.size"),
    fieldWithPath("sort").description("The sorting parameters for this page.").deprecated(),
    fieldWithPath("sort.empty").description("Determines if the sort object is empty.").deprecated(),
    fieldWithPath("sort.sorted").description("Determines if the page is sorted. Inverse of `unsorted`.").deprecated(),
    fieldWithPath("sort.unsorted").description("Determines if the page is unsorted. Inverse of `sorted`.").deprecated(),
    fieldWithPath("totalElements").description("The total amounts of elements.").deprecated("page.total_elements"),
    fieldWithPath("totalPages").description("The number of total pages.").deprecated("page.total_pages"),
)

fun ignorePageableFieldsExceptContent(): Array<FieldDescriptor> = arrayOf(
    subsectionWithPath("pageable").ignored(),
    *(
        listOf(
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
            "totalPages",
            "page",
            "page.number",
            "page.size",
            "page.total_elements",
            "page.total_pages",
        ).map { fieldWithPath(it).ignored() }.toTypedArray()
    )
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

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(): T =
    description(listOfNotNull("*Deprecated*", description).joinToString(" "))

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(replaceWith: String): T =
    description(listOfNotNull("*Deprecated*. See `$replaceWith` for replacement.", description).joinToString(" "))
