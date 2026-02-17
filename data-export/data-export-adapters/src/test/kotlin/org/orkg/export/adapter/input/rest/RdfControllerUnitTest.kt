package org.orkg.export.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.Test
import org.orkg.export.adapter.input.rest.RdfController.Companion.DUMP_ENDPOINT
import org.orkg.export.adapter.input.rest.testing.fixtures.configuration.DataExportControllerUnitTestConfiguration
import org.orkg.export.input.ExportRDFUseCase
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.core.task.TaskExecutor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [RdfController::class, DataExportControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [RdfController::class])
internal class RdfControllerUnitTest : MockMvcBaseTest("rdf-hints") {
    @MockkBean
    private lateinit var rdfService: ExportRDFUseCase

    @MockkBean
    private lateinit var taskExecutor: TaskExecutor

    @Test
    fun legacyRedirectToDump() {
        documentedGetRequestTo(DUMP_ENDPOINT)
            .accept("application/n-triples")
            .perform()
            .andExpect(status().isMovedPermanently)
            .andExpect(header().string("Location", endsWith("/files/rdf-dumps/rdf-export-orkg.nt")))
            .andDocument {
                responseHeaders(
                    headerWithName("Location").description("Location to the rdf dump.")
                )
            }
    }

    @Test
    @TestWithMockAdmin
    fun createRdfDump() {
        every { taskExecutor.execute(any()) } just runs

        documentedPostRequestTo("/api/admin/rdf/dump")
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("RDF Dumps")
                summary("Creating RDF Dumps")
                description(
                    """
                    Dumps are created automatically by the system, but can also be triggered manually using a `POST` request.
                    
                    NOTE: This endpoint requires the admin role.
                    """
                )
            }

        verify(exactly = 1) { taskExecutor.execute(any()) }
    }
}
