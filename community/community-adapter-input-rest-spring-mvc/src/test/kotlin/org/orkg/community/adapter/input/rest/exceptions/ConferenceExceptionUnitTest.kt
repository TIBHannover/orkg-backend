package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ConferenceSeriesAlreadyExists
import org.orkg.community.domain.ConferenceSeriesNotFound
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
internal class ConferenceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun conferenceSeriesAlreadyExists_withName() {
        documentedGetRequestTo(ConferenceSeriesAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:conference_series_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Conference series with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.name", `is`("Cool name")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("name").type("String").description("The name of the conference series that already exists. (optional, either `name` or `display_id` is present)").optional(),
                        fieldWithPath("display_id").type("String").description("The display_id of the conference series that already exists. (optional, either `name` or `display_id` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun conferenceSeriesAlreadyExists_withDisplayId() {
        get(ConferenceSeriesAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:conference_series_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Conference series with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.display_id", `is`("cool_name")))
    }

    @Test
    fun conferenceNotFound_withId() {
        documentedGetRequestTo(ConferenceSeriesNotFound("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:conference_series_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Conference series "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" not found.""")
    }
}
