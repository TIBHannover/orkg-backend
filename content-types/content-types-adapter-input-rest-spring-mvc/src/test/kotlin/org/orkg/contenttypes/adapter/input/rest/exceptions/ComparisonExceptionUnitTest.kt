package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ComparisonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun comparisonAlreadyPublished() {
        documentedGetRequestTo(ComparisonAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:comparison_already_published")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison "R123" is already published.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun comparisonNotModifiable() {
        documentedGetRequestTo(ComparisonNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:comparison_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun comparisonRelatedResourceNotModifiable() {
        documentedGetRequestTo(ComparisonRelatedResourceNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:comparison_related_resource_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison related resource "R123" is not modifiable.""")
    }

    @Test
    fun comparisonRelatedFigureNotModifiable() {
        documentedGetRequestTo(ComparisonRelatedFigureNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:comparison_related_figure_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison related figure "R123" is not modifiable.""")
    }
}
