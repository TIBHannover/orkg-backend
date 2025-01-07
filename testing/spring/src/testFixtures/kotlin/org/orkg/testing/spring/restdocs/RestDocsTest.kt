package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.configuration.PagedSerializationConfiguration
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class, PagedSerializationConfiguration::class)
@ExtendWith(RestDocumentationExtension::class)
@TestPropertySource(properties = ["spring.jackson.mapper.sort-properties-alphabetically=true"])
abstract class RestDocsTest(val prefix: String) : MockkBaseTest {

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
        documentationHandler = MockMvcRestDocumentationWrapper.document(
            identifier,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
        )

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .and()
                    .uris().withScheme("https").withHost("incubating.orkg.org").withPort(443)
            )
            .build()
    }

    /**
     * Syntactic sugar to generate the default documentation snippets (`curl-request.adoc`, etc.).
     */
    protected fun generateDefaultDocSnippets(): ResultHandler = documentationHandler

    protected fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)

    protected fun MockHttpServletRequestBuilder.content(body: Any): MockHttpServletRequestBuilder =
        content(body.toContent())

    private fun Any.toContent(): String = if (this is String) this else objectMapper.writeValueAsString(this)

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
