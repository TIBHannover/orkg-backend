package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class VisualizationExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun visualizationNotFound() {
        documentedGetRequestTo(VisualizationNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:visualization_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Visualization "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
