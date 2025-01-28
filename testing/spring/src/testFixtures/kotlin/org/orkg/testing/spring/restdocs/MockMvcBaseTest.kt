package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.configuration.SpringJacksonConfiguration
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.generate.RestDocumentationGenerator
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class, SpringJacksonConfiguration::class)
@ExtendWith(RestDocumentationExtension::class)
@TestPropertySource(properties = ["spring.jackson.mapper.sort-properties-alphabetically=true"])
abstract class MockMvcBaseTest(val prefix: String) : MockkBaseTest {

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    protected lateinit var documentationHandler: RestDocumentationResultHandler

    protected lateinit var mockMvc: MockMvc

    val identifier = "$prefix-{method-name}"

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
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

    protected fun MockMultipartHttpServletRequestBuilder.json(
        name: String,
        data: Map<String, Any>,
        originalFileName: String = "$name.json"
    ): MockMultipartHttpServletRequestBuilder =
        file(MockMultipartFile(name, originalFileName, MediaType.APPLICATION_JSON_VALUE, data.toContent().toByteArray()))

    companion object {
        fun get(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedGetRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.get(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun post(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.post(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPostRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.post(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun put(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.put(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPutRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.put(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun patch(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.patch(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPatchRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.patch(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun delete(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.delete(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedDeleteRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.delete(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun head(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.head(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedHeadRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.head(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun patchMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            multipart(PATCH, urlTemplate, *uriVariables)
                .characterEncoding(Charsets.UTF_8.name()) as MockMultipartHttpServletRequestBuilder

        fun documentedPatchMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            patchMultipart(urlTemplate, *uriVariables)
                .requestAttr(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE, urlTemplate) as MockMultipartHttpServletRequestBuilder

        fun ResultActions.andPrint(): ResultActions = andDo(MockMvcResultHandlers.print())
    }
}
