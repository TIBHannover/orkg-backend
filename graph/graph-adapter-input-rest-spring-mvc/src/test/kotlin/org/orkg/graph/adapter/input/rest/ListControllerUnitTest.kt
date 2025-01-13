package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.ListElementNotFound
import org.orkg.graph.domain.ListNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createList
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ListController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [ListController::class])
internal class ListControllerUnitTest : RestDocsTest("lists") {

    @MockkBean
    private lateinit var listService: ListUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a list, when fetched by id and service succeeds, then status is 200 OK and list is returned")
    fun getSingle() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(parameterWithName("id").description("The identifier of the literal to retrieve.")),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the list."),
                        fieldWithPath("label").description("The label of the list."),
                        fieldWithPath("elements").description("The ids of the elements of the list."),
                        timestampFieldWithPath("created_at", "the list was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this list."),
                        fieldWithPath("modifiable").description("Whether this list can be modified."),
                        fieldWithPath("_class").description("The type of object this json contains. Always has the value \"list\".")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { listService.findById(id) }
    }

    @Test
    fun `Given a list, when fetched by id and service reports missing list, then status is 404 NOT FOUND`() {
        val id = ThingId("NOT_FOUND")
        val exception = ListNotFound(id)

        every { listService.findById(id) } returns Optional.empty()

        get("/api/lists/{id}", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists/$id"))

        verify(exactly = 1) { listService.findById(id) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
        every { listService.findById(any()) } returns Optional.of(list)

        documentedPostRequestTo("/api/lists")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/lists/$id")))
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("label").description("The label of the list."),
                        fieldWithPath("elements").description("The ids of the elements of the list.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { listService.create(any()) }
        verify(exactly = 1) { listService.findById(id) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value(exception.property))
            .andExpect(jsonPath("$.errors[0].message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists"))

        verify(exactly = 1) { listService.create(any()) }
        verify(exactly = 0) { listService.findById(id) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value(exception.property))
            .andExpect(jsonPath("$.errors[0].message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists"))

        verify(exactly = 1) { listService.create(any()) }
        verify(exactly = 0) { listService.findById(id) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("label").description("The new label of the list. (optional)").optional(),
                        fieldWithPath("elements").description("The new ids of the elements of the list. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
            .characterEncoding(Charsets.UTF_8.name())
            .content(request)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value(exception.property))
            .andExpect(jsonPath("$.errors[0].message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists/$id"))

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @WithMockUser(username = "b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
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
            .characterEncoding(Charsets.UTF_8.name())
            .content(request)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value(exception.property))
            .andExpect(jsonPath("$.errors[0].message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists/$id"))

        verify(exactly = 1) { listService.update(any()) }
    }

    @Test
    @DisplayName("Given a list, when fetching its elements and service succeeds, then status is 200 OK and elements are returned")
    fun getElements() {
        val id = ThingId("R123")
        val elements = listOf(
            createResource(),
            createLiteral(),
            createClass(),
            createPredicate()
        )

        every { listService.findAllElementsById(id, any()) } returns PageImpl(elements)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        documentedGetRequestTo("/api/lists/{id}/elements", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("content", Matchers.hasSize<Int>(elements.size)))
            .andExpect(jsonPath("totalElements").value(elements.size))
            .andExpect(jsonPath("content[0].id").value(elements[0].id.value))
            .andExpect(jsonPath("content[1].id").value(elements[1].id.value))
            .andExpect(jsonPath("content[2].id").value(elements[2].id.value))
            .andExpect(jsonPath("content[3].id").value(elements[3].id.value))
            // Document the representation for later reference.
            .andDo(
                documentationHandler.document(
                    pathParameters(parameterWithName("id").description("The identifier of the literal to retrieve."))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            listService.findAllElementsById(id, any())
            statementService.countIncomingStatements(any<Set<ThingId>>())
            statementService.findAllDescriptions(any())
        }
    }

    @Test
    fun `Given a list, when fetching its elements and service reports missing list, then status is 404 NOT FOUND`() {
        val id = ThingId("EXISTS")
        val exception = ListNotFound(id)

        every { listService.findAllElementsById(id, any()) } throws exception

        get("/api/lists/{id}/elements", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/lists/$id/elements"))

        verify(exactly = 1) { listService.findAllElementsById(id, any()) }
    }
}
