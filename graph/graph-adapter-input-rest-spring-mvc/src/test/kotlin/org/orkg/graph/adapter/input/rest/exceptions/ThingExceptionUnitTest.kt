package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.domain.ThingNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class ThingExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun thingAlreadyExists() {
        documentedGetRequestTo(ThingAlreadyExists(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A thing with id "R123" already exists.""")
            .andExpect(jsonPath("$.thing_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("thing_id").description("The id of the thing."),
                    )
                )
            )
    }

    @Test
    fun thingNotFound() {
        documentedGetRequestTo(ThingNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:thing_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Thing "R123" not found.""")
            .andExpect(jsonPath("$.thing_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("thing_id").description("The id of the thing."),
                    )
                )
            )
    }
}
