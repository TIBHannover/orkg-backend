package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
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
            .andDocument {
                responseFields<ContributionNotFound>(
                    fieldWithPath("contribution_id").description("The id of the contribution.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
