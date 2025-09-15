package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.adapter.input.rest.TableController.CreateTableRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.TableRowRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.UpdateTableRequest
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityFilterValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTable
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(
    classes = [
        TableController::class,
        ExceptionTestConfiguration::class,
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
                        fieldWithPath("modifiable").description("Whether the table can be modified."),
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
        every { tableService.findById(id) } returns Optional.empty()

        get("/api/tables/$id")
            .accept(TABLE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:table_not_found")

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
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of $allowedVisibilityFilterValues."""),
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
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            tableService.findAll(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table create request, when service succeeds, it creates and returns the table")
    fun create() {
        val id = ThingId("R123")
        every { tableService.create(any()) } returns id

        documentedPostRequestTo("/api/tables")
            .content(createTableRequest())
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created table can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the table."),
                        fieldWithPath("resources").description("Definition of resources that need to be created. (optional)"),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created. (optional)"),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created. (optional)"),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created (optional)."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("classes").description("Definition of classes that need to be created. (optional)"),
                        fieldWithPath("classes.*.label").description("The label of the class."),
                        fieldWithPath("classes.*.uri").description("The uri of the class."),
                        fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table and must only consist of string literals. Additionally, one data row is required. Every row must have the same length."),
                        fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
                        subsectionWithPath("rows[].data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row.").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues."""),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table update request, when service succeeds, it updates the table")
    fun update() {
        val id = ThingId("R123")
        every { tableService.update(any()) } just runs

        documentedPutRequestTo("/api/tables/{id}", id)
            .content(updateTableRequest())
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the table.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated table can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the table. (optional)"),
                        fieldWithPath("resources").description("Definition of resources that need to be created. (optional)"),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created. (optional)"),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created. (optional)"),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created (optional)."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("classes").description("Definition of classes that need to be created. (optional)"),
                        fieldWithPath("classes.*.label").description("The label of the class."),
                        fieldWithPath("classes.*.uri").description("The uri of the class."),
                        fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table and must only consist of string literals. Additionally, one data row is required. Every row must have the same length. (optional)"),
                        fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
                        subsectionWithPath("rows[].data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row.").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to. (optional)"),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to. (optional)"),
                        fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues. (optional)"""),
                        fieldWithPath("visibility").description("""The method used to extract the table resource. Can be one of $allowedVisibilityValues. (optional)"""),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row create request, when service succeeds, it creates the table row")
    fun createTableRow() {
        val id = ThingId("R123")
        every { tableService.createTableRow(any()) } returns ThingId("R456")

        post("/api/tables/{id}/rows", id)
            .content(tableRowRequest())
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))

        verify(exactly = 1) { tableService.createTableRow(withArg { it.rowIndex shouldBe null }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row create request, when service succeeds, it creates the table row at the specified index")
    fun createTableRowAtIndex() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.createTableRow(any()) } returns ThingId("R456")

        documentedPostRequestTo("/api/tables/{id}/rows/{index}", id, 5)
            .content(tableRowRequest())
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the table."),
                        parameterWithName("index").description("The insertion index the of the row. If not specified, the row will be appended at the end of the table. (optional)"),
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated table can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("resources").description("Definition of resources that need to be created. (optional)"),
                        fieldWithPath("resources.*.label").type("String").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created. (optional)"),
                        fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                        fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created. (optional)"),
                        fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                        fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                        fieldWithPath("lists").description("Definition of lists that need to be created (optional)."),
                        fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                        fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                        fieldWithPath("classes").description("Definition of classes that need to be created. (optional)"),
                        fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                        fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                        fieldWithPath("row").description("The table row. It must have the same length as the table header"),
                        fieldWithPath("row.label").description("The label of the row. (optional)").optional(),
                        subsectionWithPath("row.data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.createTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row update request, when service succeeds, it update the table row")
    fun updateTableRow() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.updateTableRow(any()) } just runs

        documentedPutRequestTo("/api/tables/{id}/rows/{index}", id, 5)
            .content(tableRowRequest())
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the table."),
                        parameterWithName("index").description("The index of the row."),
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated table can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("resources").description("Definition of resources that need to be created. (optional)"),
                        fieldWithPath("resources.*.label").type("String").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created. (optional)"),
                        fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                        fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created. (optional)"),
                        fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                        fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                        fieldWithPath("lists").description("Definition of lists that need to be created (optional)."),
                        fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                        fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                        fieldWithPath("classes").description("Definition of classes that need to be created. (optional)"),
                        fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                        fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                        fieldWithPath("row").description("The table row. It must have the same length as the table header"),
                        fieldWithPath("row.label").description("The label of the row. (optional)").optional(),
                        subsectionWithPath("row.data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.updateTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row delete request, when service succeeds, it deletes the table row")
    fun deleteTableRow() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.deleteTableRow(any()) } just runs

        documentedDeleteRequestTo("/api/tables/{id}/rows/{index}", id, 5)
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the table."),
                        parameterWithName("index").description("The index of the row."),
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated table can be fetched from.")
                    ),
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { tableService.deleteTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    private fun createTableRequest() =
        CreateTableRequest(
            label = "Table Title",
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralRequestPart("column 1", Literals.XSD.STRING.prefixedUri),
                "#temp3" to CreateLiteralRequestPart("column 2", Literals.XSD.STRING.prefixedUri),
                "#temp4" to CreateLiteralRequestPart("column 3", Literals.XSD.STRING.prefixedUri)
            ),
            predicates = mapOf(
                "#temp5" to CreatePredicateRequestPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp6" to CreateListRequestPart(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp7" to CreateClassRequestPart(
                    label = "class",
                    uri = ParsedIRI.create("https://orkg.org/class/C1")
                )
            ),
            rows = listOf(
                RowRequest(
                    label = "header",
                    data = listOf("#temp1", "#temp2", "#temp3")
                ),
                RowRequest(
                    label = null,
                    data = listOf("R456", "#temp4", "#temp5")
                ),
                RowRequest(
                    label = "row 2",
                    data = listOf("#temp6", null, "#temp7")
                )
            ),
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            ),
            extractionMethod = ExtractionMethod.UNKNOWN
        )

    private fun updateTableRequest() =
        UpdateTableRequest(
            label = "Table Title",
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralRequestPart("column 1", Literals.XSD.STRING.prefixedUri),
                "#temp3" to CreateLiteralRequestPart("column 2", Literals.XSD.STRING.prefixedUri),
                "#temp4" to CreateLiteralRequestPart("column 3", Literals.XSD.STRING.prefixedUri)
            ),
            predicates = mapOf(
                "#temp5" to CreatePredicateRequestPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp6" to CreateListRequestPart(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp7" to CreateClassRequestPart(
                    label = "class",
                    uri = ParsedIRI.create("https://orkg.org/class/C1")
                )
            ),
            rows = listOf(
                RowRequest(
                    label = "header",
                    data = listOf("#temp1", "#temp2", "#temp3")
                ),
                RowRequest(
                    label = null,
                    data = listOf("R456", "#temp4", "#temp5")
                ),
                RowRequest(
                    label = "row 2",
                    data = listOf("#temp6", null, "#temp7")
                )
            ),
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            ),
            extractionMethod = ExtractionMethod.UNKNOWN,
            visibility = Visibility.DEFAULT
        )

    private fun tableRowRequest() =
        TableRowRequest(
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap(),
            row = RowRequest(
                label = "row 3",
                data = listOf("R456", "#temp1", null)
            ),
        )
}
