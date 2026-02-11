package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorAlreadyExists
import org.orkg.community.domain.ContributorNotFound
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
internal class ContributorExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun contributorAlreadyExists() {
        val type = "orkg:problem:contributor_already_exists"
        documentedGetRequestTo(ContributorAlreadyExists(ContributorId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Contributor "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" already exists.""")
            .andExpect(jsonPath("$.contributor_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDocument {
                responseFields<ContributorAlreadyExists>(
                    fieldWithPath("contributor_id").description("The id of the contributor.").type<ContributorId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun contributorNotFound() {
        val type = "orkg:problem:contributor_not_found"
        documentedGetRequestTo(ContributorNotFound(ContributorId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Contributor "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andExpect(jsonPath("$.contributor_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDocument {
                responseFields<ContributorNotFound>(
                    fieldWithPath("contributor_id").description("The id of the contributor.").type<ContributorId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
