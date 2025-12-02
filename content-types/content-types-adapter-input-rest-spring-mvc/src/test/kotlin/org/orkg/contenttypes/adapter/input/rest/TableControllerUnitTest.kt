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
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.TableController.CreateTableRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.CreateTableRowRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.TableColumnRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.UpdateTableCellRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.UpdateTableRequest
import org.orkg.contenttypes.adapter.input.rest.TableController.UpdateTableRowRequest
import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTableColumnIndex
import org.orkg.contenttypes.domain.InvalidTableRowIndex
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.MissingTableColumnValues
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.TooFewTableColumns
import org.orkg.contenttypes.domain.TooFewTableRows
import org.orkg.contenttypes.domain.TooManyTableColumnValues
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.tableResponseFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTable
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(classes = [TableController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [TableController::class])
internal class TableControllerUnitTest : MockMvcBaseTest("tables") {
    @MockkBean
    private lateinit var tableService: TableUseCases

    @Test
    @DisplayName("Given a table, when it is fetched by id and service succeeds, then status is 200 OK and table is returned")
    fun findById() {
        val table = createTable()
        every { tableService.findById(table.id) } returns Optional.of(table)

        documentedGetRequestTo("/api/tables/{id}", table.id)
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTable()
            .andDocument {
                summary("Fetching tables")
                description(
                    """
                    A `GET` request provides information about a table.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table to retrieve."),
                )
                responseFields<TableRepresentation>(tableResponseFields())
                throws(TableNotFound::class)
            }

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
    fun findAll() {
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
            .andDocument {
                summary("Listing tables")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<tables-fetch,tables>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label of the table. (optional)").optional(),
                    parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)").optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this table. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").format("uuid").optional(),
                )
                pagedResponseFields<TableRepresentation>(tableResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
            .andDocument {
                summary("Creating tables")
                description(
                    """
                    A `POST` request creates a new table with all the given parameters.
                    The response will be `201 Created` when successful.
                    The table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created table can be fetched from."),
                )
                requestFields<CreateTableRequest>(
                    fieldWithPath("label").description("The label of the table."),
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process."),
                    fieldWithPath("resources.*.label").description("The label of the resource."),
                    fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process."),
                    fieldWithPath("literals.*.label").description("The value of the literal."),
                    fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process."),
                    fieldWithPath("predicates.*.label").description("The label of the predicate."),
                    fieldWithPath("predicates.*.description").description("The description of the predicate."),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process."),
                    fieldWithPath("lists.*.label").description("The label of the list."),
                    fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process."),
                    fieldWithPath("classes.*.label").description("The label of the class."),
                    fieldWithPath("classes.*.uri").description("The uri of the class."),
                    fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table and must only consist of string literals. Additionally, one data row is required. Every row must have the same length."),
                    fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
                    fieldWithPath("rows[].data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row."),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to."),
                    fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues."""),
                )
                throws(
                    InvalidLabel::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

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
            .andDocument {
                summary("Updating tables")
                description(
                    """
                    A `PUT` request updates an existing table with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. All fields at the top level in the request can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    2. The same rules as for <<resources-edit,updating resources>> apply when updating the visibility of a table.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<UpdateTableRequest>(
                    fieldWithPath("label").description("The label of the table. (optional)").optional(),
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process."),
                    fieldWithPath("resources.*.label").description("The label of the resource."),
                    fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process."),
                    fieldWithPath("literals.*.label").description("The value of the literal."),
                    fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process."),
                    fieldWithPath("predicates.*.label").description("The label of the predicate."),
                    fieldWithPath("predicates.*.description").description("The description of the predicate."),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process."),
                    fieldWithPath("lists.*.label").description("The label of the list."),
                    fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process."),
                    fieldWithPath("classes.*.label").description("The label of the class."),
                    fieldWithPath("classes.*.uri").description("The uri of the class."),
                    fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table and must only consist of string literals. Additionally, one data row is required. Every row must have the same length. (optional)").optional(),
                    fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
                    fieldWithPath("rows[].data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row."),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                    fieldWithPath("visibility").description("""The method used to extract the table resource. Can be one of $allowedVisibilityValues. (optional)""").optional(),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    InvalidLabel::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

        verify(exactly = 1) { tableService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row create request, when service succeeds, it creates the table row")
    fun createRow() {
        val id = ThingId("R123")
        every { tableService.createTableRow(any()) } returns ThingId("R456")

        post("/api/tables/{id}/rows", id)
            .content(createTableRowRequest())
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
    fun createRowAtIndex() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.createTableRow(any()) } returns ThingId("R456")

        documentedPostRequestTo("/api/tables/{id}/rows/{index}", id, index)
            .content(createTableRowRequest())
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Creating table rows")
                description(
                    """
                    A `POST` request creates a new table row with the given parameters.
                    The response will be `201 Created` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The insertion index the of the row. If not specified, the row will be appended at the end of the table. (optional)").optional(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<CreateTableRowRequest>(
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process.").optional(),
                    fieldWithPath("resources.*.label").type("String").description("The label of the resource.").optional(),
                    fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource.").optional(),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process.").optional(),
                    fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                    fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process.").optional(),
                    fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                    fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process.").optional(),
                    fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                    fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process.").optional(),
                    fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                    fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                    fieldWithPath("row").description("The table row. It must have the same length as the table header"),
                    fieldWithPath("row.label").description("The label of the row. (optional)").optional(),
                    fieldWithPath("row.data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    InvalidTableRowIndex::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    InvalidLabel::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

        verify(exactly = 1) { tableService.createTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row update request, when service succeeds, it updates the table row")
    fun updateRow() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.updateTableRow(any()) } just runs

        documentedPutRequestTo("/api/tables/{id}/rows/{index}", id, index)
            .content(updateTableRowRequest())
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Updating table rows")
                description(
                    """
                    A `PUT` request updates an existing row of a table with the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The index of the row."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<UpdateTableRowRequest>(
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process.").optional(),
                    fieldWithPath("resources.*.label").type("String").description("The label of the resource.").optional(),
                    fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource.").optional(),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process.").optional(),
                    fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                    fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process.").optional(),
                    fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                    fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process.").optional(),
                    fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                    fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process.").optional(),
                    fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                    fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                    fieldWithPath("row").description("The table row. It must have the same length as the table header"),
                    fieldWithPath("row.label").description("The label of the row. (optional)").optional(),
                    fieldWithPath("row.data[]").description("The ordered list of values (thing ids, temporary ids or `null`) of the row. (optional)").optional(),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    TableRowNotFound::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    InvalidLabel::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

        verify(exactly = 1) { tableService.updateTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table row delete request, when service succeeds, it deletes the table row")
    fun deleteRow() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.deleteTableRow(any()) } just runs

        documentedDeleteRequestTo("/api/tables/{id}/rows/{index}", id, index)
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Deleting table rows")
                description(
                    """
                    A `DELETE` request deletes an existing row of a table.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. The table header (first row) cannot be deleted.
                    2. There must at least be two rows left (including header) after deletion.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The index of the row."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    TooFewTableRows::class,
                    CannotDeleteTableHeader::class,
                    TableRowNotFound::class,
                )
            }

        verify(exactly = 1) { tableService.deleteTableRow(withArg { it.rowIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table column create request, when service succeeds, it creates the table column")
    fun createColumn() {
        val id = ThingId("R123")
        every { tableService.createTableColumn(any()) } returns ThingId("R456")

        post("/api/tables/{id}/columns", id)
            .content(tableColumnRequest())
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))

        verify(exactly = 1) { tableService.createTableColumn(withArg { it.columnIndex shouldBe null }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table column create request, when service succeeds, it creates the table column at the specified index")
    fun createColumnAtIndex() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.createTableColumn(any()) } returns ThingId("R456")

        documentedPostRequestTo("/api/tables/{id}/columns/{index}", id, index)
            .content(tableColumnRequest())
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Creating table columns")
                description(
                    """
                    A `POST` request creates a new table column with the given parameters.
                    The response will be `201 Created` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The insertion index the of the column. If not specified, the column will be appended at the end of the table. (optional)").optional(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<TableColumnRequest>(
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process.").optional(),
                    fieldWithPath("resources.*.label").type("String").description("The label of the resource.").optional(),
                    fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource.").optional(),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process.").optional(),
                    fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                    fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process.").optional(),
                    fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                    fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process.").optional(),
                    fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                    fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process.").optional(),
                    fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                    fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                    fieldWithPath("column[]").description("The ordered list of column values (thing ids, temporary ids or `null`). The first value always represents the header of the table and must be a string literal."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    InvalidTableColumnIndex::class,
                    MissingTableColumnValues::class,
                    TooManyTableColumnValues::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    InvalidLabel::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

        verify(exactly = 1) { tableService.createTableColumn(withArg { it.columnIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table column update request, when service succeeds, it updates the table column")
    fun updateColumn() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.updateTableColumn(any()) } just runs

        documentedPutRequestTo("/api/tables/{id}/columns/{index}", id, index)
            .content(tableColumnRequest())
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Updating table columns")
                description(
                    """
                    A `PUT` request updates an existing column of a table with the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The index of the column."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<TableColumnRequest>(
                    fieldWithPath("resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("resources.*").type("Object").description("Defines a single resource that needs to be created in the process.").optional(),
                    fieldWithPath("resources.*.label").type("String").description("The label of the resource.").optional(),
                    fieldWithPath("resources.*.classes").type("Array").description("The list of classes of the resource.").optional(),
                    fieldWithPath("literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").type("Object").description("Defines a single literal that needs to be created in the process.").optional(),
                    fieldWithPath("literals.*.label").type("String").description("The value of the literal.").optional(),
                    fieldWithPath("literals.*.data_type").type("String").description("The data type of the literal.").optional(),
                    fieldWithPath("predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("predicates.*").type("Object").description("Defines a single predicate that needs to be created in the process.").optional(),
                    fieldWithPath("predicates.*.label").type("String").description("The label of the predicate.").optional(),
                    fieldWithPath("predicates.*.description").type("String").description("The description of the predicate.").optional(),
                    fieldWithPath("lists").description("A map of temporary ids to list definitions for lists that need to be created (optional).").optional(),
                    fieldWithPath("lists.*").type("Object").description("Defines a single list that needs to be created in the process.").optional(),
                    fieldWithPath("lists.*.label").type("String").description("The label of the list.").optional(),
                    fieldWithPath("lists.*.elements").type("Array").description("The IDs of the elements of the list.").optional(),
                    fieldWithPath("classes").description("A map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
                    fieldWithPath("classes.*").type("Object").description("Defines a single class that needs to be created in the process.").optional(),
                    fieldWithPath("classes.*.label").type("String").description("The label of the class.").optional(),
                    fieldWithPath("classes.*.uri").type("String").description("The uri of the class.").optional(),
                    fieldWithPath("column[]").description("The ordered list of column values (thing ids, temporary ids or `null`). The first value always represents the header of the table and must be a string literal."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    TableColumnNotFound::class,
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    MissingTableRows::class,
                    MissingTableHeaderValue::class,
                    TooManyTableRowValues::class,
                    MissingTableRowValues::class,
                    InvalidLabel::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    TableHeaderValueMustBeLiteral::class,
                )
            }

        verify(exactly = 1) { tableService.updateTableColumn(withArg { it.columnIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table column delete request, when service succeeds, it deletes the table column")
    fun deleteColumn() {
        val id = ThingId("R123")
        val index = 5
        every { tableService.deleteTableColumn(any()) } just runs

        documentedDeleteRequestTo("/api/tables/{id}/columns/{index}", id, index)
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Deleting table columns")
                description(
                    """
                    A `DELETE` request deletes an existing column of a table.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("index").description("The index of the column."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    TooFewTableColumns::class,
                    TableColumnNotFound::class,
                )
            }

        verify(exactly = 1) { tableService.deleteTableColumn(withArg { it.columnIndex shouldBe index }) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a table cell update request, when service succeeds, it updates the table column")
    fun updateCell() {
        val id = ThingId("R123")
        val rowIndex = 5
        val columnIndex = 4
        every { tableService.updateTableCell(any()) } just runs

        documentedPutRequestTo("/api/tables/{id}/cells/{row}/{column}", id, rowIndex, columnIndex)
            .content(updateTableCellRequest())
            .accept(TABLE_CELL_JSON_V1)
            .contentType(TABLE_CELL_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/tables/$id")))
            .andDocument {
                summary("Updating table cells")
                description(
                    """
                    A `PUT` request updates an existing cell of a table with the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated table (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the table."),
                    parameterWithName("row").description("The index of the row."),
                    parameterWithName("column").description("The index of the column."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated table can be fetched from."),
                )
                requestFields<UpdateTableCellRequest>(
                    fieldWithPath("id").description("The id of the thing that should replace the current value of the cell. If `null`, the current value will be removed."),
                )
                throws(
                    TableNotFound::class,
                    TableNotModifiable::class,
                    TableColumnNotFound::class,
                    TableRowNotFound::class,
                    ThingNotFound::class,
                    TableHeaderValueMustBeLiteral::class,
                    CannotDeleteTableHeader::class,
                )
            }

        verify(exactly = 1) {
            tableService.updateTableCell(
                withArg {
                    it.tableId shouldBe id
                    it.rowIndex shouldBe rowIndex
                    it.columnIndex shouldBe columnIndex
                }
            )
        }
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
                CreateRowRequest(
                    label = "header",
                    data = listOf("#temp1", "#temp2", "#temp3")
                ),
                CreateRowRequest(
                    label = null,
                    data = listOf("R456", "#temp4", "#temp5")
                ),
                CreateRowRequest(
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
                CreateRowRequest(
                    label = "header",
                    data = listOf("#temp1", "#temp2", "#temp3")
                ),
                CreateRowRequest(
                    label = null,
                    data = listOf("R456", "#temp4", "#temp5")
                ),
                CreateRowRequest(
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

    private fun createTableRowRequest() =
        CreateTableRowRequest(
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
            row = CreateRowRequest(
                label = "row 3",
                data = listOf("R456", "#temp1", null)
            ),
        )

    private fun updateTableRowRequest() =
        UpdateTableRowRequest(
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
            row = UpdateRowRequest(
                label = "row 3",
                data = listOf("R456", "#temp1", null)
            ),
        )

    private fun tableColumnRequest() =
        TableColumnRequest(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to CreateLiteralRequestPart("column 1", Literals.XSD.STRING.prefixedUri),
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap(),
            column = listOf("#temp1", null, "R456"),
        )

    private fun updateTableCellRequest() =
        UpdateTableCellRequest(ThingId("R5646"))
}
