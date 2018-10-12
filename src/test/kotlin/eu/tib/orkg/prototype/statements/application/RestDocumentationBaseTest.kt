package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.springframework.boot.test.autoconfigure.data.neo4j.*
import org.springframework.restdocs.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.setup.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders.*

/**
 * Base class for REST API documentation test.
 *
 * It initializes MockMVc with a stand-alone set-up for testing a controller
 * in isolation. Additionally, spring-restdoc will be pre-configured.
 */
@DataNeo4jTest
@ExtendWith(RestDocumentationExtension::class)
abstract class RestDocumentationBaseTest {

    protected lateinit var mockMvc: MockMvc

    protected var objectMapper = ObjectMapper()

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
}
