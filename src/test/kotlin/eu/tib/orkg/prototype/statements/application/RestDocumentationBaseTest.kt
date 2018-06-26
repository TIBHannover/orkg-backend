package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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

    fun getRequestTo(endpoint: String) =
        mockMvc
            .perform(get(endpoint).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
}
