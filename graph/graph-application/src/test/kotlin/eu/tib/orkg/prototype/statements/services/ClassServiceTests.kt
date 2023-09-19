package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createClassWithoutURI
import eu.tib.orkg.prototype.statements.api.AlreadyInUse
import eu.tib.orkg.prototype.statements.api.ClassNotFound
import eu.tib.orkg.prototype.statements.api.CreateClassUseCase
import eu.tib.orkg.prototype.statements.api.InvalidLabel
import eu.tib.orkg.prototype.statements.api.InvalidURI
import eu.tib.orkg.prototype.statements.api.UpdateClassUseCase.ReplaceCommand
import eu.tib.orkg.prototype.statements.api.UpdateNotAllowed
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClassServiceTests {

    private val repository: ClassRepository = mockk()
    private val fixedTime = OffsetDateTime.of(2022, 11, 14, 14, 9, 23, 12345, ZoneOffset.ofHours(1))
    private val staticClock = java.time.Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())
    private val service = ClassService(repository, staticClock)

    @Test
    fun `given a class is created, when no id is given, then it gets an id from the repository`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = null))

        verify(exactly = 1) { repository.nextIdentity() }
    }

    @Test
    fun `given a class is created, when an id is given, then it does not get a new id`() {
        val mockClassId = ThingId("1")
        every { repository.save(any()) } returns Unit

        service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = mockClassId.value))

        verify(exactly = 0) { repository.nextIdentity() }
    }

    @Test
    fun `given a class is created, when the id is invalid, then an exception is thrown`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.create(CreateClassUseCase.CreateCommand(label = "irrelevant", id = "!invalid"))
        }
        assertThat(exception.message).isEqualTo("Must only contain alphanumeric characters, dashes and underscores")
    }

    @Test
    fun `given a class is created, when the label is invalid, then an exception is thrown`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId

        val exception = assertThrows<IllegalArgumentException> {
            service.create(CreateClassUseCase.CreateCommand(label = " \t "))
        }
        assertThat(exception.message).isEqualTo("Invalid label:  \t ")
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
                    createdAt = OffsetDateTime.now(staticClock),
                    createdBy = ContributorId(UUID(0, 0)),
                )
            )
        }
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
                    createdAt = OffsetDateTime.now(staticClock),
                    createdBy = randomContributorId,
                )
            )
        }
    }

    @Test
    fun `given a class does not exist, when updating the label, then it returns an appropriate error`() {
        every { repository.findById(any()) } returns Optional.empty()

        val actual = service.updateLabel(ThingId("non-existent"), "new label")

        assertThat(actual).isEqualTo(Failure(ClassNotFound))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is valid, it returns success`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } returns Unit

        val actual = service.updateLabel(ThingId("OK"), "new label")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is invalid, it returns an appropriate error`() {
        every { repository.findById(ThingId("OK")) } returns Optional.of(createClass())

        val actual = service.updateLabel(ThingId("OK"), "some\ninvalid\nlabel")

        assertThat(actual).isEqualTo(Failure(InvalidLabel))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists, when updating the label with the same text, it skips the action and returns success`() {
        val originalClass = createClass()
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateLabel(ThingId("OK"), "some label")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class does not exist, when updating the URI, then it returns an appropriate error`() {
        every { repository.findById(any()) } returns Optional.empty()

        val actual = service.updateURI(ThingId("non-existent"), "https://example.org/foo")

        assertThat(actual).isEqualTo(Failure(ClassNotFound))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is not already used, it returns success`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.updateURI(ThingId("OK"), "https://example.org/NEW")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.updateURI(ThingId("OK"), "https://example.org/NEW")

        assertThat(actual).isEqualTo(Failure(AlreadyInUse))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is invalid, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateURI(ThingId("OK"), "\n")

        assertThat(actual).isEqualTo(Failure(InvalidURI))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has a URI, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass()
        every { repository.findById(ThingId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateURI(ThingId("OK"), "https://example.com/DIFFERENT")

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when only valid inputs are provided, then updates and returns success`() {
        val classToReplace = ThingId("ToReplace")
        val originalClass = createClass()
        val replacingClass = createClass().copy(label = "other label")
        val expectedClass = originalClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when an invalid label is provided, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClass().copy(label = "invalid\nlabel", uri = URI.create("https://example.com/NEW"))

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Failure(InvalidLabel))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has a URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(label = "other label")
        val existingClass = createClass().copy(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass().copy(id = classToReplace)
        val replacingClass = createClass().copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is not already used, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass().copy(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass().copy(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()
        every { repository.save(expectedClass) } returns Unit

        val actual = service.replace(classToReplace, command = replacingClass.toReplaceCommand())

        assertThat(actual).isEqualTo(Failure(AlreadyInUse))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has a different URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass =
            createClass().copy(label = "other label", uri = URI.create("https://example.com/NEW")).toReplaceCommand()
        val existingClass = createClass().copy(id = classToReplace)
        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, command = replacingClass)

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
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
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    private fun Class.toReplaceCommand(): ReplaceCommand = ReplaceCommand(
        label = this.label,
        uri = this.uri,
    )
}
