package org.orkg

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.ParameterDeclarations
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.annotations.IntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.stream.Stream

@IntegrationTest
@Import(CorsIntegrationTest.FakeController::class)
@Suppress("HttpUrlsUsage")
internal class CorsIntegrationTest : MockMvcBaseTest("cors") {
    @Autowired
    private lateinit var repository: ResourceRepository

    @Autowired
    private lateinit var statementRepository: StatementRepository

    @Autowired
    private lateinit var classRepository: ClassRepository

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
        val response = options("/headers/location")
            .header("Origin", "https://example.com")
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response

        val exposedHeadersField = response.getHeaderValue("Access-Control-Expose-Headers")

        assertThat(exposedHeadersField).isNotNull

        val safeHeaders = exposedHeadersField?.toString()?.split(", ").orEmpty()

        assertThat(safeHeaders).contains("Location")
    }

    internal class RequestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(parameters: ParameterDeclarations, context: ExtensionContext): Stream<out Arguments> =
            Stream.of(
                // API endpoints
                Arguments.of("/api/resources", "POST"),
                // AuthorizationServer endpoints
                Arguments.of("/oauth/token", "POST"),
                Arguments.of("/oauth/token_key", "POST"),
                Arguments.of("/oauth/check_token", "POST"),
            )
    }

    private fun allOriginsAllowed(): ResultMatcher =
        header().string("Access-Control-Allow-Origin", "*")

    private fun allAllowedMethodsPresent(): ResultMatcher =
        header().string("Access-Control-Allow-Methods", "OPTIONS,GET,HEAD,POST,PUT,PATCH,DELETE")

    @TestComponent
    @RestController
    internal class FakeController {
        @GetMapping("/headers/location")
        fun testLocation(uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> =
            created(uriComponentsBuilder.path("headers/location/created").build().toUri()).build()
    }
}
