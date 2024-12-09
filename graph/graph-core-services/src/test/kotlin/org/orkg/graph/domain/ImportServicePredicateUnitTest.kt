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
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf

internal class ImportServicePredicateUnitTest : AbstractImportServiceUnitTest() {
    @Test
    fun `Given an import service, when importing a predicate by short form and ontology id, but ontology is not supported, then it throws an exception`() {
        every { externalPredicateService.supportsOntology(any()) } returns false

        assertThrows<ExternalPredicateNotFound> {
            service.importPredicateByShortForm(ContributorId(MockUserId.USER), "unknown", "irrelevant")
        }

        verify(exactly = 1) { externalPredicateService.supportsOntology("unknown") }
    }

    @Test
    fun `Given an import service, when importing a predicate by short form and ontology id, and external predicate could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val shortForm = "predicateId"

        every { externalPredicateService.supportsOntology(any()) } returns true
        every { externalPredicateService.findPredicateByShortForm(any(), any()) } returns null

        assertThrows<ExternalPredicateNotFound> {
            service.importPredicateByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        }

        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalPredicateService.findPredicateByShortForm(ontologyId, shortForm) }
    }

    @Test
    fun `Given an import service, when importing a predicate by short form and ontology id, and external predicate is found, but already exists in orkg, then it returns the existing predicate id`() {
        val ontologyId = "ontology"
        val shortForm = "predicateId"
        val externalThing = ExternalThing(
            uri = ParsedIRI("https://example.org/predicates/$ontologyId/$shortForm"),
            label = "predicate label",
            description = "predicate description"
        )
        val existingId = ThingId("existing")

        every { externalPredicateService.supportsOntology(any()) } returns true
        every { externalPredicateService.findPredicateByShortForm(any(), any()) } returns externalThing
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf(
            createStatement(subject = createPredicate(existingId))
        )

        val result = service.importPredicateByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        result shouldBe existingId

        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalPredicateService.findPredicateByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given an import service, when importing a predicate by short form and ontology id, and external predicate is found, and does not exist in orkg, then it creates a new predicate and returns its id`() {
        val ontologyId = "ontology"
        val shortForm = "predicateId"
        val externalThing = ExternalThing(
            uri = ParsedIRI("https://example.org/predicates/$ontologyId/$shortForm"),
            label = "predicate label",
            description = "predicate description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val predicateId = ThingId("newPredicateId")

        every { externalPredicateService.supportsOntology(any()) } returns true
        every { externalPredicateService.findPredicateByShortForm(any(), any()) } returns externalThing
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        mockPredicateCreation(contributorId, externalThing, predicateId)

        val result = service.importPredicateByShortForm(contributorId, ontologyId, shortForm)
        result shouldBe predicateId

        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalPredicateService.findPredicateByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = externalThing.uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verifyPredicateCreation(contributorId, externalThing, predicateId)
    }

    @Test
    fun `Given an import service, when importing a predicate by uri and ontology id, and external predicate already exists in orkg, then it returns the id of the existing predicate`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/predicates/$ontologyId/predicateId")
        val existingId = ThingId("existing")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf(
            createStatement(subject = createPredicate(existingId))
        )

        val result = service.importPredicateByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        result shouldBe existingId

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given an import service, when importing a predicate by uri and ontology id, and external predicate does not exist in orkg and ontology is not supported, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/predicates/$ontologyId/predicateId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalPredicateService.supportsOntology(any()) } returns false

        assertThrows<ExternalPredicateNotFound> {
            service.importPredicateByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
    }

    @Test
    fun `Given an import service, when importing a predicate by uri and ontology id, and external predicate does not exist in orkg and external predicate could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/predicates/$ontologyId/predicateId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalPredicateService.supportsOntology(any()) } returns true
        every { externalPredicateService.findPredicateByURI(any(), any()) } returns null

        assertThrows<ExternalPredicateNotFound> {
            service.importPredicateByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalPredicateService.findPredicateByURI(ontologyId, uri) }
    }

    @Test
    fun `Given an import service, when importing a predicate by uri and ontology id, and external predicate is found, and does not exist in orkg, then it creates a new predicate and returns its id`() {
        val ontologyId = "ontology"
        val uri = ParsedIRI("https://example.org/predicates/$ontologyId/predicateId")
        val externalThing = ExternalThing(
            uri = uri,
            label = "predicate label",
            description = "predicate description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val predicateId = ThingId("newPredicateId")

        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()
        every { externalPredicateService.supportsOntology(any()) } returns true
        every { externalPredicateService.findPredicateByURI(any(), any()) } returns externalThing
        mockPredicateCreation(contributorId, externalThing, predicateId)

        service.importPredicateByURI(ContributorId(MockUserId.USER), ontologyId, uri)

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectClasses = setOf(Classes.predicate),
                predicateId = Predicates.sameAs,
                objectLabel = uri.toString(),
                objectClasses = setOf(Classes.literal)
            )
        }
        verify(exactly = 1) { externalPredicateService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalPredicateService.findPredicateByURI(ontologyId, uri) }
        verifyPredicateCreation(contributorId, externalThing, predicateId)
    }

    private fun mockPredicateCreation(contributorId: ContributorId, externalThing: ExternalThing, predicateId: ThingId): ThingId {
        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                )
            )
        } returns predicateId
        mockDescriptionCreation(contributorId, externalThing, predicateId)
        mockSameAsLiteralCreation(contributorId, externalThing, predicateId)
        return predicateId
    }

    private fun verifyPredicateCreation(contributorId: ContributorId, externalThing: ExternalThing, subjectId: ThingId) {
        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                )
            )
        }
        verifyDescriptionCreation(contributorId, externalThing, subjectId)
        verifySameAsLiteralCreation(contributorId, externalThing, subjectId)
    }
}
