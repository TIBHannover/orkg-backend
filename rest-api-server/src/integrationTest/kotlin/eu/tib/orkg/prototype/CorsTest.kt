package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.services.ResourceService
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import java.util.stream.Stream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class CorsTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var comparisonRepository: ComparisonRepository

    @Autowired
    private lateinit var contributionRepository: ContributionRepository

    @Autowired
    private lateinit var visualizationRepository: VisualizationRepository

    @Autowired
    private lateinit var smartReviewRepository: SmartReviewRepository

    @Autowired
    private lateinit var repository: ResourceRepository

    @Autowired
    private lateinit var statementRepository: StatementRepository

    @Autowired
    private lateinit var classRepository: ClassRepository

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .dispatchOptions<DefaultMockMvcBuilder>(true)
            .build()
    }

    @DisplayName("CORS Pre-flight requests should pass with `200 OK`")
    @ParameterizedTest(name = "to endpoint {0} requesting method {1}")
    @ArgumentsSource(RequestArgumentsProvider::class)
    fun preflightRequestToOauthEndpointWorksFromAnyOrigin(endpoint: String, method: String) {
        mockMvc
            .perform(
                options(endpoint)
                    .header("Origin", "http://example.com")
                    .header("Access-Control-Request-Method", method)
            )
            .andExpect(status().isOk)
            .andExpect(allOriginsAllowed())
            .andExpect(allAllowedMethodsPresent())
    }

    internal class RequestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                // API endpoints
                Arguments.of("/api/resources/", "POST"),
                // AuthorizationServer endpoints
                Arguments.of("/oauth/token", "POST"),
                Arguments.of("/oauth/token_key", "POST"),
                Arguments.of("/oauth/check_token", "POST")
            )
    }

    private fun allOriginsAllowed(): ResultMatcher =
        header().string("Access-Control-Allow-Origin", "*")

    private fun allAllowedMethodsPresent(): ResultMatcher =
        header().string("Access-Control-Allow-Methods", "OPTIONS,GET,HEAD,POST,PUT,PATCH,DELETE")

    @Configuration
    inner class TestConfiguration {
        @Bean
        fun resourceService(): ResourceUseCases = object : ResourceService(
            comparisonRepository,
            contributionRepository,
            visualizationRepository,
            smartReviewRepository,
            repository,
            statementRepository,
            classRepository
        ) {
            override fun findAll(pageable: Pageable): Page<Resource> = Page.empty(pageable)
        }
    }
}
