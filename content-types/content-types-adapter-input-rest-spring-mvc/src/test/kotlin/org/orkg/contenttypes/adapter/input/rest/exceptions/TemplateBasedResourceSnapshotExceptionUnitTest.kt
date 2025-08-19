package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeJacksonModule::class, FixedClockConfig::class])
internal class TemplateBasedResourceSnapshotExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateBasedResourceSnapshotNotFound() {
        documentedGetRequestTo(TemplateBasedResourceSnapshotNotFound(SnapshotId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_based_resource_snapshot_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template based resource snapshot "R123" not found.""")
            .andExpect(jsonPath("$.template_based_resource_snapshot_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_based_resource_snapshot_id").description("The id of the template based resource snapshot."),
                    )
                )
            )
    }
}
