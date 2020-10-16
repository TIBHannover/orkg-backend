package eu.tib.orkg.prototype

import java.util.stream.Stream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
class CorsTest : CorsBaseTest() {
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
}
