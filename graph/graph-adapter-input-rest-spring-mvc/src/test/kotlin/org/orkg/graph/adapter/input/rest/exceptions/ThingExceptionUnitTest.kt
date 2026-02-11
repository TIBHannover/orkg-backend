package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.domain.ThingNotFound
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [GraphControllerExceptionUnitTestConfiguration::class])
internal class ThingExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun thingAlreadyExists() {
        val type = "orkg:problem:thing_already_exists"
        documentedGetRequestTo(ThingAlreadyExists(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A thing with id "R123" already exists.""")
            .andExpect(jsonPath("$.thing_id", `is`("R123")))
            .andDocument {
                responseFields<ThingAlreadyExists>(
                    fieldWithPath("thing_id").description("The id of the thing.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun thingNotFound() {
        val type = "orkg:problem:thing_not_found"
        documentedGetRequestTo(ThingNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Thing "R123" not found.""")
            .andExpect(jsonPath("$.thing_id", `is`("R123")))
            .andDocument {
                responseFields<ThingNotFound>(
                    fieldWithPath("thing_id").description("The id of the thing.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
