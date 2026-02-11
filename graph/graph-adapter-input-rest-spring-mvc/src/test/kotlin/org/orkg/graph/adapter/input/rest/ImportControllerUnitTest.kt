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
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.graph.adapter.input.rest.ImportController.ImportByShortFormRequest
import org.orkg.graph.adapter.input.rest.ImportController.ImportByURIRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.domain.ExternalClassNotFound
import org.orkg.graph.domain.ExternalEntityIsNotAClass
import org.orkg.graph.domain.ExternalEntityIsNotAResource
import org.orkg.graph.domain.ExternalPredicateNotFound
import org.orkg.graph.domain.ExternalResourceNotFound
import org.orkg.graph.input.ImportUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ImportController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ImportController::class])
internal class ImportControllerUnitTest : MockMvcBaseTest("import") {
    @MockkBean
    private lateinit var service: ImportUseCases

    @Test
    @TestWithMockUser
    @DisplayName("Given an import by uri request, when trying to import a resource and service succeeds, then status is 201 CREATED and location header is returned")
    fun importResourceByURI() {
        val id = ThingId("R123")
        val request = ImportByURIRequest(
            uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
            ontology = "wikidata"
        )

        every { service.importResourceByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/resources")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/resources/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing resources by URI")
                description(
                    """
                    A `POST` request imports a resource from an external ontology by a given URI.
                    The response will be `201 Created` when successful, even when the resource was already imported previously.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported resource can be fetched from.")
                )
                requestFields<ImportByURIRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("uri").description("The uri of the resource to import."),
                )
                throws(ExternalResourceNotFound::class, ServiceUnavailable::class, ExternalEntityIsNotAResource::class)
            }

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
        val request = ImportByShortFormRequest(
            shortForm = "Q42",
            ontology = "wikidata"
        )

        every { service.importResourceByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/resources")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/resources/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing resources by short form")
                description(
                    """
                    A `POST` request imports a resource from an external ontology by a given short form id.
                    The response will be `201 Created` when successful, even when the resource was already imported.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported resource can be fetched from.")
                )
                requestFields<ImportByShortFormRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("short_form").description("The short form id of the resource to import."),
                )
                throws(ExternalResourceNotFound::class, ServiceUnavailable::class, ExternalEntityIsNotAResource::class)
            }

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
        val request = ImportByURIRequest(
            uri = ParsedIRI.create("https://www.wikidata.org/entity/P30"),
            ontology = "wikidata"
        )

        every { service.importPredicateByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/predicates")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/predicates/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing predicates by URI")
                description(
                    """
                    A `POST` request imports a predicate from an external ontology by a given URI.
                    The response will be `201 Created` when successful, even when the predicate was already imported previously.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported predicate can be fetched from.")
                )
                requestFields<ImportByURIRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("uri").description("The uri of the predicate to import."),
                )
                throws(ExternalPredicateNotFound::class, ServiceUnavailable::class)
            }

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
        val request = ImportByShortFormRequest(
            shortForm = "P30",
            ontology = "wikidata"
        )

        every { service.importPredicateByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/predicates")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/predicates/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing predicates by short form")
                description(
                    """
                    A `POST` request imports a predicate from an external ontology by a given short form id.
                    The response will be `201 Created` when successful, even when the predicate was already imported previously.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported predicate can be fetched from.")
                )
                requestFields<ImportByShortFormRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("short_form").description("The short form id of the resource to import."),
                )
                throws(ExternalPredicateNotFound::class, ServiceUnavailable::class)
            }

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
        val request = ImportByURIRequest(
            uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
            ontology = "wikidata"
        )

        every { service.importClassByURI(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/classes")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing classes by URI")
                description(
                    """
                    A `POST` request imports a class from an external ontology by a given URI.
                    The response will be `201 Created` when successful, even when the class was already imported.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported class can be fetched from.")
                )
                requestFields<ImportByURIRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("uri").description("The uri of the class to import."),
                )
                throws(ExternalClassNotFound::class, ServiceUnavailable::class, ExternalEntityIsNotAClass::class)
            }

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
        val request = ImportByShortFormRequest(
            shortForm = "Q42",
            ontology = "wikidata"
        )

        every { service.importClassByShortForm(any(), any(), any()) } returns id

        documentedPostRequestTo("/api/import/classes")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDocument {
                tag("Entity Import")
                summary("Importing classes by short form")
                description(
                    """
                    A `POST` request imports a class from an external ontology by a given short form id.
                    The response will be `201 Created` when successful, even when the class was already imported.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the imported class can be fetched from.")
                )
                requestFields<ImportByShortFormRequest>(
                    fieldWithPath("ontology").description("The identifier of the ontology. See <<external-sources,External Sources>> for more information."),
                    fieldWithPath("short_form").description("The short form id of the resource to import."),
                )
                throws(ExternalClassNotFound::class, ServiceUnavailable::class, ExternalEntityIsNotAClass::class)
            }

        verify(exactly = 1) {
            service.importClassByShortForm(
                contributorId = ContributorId(MockUserId.USER),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
    }
}
