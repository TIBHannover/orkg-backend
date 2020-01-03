package eu.tib.orkg.prototype

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
class CorsDefaultPropertyTest : CorsBaseTest() {
    @Test
    fun defaultSettingWillAllowFromAllOrigins() {
        mockMvc
            .perform(
                options("/api/resources/")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Origin", "https://example.com")
            )
            .andExpect(status().isOk)
            .andExpect(allOriginsAllowed())
            .andExpect(allAllowedMethodsPresent())
    }
}
