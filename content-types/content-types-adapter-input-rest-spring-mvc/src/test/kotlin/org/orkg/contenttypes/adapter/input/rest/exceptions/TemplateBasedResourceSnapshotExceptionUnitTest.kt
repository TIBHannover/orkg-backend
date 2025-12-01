package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class TemplateBasedResourceSnapshotExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateBasedResourceSnapshotNotFound() {
        val type = "orkg:problem:template_based_resource_snapshot_not_found"
        documentedGetRequestTo(TemplateBasedResourceSnapshotNotFound(SnapshotId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template based resource snapshot "R123" not found.""")
            .andExpect(jsonPath("$.template_based_resource_snapshot_id").value("R123"))
            .andDocument {
                responseFields<TemplateBasedResourceSnapshotNotFound>(
                    fieldWithPath("template_based_resource_snapshot_id").description("The id of the template based resource snapshot.").type<SnapshotId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
