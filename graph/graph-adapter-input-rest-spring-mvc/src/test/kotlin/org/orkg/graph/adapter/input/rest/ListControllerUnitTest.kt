package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.ListController.CreateListRequest
import org.orkg.graph.adapter.input.rest.ListController.UpdateListRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.thingResponseFields
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.ListElementNotFound
import org.orkg.graph.domain.ListInUse
import org.orkg.graph.domain.ListNotFound
import org.orkg.graph.domain.ListNotModifiable
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createList
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.Optional

@ContextConfiguration(classes = [ListController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ListController::class])
internal class ListControllerUnitTest : MockMvcBaseTest("lists") {
    @MockkBean
    private lateinit var listService: ListUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a list, when fetched by id and service succeeds, then status is 200 OK and list is returned")
    fun findById() {
        val id = ThingId("R123")
        val list = createList(id = id, createdAt = OffsetDateTime.parse("2023-06-01T15:19:04.778631092+02:00"))

        every { listService.findById(id) } returns Optional.of(list)

        documentedGetRequestTo("/api/lists/{id}", id)
            .perform()
            .andExpect(status().isOk)
            // Explicitly test all properties of the representation. This works as serialization test.
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.label").value(list.label))
            .andExpect(jsonPath("$.elements").isArray)
            .andExpect(jsonPath("$.created_at").value("2023-06-01T15:19:04.778631092+02:00"))
            .andExpect(jsonPath("$.created_by").value(list.createdBy.value.toString()))
            .andExpect(jsonPath("$.modifiable").value(list.modifiable.toString()))
            .andExpect(jsonPath("$._class").value("list"))
            // Document the representation for later reference.
            .andDocument {
                summary("Fetching lists")
                description(
                    """
                    A `GET` request provides information about a list.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the list.")
                )
                responseFields<ListRepresentation>(
                    // The order here determines the order in the generated table. More relevant items should be up.
                    fieldWithPath("id").description("The identifier of the list."),
                    fieldWithPath("label").description("The label of the list."),
                    fieldWithPath("elements[]").description("The ids of the elements of the list."),
                    timestampFieldWithPath("created_at", "the list was created"),
                    // TODO: Add links to documentation of special user UUIDs.
                    fieldWithPath("created_by").description("The UUID of the user or service who created this list."),
                    fieldWithPath("modifiable").description("Whether this list can be modified."),
                    fieldWithPath("_class").description("The type of object this json contains. Always has the value \"list\"."),
                )
                throws(ListNotFound::class)
            }

        verify(exactly = 1) { listService.findById(id) }
    }

    @Test
    fun `Given a list, when fetched by id and service reports missing list, then status is 404 NOT FOUND`() {
        val id = ThingId("NOT_FOUND")

        every { listService.findById(id) } returns Optional.empty()

        get("/api/lists/{id}", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:list_not_found")

        verify(exactly = 1) { listService.findById(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When creating a list and service succeeds, then status is 201 CREATED")
    fun create() {
        val id = ThingId("R123")
        val list = createList(
            id = id,
            label = "List label",
            elements = listOf(ThingId("R1"))
        )
        val request = mapOf(
            "label" to list.label,
            "elements" to list.elements
        )

        every { listService.create(any()) } returns id

        documentedPostRequestTo("/api/lists")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/lists/$id")))
            .andDocument {
                summary("Creating lists")
                description(
                    """
                    A `POST` request creates a new list with all the given parameters.
                    The response will be `201 Created` when successful.
                    The list resource (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created list can be fetched from.")
                )
                requestFields<CreateListRequest>(
                    fieldWithPath("label").description("The label of the list."),
                    fieldWithPath("elements[]").description("The ids of the elements of the list.")
                )
                throws(InvalidLabel::class, ThingAlreadyExists::class, ListElementNotFound::class)
            }

        verify(exactly = 1) { listService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `When creating a list and service reports invalid label, then status is 400 BAD REQUEST`() {
        val id = ThingId("List1")
        val request = mapOf(
            "label" to "List label",
            "elements" to listOf(ThingId("R1"))
        )
        val exception = InvalidLabel()

        every { listService.create(any()) } throws exception

        post("/api/lists")
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_label")

        verify(exactly = 1) { listService.create(any()) }
        verify(exactly = 0) { listService.findById(id) }
    }

    @Test
    @TestWithMockUser
    fun `When creating a list and service reports list element not found, then status is 400 BAD REQUEST`() {
        val id = ThingId("List1")
        val request = mapOf(
            "label" to "List label",
            "elements" to listOf(ThingId("R1"))
        )
        val exception = ListElementNotFound()

        every { listService.create(any()) } throws exception

        post("/api/lists")
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:list_element_not_found")

        verify(exactly = 1) { listService.create(any()) }
        verify(exactly = 0) { listService.findById(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When updating a list and service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = ThingId("List1")
        val list = createList(
            id = id,
            label = "List label",
            elements = listOf(ThingId("R1"))
        )
        val request = mapOf(
            "label" to list.label,
            "elements" to list.elements
        )

        every { listService.update(any()) } just runs

        documentedPatchRequestTo("/api/lists/{id}", id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/lists/$id")))
            .andDocument {
                summary("Updating lists")
                description(
                    """
                    A `PATCH` request updates a list with all the given parameters.
                    The response will be `204 No Content` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the list.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated list can be fetched from.")
                )
                requestFields<UpdateListRequest>(
                    fieldWithPath("label").description("The new label of the list. (optional)").optional(),
                    fieldWithPath("elements[]").description("The new ids of the elements of the list. (optional)").optional()
                )
                throws(ListNotFound::class, ListNotModifiable::class, InvalidLabel::class, ListElementNotFound::class)
            }

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `When updating a list and service reports invalid label, then status is 400 BAD REQUEST`() {
        val id = ThingId("List1")
        val request = mapOf(
            "label" to "List label",
            "elements" to listOf(ThingId("R1"))
        )
        val exception = InvalidLabel()

        every { listService.update(any()) } throws exception

        patch("/api/lists/{id}", id)
            .contentType(APPLICATION_JSON)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_label")

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `When updating a list and service reports list element not found, then status is 400 BAD REQUEST`() {
        val id = ThingId("List1")
        val request = mapOf(
            "label" to "List label",
            "elements" to listOf(ThingId("R1"))
        )
        val exception = ListElementNotFound()

        every { listService.update(any()) } throws exception

        patch("/api/lists/{id}", id)
            .contentType(APPLICATION_JSON)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:list_element_not_found")

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @DisplayName("Given a list, when fetching its elements and service succeeds, then status is 200 OK and elements are returned")
    fun findAllElementsById() {
        val id = ThingId("R123")
        val elements = listOf(
            createResource(),
            createLiteral(),
            createClass(),
            createPredicate()
        )

        every { listService.findAllElementsById(id, any()) } returns PageImpl(elements)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/lists/{id}/elements", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("content", Matchers.hasSize<Int>(elements.size)))
            .andExpect(jsonPath("content[0].id").value(elements[0].id.value))
            .andExpect(jsonPath("content[1].id").value(elements[1].id.value))
            .andExpect(jsonPath("content[2].id").value(elements[2].id.value))
            .andExpect(jsonPath("content[3].id").value(elements[3].id.value))
            .andExpect(jsonPath("page.total_elements").value(elements.size))
            // Document the representation for later reference.
            .andDocument {
                summary("Fetching list elements")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of elements, in order, with their full representations (see <<resources,resources>>, <<classes,classes>>, <<predicates,predicates>>, <<literals,literals>>), that are part of the list.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the list.")
                )
                pagedQueryParameters()
                pagedResponseFields<ThingRepresentation>(thingResponseFields())
                throws(ListNotModifiable::class, ListInUse::class)
            }

        verify(exactly = 1) {
            listService.findAllElementsById(id, any())
            statementService.countAllIncomingStatementsById(any<Set<ThingId>>())
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    fun `Given a list, when fetching its elements and service reports missing list, then status is 404 NOT FOUND`() {
        val id = ThingId("EXISTS")
        val exception = ListNotFound(id)

        every { listService.findAllElementsById(id, any()) } throws exception

        get("/api/lists/{id}/elements", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:list_not_found")

        verify(exactly = 1) { listService.findAllElementsById(id, any()) }
    }
}
