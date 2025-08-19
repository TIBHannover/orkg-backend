package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class TemplateBasedResourceSnapshotExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateBasedResourceSnapshotNotFound() {
        documentedGetRequestTo(TemplateBasedResourceSnapshotNotFound(SnapshotId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_based_resource_snapshot_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template based resource snapshot "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
