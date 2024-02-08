package org.orkg.graph.domain

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.AlreadyInUse
import org.orkg.graph.input.ClassNotFound
import org.orkg.graph.input.ClassNotModifiableProblem
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.InvalidLabel
import org.orkg.graph.input.InvalidURI
import org.orkg.graph.input.UpdateClassUseCase.ReplaceCommand
import org.orkg.graph.input.UpdateNotAllowed
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createClassWithoutURI
import org.orkg.testing.fixedClock

class ClassServiceUnitTests {

    private val repository: ClassRepository = mockk()
    private val service = ClassService(repository, fixedClock)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(repository)
    }

    @Test
    fun `given a class is created, when no id is given, then it gets an id from the repository`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = null))

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `given a class is created, when an id is given, then it does not get a new id`() {
        val mockClassId = ThingId("1")
        every { repository.findById(mockClassId) } returns Optional.empty()
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = mockClassId))

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
        assertThrows<org.orkg.graph.domain.InvalidLabel> {
            service.create(CreateClassUseCase.CreateCommand(label = " \t "))
        }
    }

    @Test
    fun `given a class is created, when an already existing uri is given, then an exception is thrown`() {
        val mockClass = createClass(uri = URI.create("https://orkg.org/class/C1"))
        every { repository.findByUri(mockClass.uri.toString()) } returns mockClass.toOptional()

        assertThrows<DuplicateURI> {
            service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", uri = mockClass.uri))
        }

        verify(exactly = 1) { repository.findByUri(mockClass.uri.toString()) }
    }

    @Test
    fun `given a class is created, when no contributor is given, the anonymous user id is used`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant"))

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

        val actual = service.updateLabel(id, "new label")
        assertThat(actual).isEqualTo(Failure(ClassNotFound))

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is valid, it returns success`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } returns Unit

        val actual = service.updateLabel(originalClass.id, "new label")
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is invalid, it returns an appropriate error`() {
        val actual = service.updateLabel(ThingId("OK"), "some\ninvalid\nlabel")
        assertThat(actual).isEqualTo(Failure(InvalidLabel))
    }

    @Test
    fun `given a class exists, when updating the label with the same text, it skips the action and returns success`() {
        val originalClass = createClass()
        val id = ThingId("OK")
        every { repository.findById(id) } returns Optional.of(originalClass)

        val actual = service.updateLabel(id, "some label")
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class is unmodifiable, when updating the label, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        val actual = service.updateLabel(originalClass.id, "some label")
        assertThat(actual).isEqualTo(Failure(ClassNotModifiableProblem))

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `given a class does not exist, when updating the URI, then it returns an appropriate error`() {
        val id = ThingId("non-existent")
        every { repository.findById(id) } returns Optional.empty()

        val actual = service.updateURI(id, "https://example.org/foo")
        assertThat(actual).isEqualTo(Failure(ClassNotFound))

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is not already used, it returns success`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.updateURI(originalClass.id, "https://example.org/NEW")
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        val actual = service.updateURI(originalClass.id, "https://example.org/NEW")
        assertThat(actual).isEqualTo(Failure(AlreadyInUse))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is invalid, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val actual = service.updateURI(originalClass.id, "\n")
        assertThat(actual).isEqualTo(Failure(InvalidURI))
    }

    @Test
    fun `given a class exists and has a URI, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass()
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        val actual = service.updateURI(originalClass.id, "https://example.com/DIFFERENT")
        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `given a class is unmodifiable, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        val actual = service.updateURI(originalClass.id, "https://example.com/DIFFERENT")
        assertThat(actual).isEqualTo(Failure(ClassNotModifiableProblem))

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

        val actual = service.replace(originalClass.id, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when an invalid label is provided, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClass(label = "invalid\nlabel", uri = URI.create("https://example.com/NEW"))

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Failure(InvalidLabel))
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has a URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(label = "other label")
        val existingClass = createClass(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace)
        val replacingClass = createClass(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is not already used, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())
        assertThat(actual).isEqualTo(Failure(AlreadyInUse))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has a different URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass =
            createClass(label = "other label", uri = URI.create("https://example.com/NEW")).toReplaceCommand()
        val existingClass = createClass(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass)
        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has no URI, then updates and returns success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = createClassWithoutURI().copy(label = "other label").toReplaceCommand()
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass)
        assertThat(actual).isEqualTo(Success(Unit))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when class is unmodifiable, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace, modifiable = false)
        val replacingClass = createClass(label = "other label").toReplaceCommand()
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass)
        assertThat(actual).isEqualTo(Failure(ClassNotModifiableProblem))

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    private fun Class.toReplaceCommand(): ReplaceCommand = ReplaceCommand(
        label = this.label,
        uri = this.uri,
    )
}
