package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.http.MediaType.*
import org.springframework.restdocs.*
import org.springframework.restdocs.headers.*
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.test.context.junit.jupiter.*
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders.*

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

    protected var document = document(
        "{class-name}-{method-name}",
        preprocessRequest(prettyPrint()),
        preprocessResponse(prettyPrint())
    )

    protected val snippet = "{class-name}-{method-name}"

    abstract fun createController(): Any

    @BeforeEach
    fun setup(
        restDocumentation: RestDocumentationContextProvider
    ) {
        mockMvc = standaloneSetup(createController())
            .apply<StandaloneMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .alwaysDo<StandaloneMockMvcBuilder>(document)
            .build()
    }

    protected fun getRequestTo(urlTemplate: String): MockHttpServletRequestBuilder =
        get(urlTemplate)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")

    protected fun postRequestWithBody(url: String, body: Map<String, Any?>): MockHttpServletRequestBuilder =
        post(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(objectMapper.writeValueAsString(body))

    protected fun createdResponseHeaders(): ResponseHeadersSnippet =
        responseHeaders(
            headerWithName("Location").description("Location to the created statement")
        )
}
