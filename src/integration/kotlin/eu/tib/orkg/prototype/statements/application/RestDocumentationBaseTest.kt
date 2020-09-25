package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

/**
 * Base class for REST API documentation test.
 *
 * It initializes MockMVc with a stand-alone set-up for testing a controller
 * in isolation. Additionally, spring-restdoc will be pre-configured.
 */
@SpringBootTest
@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
abstract class RestDocumentationBaseTest {

    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected val snippet = "{class-name}-{method-name}"

    abstract fun createController(): Any

    @BeforeEach
    fun setup(
        restDocumentation: RestDocumentationContextProvider
    ) {
        mockMvc = standaloneSetup(createController())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .apply<StandaloneMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .alwaysDo<StandaloneMockMvcBuilder>(
                document(
                    "{class-name}-{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                )
            )
            .build()
    }

    protected fun getRequestTo(urlTemplate: String): MockHttpServletRequestBuilder =
        get(urlTemplate)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")

    protected fun getFileRequestTo(urlTemplate: String): MockHttpServletRequestBuilder =
        get(urlTemplate)
            .accept(MediaType.parseMediaType("application/n-triples"))
            .characterEncoding("utf-8")

    protected fun postRequestWithBody(url: String, body: Map<String, Any?>): MockHttpServletRequestBuilder =
        post(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(objectMapper.writeValueAsString(body))

    protected fun putRequestWithBody(url: String, body: Map<String, Any?>): MockHttpServletRequestBuilder =
        put(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(objectMapper.writeValueAsString(body))

    protected fun deleteRequest(url: String): MockHttpServletRequestBuilder =
        delete(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")

    protected fun createdResponseHeaders(): ResponseHeadersSnippet =
        responseHeaders(
            headerWithName("Location").description("Location to the created statement")
        )
}
