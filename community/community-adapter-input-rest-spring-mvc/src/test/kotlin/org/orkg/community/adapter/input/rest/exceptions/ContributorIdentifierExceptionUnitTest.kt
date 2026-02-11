package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifierAlreadyExists
import org.orkg.community.domain.UnknownIdentifierType
import org.orkg.community.testing.fixtures.configuration.CommunityControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommunityControllerExceptionUnitTestConfiguration::class])
internal class ContributorIdentifierExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun unknownIdentifierType() {
        val type = "orkg:problem:unknown_identifier_type"
        documentedGetRequestTo(UnknownIdentifierType("doi"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown identifier type "doi".""")
            .andExpect(jsonPath("$.identifier_type", `is`("doi")))
            .andDocument {
                responseFields<UnknownIdentifierType>(
                    fieldWithPath("identifier_type").description("The provided identifier type."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun contributorIdentifierAlreadyExists() {
        val type = "orkg:problem:contributor_identifier_already_exists"
        documentedGetRequestTo(ContributorIdentifierAlreadyExists(ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"), "identifier"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Identifier "identifier" for contributor "9d791767-245b-46e1-b260-2c00fb34efda" already exists.""")
            .andExpect(jsonPath("$.contributor_id", `is`("9d791767-245b-46e1-b260-2c00fb34efda")))
            .andExpect(jsonPath("$.identifier_value", `is`("identifier")))
            .andDocument {
                responseFields<ContributorIdentifierAlreadyExists>(
                    fieldWithPath("contributor_id").description("The id of the contributor.").type<ContributorId>(),
                    fieldWithPath("identifier_value").description("The value of the identifier."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
