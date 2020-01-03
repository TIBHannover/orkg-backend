package eu.tib.orkg.prototype

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(properties = ["orkg.cors.origins=https://example.com"])
class CorsCustomPropertyTest : CorsBaseTest() {
    @Test
    fun customOriginWillAllowFromThatOrigin() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.options("/api/resources/")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Origin", "https://example.com")
            )
            .andExpect(status().isOk)
            .andExpect(customOrigin())
            .andExpect(allAllowedMethodsPresent())
    }

    @Test
    fun customOriginWillFailFromDifferentOrigin() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.options("/api/resources/")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Origin", "https://other-domain.com")
            )
            .andExpect(status().isForbidden)
            .andExpect(content().string("Invalid CORS request"))
    }

    private fun customOrigin() =
        header().string("Access-Control-Allow-Origin", "https://example.com")
}
