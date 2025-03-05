package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(
    classes = [
        TableController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [TableController::class])
internal class TableControllerUnitTest : MockMvcBaseTest("tables") {
    @MockkBean
    private lateinit var tableService: TableUseCases

    @Test
    @DisplayName("Given a table, when it is fetched by id and service succeeds, then status is 200 OK and table is returned")
    fun getSingle() {
        val table = createTable()
        every { tableService.findById(table.id) } returns Optional.of(table)

        documentedGetRequestTo("/api/tables/{id}", table.id)
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andExpectTable()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the table to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the table."),
                        fieldWithPath("label").description("The label of the table."),
                        fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table."),
                        fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
                        subsectionWithPath("rows[].data[]").description("The ordered list of values (thing representations) of the row.").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues."""),
                        timestampFieldWithPath("created_at", "the table resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this table."),
                        fieldWithPath("visibility").description("""Visibility of the table. Can be one of $allowedVisibilityValues."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this table.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.findById(table.id) }
    }

    @Test
    fun `Given a table, when it is fetched by id and service reports missing table, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = TableNotFound(id)
        every { tableService.findById(id) } returns Optional.empty()

        get("/api/tables/$id")
            .accept(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/tables/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { tableService.findById(id) }
    }

    @Test
    @DisplayName("Given several tables, when they are fetched, then status is 200 OK and tables are returned")
    fun getPaged() {
        every {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createTable())

        documentedGetRequestTo("/api/tables")
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTable("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several tables, when filtering by several parameters, then status is 200 OK and tables are returned")
    fun getPagedWithParameters() {
        every {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createTable())

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")

        documentedGetRequestTo("/api/tables")
            .param("q", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTable("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label of the table. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this table. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            tableService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        }
    }

    @Test
    fun `Given several tables, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/tables")
            .param("sort", "unknown")
            .accept(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/tables"))

        verify(exactly = 1) {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }
}
