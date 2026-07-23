package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.InvalidHopBounds
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [GraphControllerExceptionUnitTestConfiguration::class])
internal class SubgraphExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidHopBounds() {
        val type = "orkg:problem:invalid_hop_bounds"
        documentedGetRequestTo(InvalidHopBounds(5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid hop bounds. Min hops must be less than or equal to max hops. Found: min: "5", max: "2".""")
            .andExpect(jsonPath("$.min_hops").value("5"))
            .andExpect(jsonPath("$.max_hops").value("2"))
            .andDocument {
                responseFields<InvalidHopBounds>(
                    fieldWithPath("min_hops").description("The provided min hops.").type<Int>(),
                    fieldWithPath("max_hops").description("The provided max hops.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
