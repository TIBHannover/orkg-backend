package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.TooFewContributions
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ContributionExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun contributionNotFound() {
        documentedGetRequestTo(ContributionNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:contribution_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Contribution "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooFewContributions() {
        documentedGetRequestTo(TooFewContributions(listOf(ThingId("R123"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_few_contributions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too few ids: At least two ids are required. Got only "1".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
