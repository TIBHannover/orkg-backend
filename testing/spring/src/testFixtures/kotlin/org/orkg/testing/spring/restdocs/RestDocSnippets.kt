package org.orkg.testing.spring.restdocs

import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

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
