package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.CoreMatchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.input.ImportUseCases
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ImportController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ImportController::class])
internal class ImportControllerUnitTest : RestDocsTest("import") {

    @MockkBean
    private lateinit var service: ImportUseCases

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by uri request, when trying to import a resource and service succeeds, then status is 201 CREATED and location header is returned")
    fun importResourceByURI() {
        val id = ThingId("R123")
        val request = ImportController.ImportByURIRequest(
            uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
            ontology = "wikidata"
        )

        every { service.importResourceByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/resources")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("uri").description("The uri of the resource to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importResourceByURI(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                uri = request.uri
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by short form request, when trying to import a resource and service succeeds, then status is 201 CREATED and location header is returned")
    fun importResourceByShortForm() {
        val id = ThingId("R123")
        val request = ImportController.ImportByShortFormRequest(
            shortForm = "Q42",
            ontology = "wikidata"
        )

        every { service.importResourceByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/resources")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("short_form").description("The short form id of the resource to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importResourceByShortForm(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by uri request, when trying to import a predicate and service succeeds, then status is 201 CREATED and location header is returned")
    fun importPredicateByURI() {
        val id = ThingId("P123")
        val request = ImportController.ImportByURIRequest(
            uri = ParsedIRI("https://www.wikidata.org/entity/P30"),
            ontology = "wikidata"
        )

        every { service.importPredicateByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/predicates")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/predicates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported predicate can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("uri").description("The uri of the predicate to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importPredicateByURI(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                uri = request.uri
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by short form request, when trying to import a predicate and service succeeds, then status is 201 CREATED and location header is returned")
    fun importPredicateByShortForm() {
        val id = ThingId("P123")
        val request = ImportController.ImportByShortFormRequest(
            shortForm = "P30",
            ontology = "wikidata"
        )

        every { service.importPredicateByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/predicates")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/predicates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported predicate can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("short_form").description("The short form id of the resource to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importPredicateByShortForm(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by uri request, when trying to import a class and service succeeds, then status is 201 CREATED and location header is returned")
    fun importClassByURI() {
        val id = ThingId("C123")
        val request = ImportController.ImportByURIRequest(
            uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
            ontology = "wikidata"
        )

        every { service.importClassByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/classes")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported class can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("uri").description("The uri of the class to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importClassByURI(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                uri = request.uri
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by short form request, when trying to import a class and service succeeds, then status is 201 CREATED and location header is returned")
    fun importClassByShortForm() {
        val id = ThingId("C123")
        val request = ImportController.ImportByShortFormRequest(
            shortForm = "Q42",
            ontology = "wikidata"
        )

        every { service.importClassByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/classes")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the imported class can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                        fieldWithPath("short_form").description("The short form id of the resource to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            service.importClassByShortForm(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
    }
}
