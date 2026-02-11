package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.community.domain.ConferenceSeriesAlreadyExists
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.ConferenceSeriesNotFound
import org.orkg.community.testing.fixtures.configuration.CommunityControllerExceptionUnitTestConfiguration
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
@ContextConfiguration(classes = [CommunityControllerExceptionUnitTestConfiguration::class])
internal class ConferenceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun conferenceSeriesAlreadyExists_withName() {
        val type = "orkg:problem:conference_series_already_exists"
        documentedGetRequestTo(ConferenceSeriesAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Conference series with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.conference_series_name", `is`("Cool name")))
            .andDocument {
                responseFields<ConferenceSeriesAlreadyExists>(
                    fieldWithPath("conference_series_name").type("String").description("The name of the conference series. (optional, either `conference_series_name` or `conference_series_display_id` is present)").optional(),
                    fieldWithPath("conference_series_display_id").type("String").description("The display_id of the conference series. (optional, either `conference_series_name` or `conference_series_display_id` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun conferenceSeriesAlreadyExists_withDisplayId() {
        get(ConferenceSeriesAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:conference_series_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Conference series with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.conference_series_display_id", `is`("cool_name")))
    }

    @Test
    fun conferenceNotFound_withId() {
        val type = "orkg:problem:conference_series_not_found"
        documentedGetRequestTo(ConferenceSeriesNotFound("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Conference series "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" not found.""")
            .andExpect(jsonPath("$.conference_series_id", `is`("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andDocument {
                responseFields<ConferenceSeriesNotFound>(
                    fieldWithPath("conference_series_id").description("The id of the conference series.").type<ConferenceSeriesId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
