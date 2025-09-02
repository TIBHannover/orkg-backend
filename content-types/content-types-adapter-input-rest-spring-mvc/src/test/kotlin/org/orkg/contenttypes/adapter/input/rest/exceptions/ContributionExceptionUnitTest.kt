package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.TooFewContributions
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
internal class ContributionExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun contributionNotFound() {
        val type = "orkg:problem:contribution_not_found"
        documentedGetRequestTo(ContributionNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Contribution "R123" not found.""")
            .andExpect(jsonPath("$.contribution_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("contribution_id").description("The id of the contribution."),
                    )
                )
            )
    }

    @Test
    fun tooFewContributions() {
        val type = "orkg:problem:too_few_contributions"
        documentedGetRequestTo(TooFewContributions(listOf(ThingId("R123"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too few ids: At least two ids are required. Got only "1".""")
            .andExpect(jsonPath("$.contribution_ids[0]").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("contribution_ids[]").description("The ids of the provided contributions."),
                    )
                )
            )
    }
}
