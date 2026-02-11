package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationNotFound
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
internal class VisualizationExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun visualizationNotFound() {
        val type = "orkg:problem:visualization_not_found"
        documentedGetRequestTo(VisualizationNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Visualization "R123" not found.""")
            .andExpect(jsonPath("$.visualization_id").value("R123"))
            .andDocument {
                responseFields<VisualizationNotFound>(
                    fieldWithPath("visualization_id").description("The id of the visualization.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
