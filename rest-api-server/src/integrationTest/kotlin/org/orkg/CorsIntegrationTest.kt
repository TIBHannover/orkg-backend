package org.orkg

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceService
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime
import java.util.stream.Stream

@SpringBootTest
@Import(CorsIntegrationTest.FakeController::class)
@Suppress("HttpUrlsUsage")
internal class CorsIntegrationTest : MockMvcBaseTest("cors") {
    @Autowired
    private lateinit var repository: ResourceRepository

    @Autowired
    private lateinit var statementRepository: StatementRepository

    @Autowired
    private lateinit var classRepository: ClassRepository

    @MockkBean
    private lateinit var contributorRepository: ContributorRepository

    @MockkBean
    private lateinit var thingRepository: ThingRepository

    @MockkBean
    private lateinit var unsafeResourceUseCases: UnsafeResourceUseCases

    @MockkBean
    private lateinit var observatoryRepository: ObservatoryRepository

    @MockkBean
    private lateinit var organizationRepository: OrganizationRepository

    @DisplayName("CORS Pre-flight requests should pass with `200 OK`")
    @ParameterizedTest(name = "to endpoint {0} requesting method {1}")
    @ArgumentsSource(RequestArgumentsProvider::class)
    fun preflightRequestToOauthEndpointWorksFromAnyOrigin(endpoint: String, method: String) {
        options(endpoint)
            .header("Origin", "http://example.com")
            .header("Access-Control-Request-Method", method)
            .perform()
            .andExpect(status().isOk)
            .andExpect(allOriginsAllowed())
            .andExpect(allAllowedMethodsPresent())
    }

    @Test
    @DisplayName("CORS Preflight request declares Location header as safe")
    fun preflightRequestDeclaresLocationHeaderAsSafe() {
        val response = mockMvc
            .perform(options("/headers/location").header("Origin", "https://example.com"))
            .andExpect(status().isOk)
            .andReturn()
            .response

        val exposedHeadersField = response.getHeaderValue("Access-Control-Expose-Headers")

        assertThat(exposedHeadersField).isNotNull

        val safeHeaders = exposedHeadersField?.toString()?.split(", ").orEmpty()

        assertThat(safeHeaders).contains("Location")
    }

    internal class RequestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                // API endpoints
                Arguments.of("/api/resources", "POST"),
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
            repository,
            statementRepository,
            classRepository,
            contributorRepository,
            thingRepository,
            unsafeResourceUseCases,
            observatoryRepository,
            organizationRepository,
        ) {
            override fun findAll(
                pageable: Pageable,
                label: SearchString?,
                visibility: VisibilityFilter?,
                createdBy: ContributorId?,
                createdAtStart: OffsetDateTime?,
                createdAtEnd: OffsetDateTime?,
                includeClasses: Set<ThingId>,
                excludeClasses: Set<ThingId>,
                baseClass: ThingId?,
                observatoryId: ObservatoryId?,
                organizationId: OrganizationId?,
            ): Page<Resource> = Page.empty(pageable)
        }
    }

    @TestComponent
    @RestController
    internal class FakeController {
        @GetMapping("/headers/location")
        fun testLocation(uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> =
            created(uriComponentsBuilder.path("headers/location/created").build().toUri()).build()
    }
}
