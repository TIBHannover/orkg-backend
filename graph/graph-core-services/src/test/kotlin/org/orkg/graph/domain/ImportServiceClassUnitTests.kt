package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.MockUserId

class ImportServiceClassUnitTests : ImportServiceUnitTests() {
    @Test
    fun `Given an import service, when importing a class by short form and ontology id, but ontology is not supported, then it throws an exception`() {
        every { externalClassService.supportsOntology(any()) } returns false

        assertThrows<ExternalClassNotFound> {
            service.importClassByShortForm(ContributorId(MockUserId.USER), "unknown", "irrelevant")
        }

        verify(exactly = 1) { externalClassService.supportsOntology("unknown") }
    }

    @Test
    fun `Given an import service, when importing a class by short form and ontology id, and external class could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val shortForm = "classId"

        every { externalClassService.supportsOntology(any()) } returns true
        every { externalClassService.findClassByShortForm(any(), any()) } returns null

        assertThrows<ExternalClassNotFound> {
            service.importClassByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        }

        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalClassService.findClassByShortForm(ontologyId, shortForm) }
    }

    @Test
    fun `Given an import service, when importing a class by short form and ontology id, and external class is found, but already exists in orkg, then it returns the existing class id`() {
        val ontologyId = "ontology"
        val shortForm = "classId"
        val externalThing = ExternalThing(
            uri = URI.create("https://example.org/classs/$ontologyId/$shortForm"),
            label = "class label",
            description = "class description"
        )
        val existingId = ThingId("existing")

        every { externalClassService.supportsOntology(any()) } returns true
        every { externalClassService.findClassByShortForm(any(), any()) } returns externalThing
        every { classService.findByURI(externalThing.uri) } returns Optional.of(createClass(existingId))

        val result = service.importClassByShortForm(ContributorId(MockUserId.USER), ontologyId, shortForm)
        result shouldBe existingId

        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalClassService.findClassByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) { classService.findByURI(externalThing.uri) }
    }

    @Test
    fun `Given an import service, when importing a class by short form and ontology id, and external class is found, and does not exist in orkg, then it creates a new class and returns its id`() {
        val ontologyId = "ontology"
        val shortForm = "classId"
        val externalThing = ExternalThing(
            uri = URI.create("https://example.org/classs/$ontologyId/$shortForm"),
            label = "class label",
            description = "class description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val classId = ThingId("newClassId")

        every { externalClassService.supportsOntology(any()) } returns true
        every { externalClassService.findClassByShortForm(any(), any()) } returns externalThing
        every { classService.findByURI(externalThing.uri) } returns Optional.empty()
        mockClassCreation(contributorId, externalThing, classId)

        val result = service.importClassByShortForm(contributorId, ontologyId, shortForm)
        result shouldBe classId

        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalClassService.findClassByShortForm(ontologyId, shortForm) }
        verify(exactly = 1) { classService.findByURI(externalThing.uri) }
        verifyClassCreation(contributorId, externalThing, classId)
    }

    @Test
    fun `Given an import service, when importing a class by uri and ontology id, and external class already exists in orkg, then it returns the id of the existing class`() {
        val ontologyId = "ontology"
        val uri = URI.create("https://example.org/classs/$ontologyId/classId")
        val existingId = ThingId("existing")

        every { classService.findByURI(uri) } returns Optional.of(createClass(existingId))

        val result = service.importClassByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        result shouldBe existingId

        verify(exactly = 1) { classService.findByURI(uri) }
    }

    @Test
    fun `Given an import service, when importing a class by uri and ontology id, and external class does not exist in orkg and ontology is not supported, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = URI.create("https://example.org/classs/$ontologyId/classId")

        every { classService.findByURI(uri) } returns Optional.empty()
        every { externalClassService.supportsOntology(any()) } returns false

        assertThrows<ExternalClassNotFound> {
            service.importClassByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) { classService.findByURI(uri) }
        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
    }

    @Test
    fun `Given an import service, when importing a class by uri and ontology id, and external class does not exist in orkg and external class could not be found, then it throws an exception`() {
        val ontologyId = "ontology"
        val uri = URI.create("https://example.org/classs/$ontologyId/classId")

        every { classService.findByURI(uri) } returns Optional.empty()
        every { externalClassService.supportsOntology(any()) } returns true
        every { externalClassService.findClassByURI(any(), any()) } returns null

        assertThrows<ExternalClassNotFound> {
            service.importClassByURI(ContributorId(MockUserId.USER), ontologyId, uri)
        }

        verify(exactly = 1) { classService.findByURI(uri) }
        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalClassService.findClassByURI(ontologyId, uri) }
    }

    @Test
    fun `Given an import service, when importing a class by uri and ontology id, and external class is found, and does not exist in orkg, then it creates a new class and returns its id`() {
        val ontologyId = "ontology"
        val uri = URI.create("https://example.org/classs/$ontologyId/classId")
        val externalThing = ExternalThing(
            uri = uri,
            label = "class label",
            description = "class description"
        )
        val contributorId = ContributorId(MockUserId.USER)
        val classId = ThingId("newClassId")

        every { classService.findByURI(uri) } returns Optional.empty()
        every { externalClassService.supportsOntology(any()) } returns true
        every { externalClassService.findClassByURI(any(), any()) } returns externalThing
        mockClassCreation(contributorId, externalThing, classId)

        service.importClassByURI(ContributorId(MockUserId.USER), ontologyId, uri)

        verify(exactly = 1) { classService.findByURI(uri) }
        verify(exactly = 1) { externalClassService.supportsOntology(ontologyId) }
        verify(exactly = 1) { externalClassService.findClassByURI(ontologyId, uri) }
        verifyClassCreation(contributorId, externalThing, classId)
    }

    private fun mockClassCreation(contributorId: ContributorId, externalThing: ExternalThing, classId: ThingId): ThingId {
        every {
            classService.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                    uri = externalThing.uri
                )
            )
        } returns classId
        mockDescriptionCreation(contributorId, externalThing, classId)
        return classId
    }

    private fun verifyClassCreation(contributorId: ContributorId, externalThing: ExternalThing, subjectId: ThingId) {
        verify(exactly = 1) {
            classService.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.label,
                    uri = externalThing.uri
                )
            )
        }
        verifyDescriptionCreation(contributorId, externalThing, subjectId)
    }
}
