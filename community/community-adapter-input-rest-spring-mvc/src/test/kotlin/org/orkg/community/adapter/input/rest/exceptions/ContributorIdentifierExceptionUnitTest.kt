package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ContributorIdentifierAlreadyExists
import org.orkg.community.domain.UnknownIdentifierType
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class ContributorIdentifierExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun unknownIdentifierType() {
        documentedGetRequestTo(UnknownIdentifierType("doi"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_identifier_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown identifier type "doi".""")
            .andExpect(jsonPath("$.identifier_type", `is`("doi")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("identifier_type").description("The provided identifer type."),
                    )
                )
            )
    }

    @Test
    fun contributorIdentifierAlreadyExists() {
        documentedGetRequestTo(ContributorIdentifierAlreadyExists(ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"), "identifier"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:contributor_identifier_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Identifier "identifier" for contributor "9d791767-245b-46e1-b260-2c00fb34efda" already exists.""")
            .andExpect(jsonPath("$.contributor_id", `is`("9d791767-245b-46e1-b260-2c00fb34efda")))
            .andExpect(jsonPath("$.identifier_value", `is`("identifier")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("contributor_id").description("The id of the contributor."),
                        fieldWithPath("identifier_value").description("The value of the identifier."),
                    )
                )
            )
    }
}
