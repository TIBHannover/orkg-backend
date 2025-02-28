package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase.ReplaceCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createClassWithoutURI
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class ClassServiceUnitTest : MockkBaseTest {
    private val repository: ClassRepository = mockk()
    private val service = ClassService(repository, fixedClock)

    @Test
    fun `given a class is created, when no id is given, then it gets an id from the repository`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = null)) shouldBe mockClassId

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `given a class is created, when an id is given, then it does not get a new id`() {
        val mockClassId = ThingId("1")
        every { repository.findById(mockClassId) } returns Optional.empty()
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = mockClassId)) shouldBe mockClassId

        verify(exactly = 1) { repository.findById(mockClassId) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `given a class is created, when an already existing id is given, then an exception is thrown`() {
        val mockClassId = ThingId("1")
        every { repository.findById(mockClassId) } returns createClass(id = mockClassId).toOptional()

        assertThrows<ClassAlreadyExists> {
            service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = mockClassId))
        }

        verify(exactly = 1) { repository.findById(mockClassId) }
    }

    @Test
    fun `given a class is created, when a reserved id is given, then an exception is thrown`() {
        assertThrows<ClassNotAllowed> {
            service.create(CreateClassUseCase.CreateCommand(id = reservedClassIds.first(), label = "irrelevant"))
        }
    }

    @Test
    fun `given a class is created, when the label is invalid, then an exception is thrown`() {
        assertThrows<InvalidLabel> {
            service.create(CreateClassUseCase.CreateCommand(label = " \t "))
        }
    }

    @Test
    fun `given a class is created, when uri is not absolute, then an exception is thrown`() {
        assertThrows<URINotAbsolute> {
            service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", uri = ParsedIRI("invalid")))
        }
    }

    @Test
    fun `given a class is created, when an already existing uri is given, then an exception is thrown`() {
        val mockClass = createClass(uri = ParsedIRI("https://orkg.org/class/C1"))
        every { repository.findByUri(mockClass.uri.toString()) } returns mockClass.toOptional()

        assertThrows<URIAlreadyInUse> {
            service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", uri = mockClass.uri))
        }

        verify(exactly = 1) { repository.findByUri(mockClass.uri.toString()) }
    }

    @Test
    fun `given a class is created, when no contributor is given, the anonymous user id is used`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant")) shouldBe mockClassId

        verify(exactly = 1) {
            repository.save(
                Class(
                    id = mockClassId,
                    label = "irrelevant",
                    uri = null,
                    createdAt = OffsetDateTime.now(fixedClock),
                    createdBy = ContributorId(UUID(0, 0)),
                )
            )
        }
        verify(exactly = 1) { repository.nextIdentity() }
    }

    @Test
    fun `given a class is created, when a contributor is given, the contributor id is used`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        val randomContributorId = ContributorId(UUID.randomUUID())
        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", contributorId = randomContributorId))

        verify(exactly = 1) {
            repository.save(
                Class(
                    id = mockClassId,
                    label = "irrelevant",
                    uri = null,
                    createdAt = OffsetDateTime.now(fixedClock),
                    createdBy = randomContributorId,
                )
            )
        }
        verify(exactly = 1) { repository.nextIdentity() }
    }

    @Test
    fun `given a class does not exist, when updating the label, then it returns an appropriate error`() {
        val id = ThingId("non-existent")
        every { repository.findById(any()) } returns Optional.empty()

        assertThrows<ClassNotFound> { service.update(UpdateClassUseCase.UpdateCommand(id, label = "new label")) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is valid, it returns success`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } returns Unit

        service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, label = "new label"))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is invalid, it returns an appropriate error`() {
        assertThrows<InvalidLabel> {
            service.update(UpdateClassUseCase.UpdateCommand(ThingId("OK"), label = "some\ninvalid\nlabel"))
        }
    }

    @Test
    fun `given a class exists, when updating the label with the same text, it skips the action and returns success`() {
        val originalClass = createClass()
        val id = ThingId("OK")
        every { repository.findById(id) } returns Optional.of(originalClass)

        service.update(UpdateClassUseCase.UpdateCommand(id, label = "some label"))

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class is unmodifiable, when updating the label, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<ClassNotModifiable> {
            service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, label = "some label"))
        }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `given a class does not exist, when updating the URI, then it returns an appropriate error`() {
        val id = ThingId("non-existent")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ClassNotFound> {
            service.update(UpdateClassUseCase.UpdateCommand(id, uri = ParsedIRI("https://example.org/foo")))
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is not already used, it returns success`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = ParsedIRI("https://example.org/NEW"))
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, uri = ParsedIRI("https://example.org/NEW")))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = ParsedIRI("https://example.org/NEW"))
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        assertThrows<URIAlreadyInUse> {
            service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, uri = ParsedIRI("https://example.org/NEW")))
        }

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `given a class exists and has a URI, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass()
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<CannotResetURI> {
            service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, uri = ParsedIRI("https://example.org/DIFFERENT")))
        }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `given a class exists, when updating with no URI and no label, it does nothing`() {
        service.update(UpdateClassUseCase.UpdateCommand(ThingId("EXISTS")))
    }

    @Test
    fun `given a class is unmodifiable, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<ClassNotModifiable> {
            service.update(UpdateClassUseCase.UpdateCommand(originalClass.id, uri = ParsedIRI("https://example.com/DIFFERENT")))
        }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `given a class is replaced, when only valid inputs are provided, then updates and returns success`() {
        val originalClass = createClass()
        val replacingClass = createClass(label = "other label")
        val expectedClass = originalClass.copy(id = originalClass.id, label = replacingClass.label)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand())

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when an invalid label is provided, then returns an error`() {
        val replacingClass = createClass(label = "invalid\nlabel", uri = ParsedIRI("https://example.com/NEW"))

        assertThrows<InvalidLabel> { service.replace(replacingClass.toReplaceCommand()) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has a URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(id = classToReplace, label = "other label")
        val existingClass = createClass(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<CannotResetURI> { service.replace(replacingClass.toReplaceCommand()) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand())

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is not already used, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(uri = ParsedIRI("https://example.com/NEW"))
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand())

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(uri = ParsedIRI("https://example.com/NEW"))
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        assertThrows<URIAlreadyInUse> { service.replace(replacingClass.toReplaceCommand()) }

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has a different URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClass(id = classToReplace, label = "other label", uri = ParsedIRI("https://example.com/NEW"))
        val existingClass = createClass(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<CannotResetURI> { service.replace(replacingClass.toReplaceCommand()) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has no URI, then updates and returns success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand())

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when class is unmodifiable, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace, modifiable = false)
        val replacingClass = existingClass.copy(label = "other label")
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<ClassNotModifiable> { service.replace(replacingClass.toReplaceCommand()) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    private fun Class.toReplaceCommand(): ReplaceCommand = ReplaceCommand(id, label, uri)
}
