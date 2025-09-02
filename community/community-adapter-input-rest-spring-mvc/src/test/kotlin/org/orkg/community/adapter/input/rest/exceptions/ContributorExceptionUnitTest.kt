package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ContributorAlreadyExists
import org.orkg.community.domain.ContributorNotFound
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("contributor_id").description("The id of the contributor."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("contributor_id").description("The id of the contributor."),
                    )
                )
            )
    }
}
