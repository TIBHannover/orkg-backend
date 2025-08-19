package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class CommonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMonth() {
        documentedGetRequestTo(InvalidMonth(0))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_month")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid month "0". Must be in range [1..12].""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun sustainableDevelopmentGoalNotFound() {
        documentedGetRequestTo(SustainableDevelopmentGoalNotFound(ThingId("SDG1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:sustainable_development_goal_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Sustainable Development Goal "SDG1" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidBibTeXReference() {
        documentedGetRequestTo(InvalidBibTeXReference("not bibtex"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_bibtex_reference")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid BibTeX reference "not bibtex".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
