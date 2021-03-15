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
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestParametersSnippet
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import testhelper.spring.testcontainers.Neo4jContainerTest

/**
 * Base class for REST API documentation test.
 *
 * It initializes MockMVc with a stand-alone set-up for testing a controller
 * in isolation. Additionally, spring-restdoc will be pre-configured.
 */
@SpringBootTest
@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
abstract class RestDocumentationBaseTest : Neo4jContainerTest() {

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
            // These tests are not auto-configured so @EnableSpringDataWebSupport is missing which means we need to
            // configure the handlers and resolvers ourself.
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

    protected fun putRequest(url: String): MockHttpServletRequestBuilder =
        put(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")

    protected fun deleteRequest(url: String): MockHttpServletRequestBuilder =
        delete(url)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .characterEncoding("utf-8")

    protected fun createdResponseHeaders(): ResponseHeadersSnippet =
        responseHeaders(
            headerWithName("Location").description("Location to the created statement")
        )

    protected fun pageableRequestParameters(vararg descriptors: ParameterDescriptor):
        RequestParametersSnippet =
            RequestDocumentation.requestParameters(
                mutableListOf(
                    RequestDocumentation.parameterWithName("page").description(
                        "Page number of items to fetch (default: 1)"
                    )
                        .optional(),
                    RequestDocumentation.parameterWithName("size").description(
                        "Number of items to fetch per page (default: 10)"
                    )
                        .optional(),
                    RequestDocumentation.parameterWithName("sort").description(
                        "Key to sort by (default: not provided)"
                    )
                        .optional()
                ).apply {
                    addAll(descriptors)
                }
            )

    protected fun pageableDetailedFieldParameters(): List<FieldDescriptor> = listOf(
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
        fieldWithPath("empty").description("The empty attribute"))
}
