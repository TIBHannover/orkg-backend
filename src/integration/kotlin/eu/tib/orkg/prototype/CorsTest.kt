package eu.tib.orkg.prototype

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
class CorsTest : CorsBaseTest() {
    @Test
    fun preflightRequestToApiWorksFromAnyOrigin() {
        mockMvc
            .perform(
                options("/api/resources/")
                    .header("Origin", "https://example.com")
                    .header("Access-Control-Request-Method", "POST")
            )
            .andExpect(status().isOk)
            .andExpect(allOriginsAllowed())
            .andExpect(allAllowedMethodsPresent())
    }

    @Test
    fun preflightRequestToOauthEndpointWorksFromAnyOrigin() {
        mockMvc
            .perform(
                options("/oauth/token")
                    .header("Origin", "http://example.com")
                    .header("Access-Control-Request-Method", "POST")
            )
            .andExpect(status().isOk)
            .andExpect(allOriginsAllowed())
            .andExpect(allAllowedMethodsPresent())
    }
}
