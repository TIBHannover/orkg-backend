package org.orkg.testing.spring.restdocs

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@ExtendWith(RestDocumentationExtension::class)
@TestPropertySource(properties = ["spring.jackson.mapper.sort-properties-alphabetically=true"])
abstract class RestDocsTest(private val prefix: String) {

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    protected lateinit var documentationHandler: RestDocumentationResultHandler

    protected lateinit var mockMvc: MockMvc

    val identifier = "$prefix-{method-name}"

    @BeforeEach
    fun setup(
        restDocumentation: RestDocumentationContextProvider
    ) {
        documentationHandler = document(
            identifier,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
        )

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
            )
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .uris().withScheme("https").withHost("incubating.orkg.org").withPort(443)
            )
            //.alwaysDo<DefaultMockMvcBuilder>(documentationHandler)
            .build()
    }

    /**
     * Syntactic sugar to generate the default documentation snippets (`curl-request.adoc`, etc.).
     */
    protected fun generateDefaultDocSnippets(): ResultHandler = documentationHandler

    protected fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)

    protected fun MockHttpServletRequestBuilder.content(body: Any): MockHttpServletRequestBuilder =
        content(body.toContent())

    protected fun post(string: String, body: Any): MockHttpServletRequestBuilder =
        MockMvcRequestBuilders.post(string).content(body.toContent())

    private fun Any.toContent(): String = if (this is String) this else objectMapper.writeValueAsString(this)

    fun pageableFields(): Array<FieldDescriptor> = arrayOf(
        fieldWithPath("content").description("The result of the request as a (sorted) array."),
        *pageableFieldsWithoutContent(),
    )

    fun pageableFieldsWithoutContent(): Array<FieldDescriptor> = arrayOf(
        subsectionWithPath("pageable").ignored(), // Pageable used in request. Not relevant in most cases.
        fieldWithPath("empty").description("Determines if the current page is empty."),
        fieldWithPath("first").description("Determines if the current page is the first one."),
        fieldWithPath("last").description("Determines if the current page is the last one."),
        fieldWithPath("number").description("The number of the current page."),
        fieldWithPath("numberOfElements").description("The number of elements currently on this page."),
        fieldWithPath("size").description("The size of the current page."),
        fieldWithPath("sort").description("The sorting parameters for this page."),
        fieldWithPath("sort.empty").description("Determines if the sort object is empty."),
        fieldWithPath("sort.sorted").description("Determines if the page is sorted. Inverse of `unsorted`."),
        fieldWithPath("sort.unsorted").description("Determines if the page is unsorted. Inverse of `sorted`."),
        fieldWithPath("totalElements").description("The total amounts of elements."),
        fieldWithPath("totalPages").description("The number of total pages."),
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
        ).map { fieldWithPath(it).ignored() }.toTypedArray()),
    )
}
