package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf

class ImportServiceResourceUnitTest : AbstractImportServiceUnitTest() {
    @Test
    fun `Given an import service, when importing a resource by short form and ontology id, but ontology is not supported, then it throws an exception`() {
        every { externalResourceService.supportsOntology(any()) } returns false

        assertThrows<ExternalResourceNotFound> {
            service.importResourceByShortForm(ContributorId(MockUserId.USER), "unknown", "irrelevant")
        }

        verify(exactly = 1) { externalResourceService.supportsOntology("unknown") }
    }

    @Test
    fun `Given an import service, when importing a resource by short form and ontology id, and external resource could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val shortForm = "resourceId"

        every { externalResourceService.supportsOntology(any()) } returns true
        every { externalResourceService.findResourceByShortForm(any(), any()) } returns null

        assertThrows<ExternalResourceNotFound> {
            service.importResourceByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        }

        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalResourceService.findResourceByShortForm(ontologyId, shortForm) }
    }

    @Test
    fun `Given an import service, when importing a resource by short form and ontology id, and external resource is found, but already exists in orkg, then it returns the existing resource id`() {
        val ontologyId = "ontology"
        val shortForm = "resourceId"
        val externalThing = ExternalThing(
            uri = ParsedIRI("https://example.org/resources/$ontologyId/$shortForm"),
            label = "resource label",
            description = "resource description"
        )
        val existingId = ThingId("existing")

        every { externalResourceService.supportsOntology(any()) } returns true
        every { externalResourceService.findResourceByShortForm(any(), any()) } returns externalThing
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf(
            createStatement(subject = createResource(existingId))
        )

        val result = service.importResourceByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        result shouldBe existingId

        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalResourceService.findResourceByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given an import service, when importing a resource by short form and ontology id, and external resource is found, and does not exist in orkg, then it creates a new resource and returns its id`() {
        val ontologyId = "ontology"
        val shortForm = "resourceId"
        val externalThing = ExternalThing(
            uri = ParsedIRI("https://example.org/resources/$ontologyId/$shortForm"),
            label = "resource label",
            description = "resource description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val resourceId = ThingId("newResourceId")

        every { externalResourceService.supportsOntology(any()) } returns true
        every { externalResourceService.findResourceByShortForm(any(), any()) } returns externalThing
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        mockResourceCreation(contributorId, externalThing, resourceId)

        val result = service.importResourceByShortForm(contributorId, ontologyId, shortForm)
        result shouldBe resourceId

        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalResourceService.findResourceByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verifyResourceCreation(contributorId, externalThing, resourceId)
    }

    @Test
    fun `Given an import service, when importing a resource by uri and ontology id, and external resource already exists in orkg, then it returns the id of the existing resource`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/resources/$ontologyId/resourceId")
        val existingId = ThingId("existing")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf(
            createStatement(subject = createResource(existingId))
        )

        val result = service.importResourceByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        result shouldBe existingId

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given an import service, when importing a resource by uri and ontology id, and external resource does not exist in orkg and ontology is not supported, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/resources/$ontologyId/resourceId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalResourceService.supportsOntology(any()) } returns false

        assertThrows<ExternalResourceNotFound> {
            service.importResourceByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
    }

    @Test
    fun `Given an import service, when importing a resource by uri and ontology id, and external resource does not exist in orkg and external resource could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/resources/$ontologyId/resourceId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalResourceService.supportsOntology(any()) } returns true
        every { externalResourceService.findResourceByURI(any(), any()) } returns null

        assertThrows<ExternalResourceNotFound> {
            service.importResourceByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalResourceService.findResourceByURI(ontologyId, uri) }
    }

    @Test
    fun `Given an import service, when importing a resource by uri and ontology id, and external resource is found, and does not exist in orkg, then it creates a new resource and returns its id`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/resources/$ontologyId/resourceId")
        val externalThing = ExternalThing(
            uri = uri,
            label = "resource label",
            description = "resource description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val resourceId = ThingId("newResourceId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalResourceService.supportsOntology(any()) } returns true
        every { externalResourceService.findResourceByURI(any(), any()) } returns externalThing
        mockResourceCreation(contributorId, externalThing, resourceId)

        service.importResourceByURI(ContributorId(MockUserId.USER), ontologyId, uri)

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.resource),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalResourceService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalResourceService.findResourceByURI(ontologyId, uri) }
        verifyResourceCreation(contributorId, externalThing, resourceId)
    }

    private fun mockResourceCreation(contributorId: ContributorId, externalThing: ExternalThing, resourceId: ThingId): ThingId {
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                )
            )
        } returns resourceId
        mockDescriptionCreation(contributorId, externalThing, resourceId)
        mockSameAsLiteralCreation(contributorId, externalThing, resourceId)
        return resourceId
    }

    private fun verifyResourceCreation(contributorId: ContributorId, externalThing: ExternalThing, subjectId: ThingId) {
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                )
            )
        }
        verifyDescriptionCreation(contributorId, externalThing, subjectId)
        verifySameAsLiteralCreation(contributorId, externalThing, subjectId)
    }
}
