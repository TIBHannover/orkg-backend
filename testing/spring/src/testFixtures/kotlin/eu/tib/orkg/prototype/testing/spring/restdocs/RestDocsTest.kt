package eu.tib.orkg.prototype.testing.spring.restdocs

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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@ExtendWith(RestDocumentationExtension::class)
abstract class RestDocsTest(private val prefix: String) {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    protected lateinit var documentationHandler: RestDocumentationResultHandler

    protected lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup(
        restDocumentation: RestDocumentationContextProvider
    ) {
        documentationHandler = document(
            "$prefix-{method-name}",
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
}
