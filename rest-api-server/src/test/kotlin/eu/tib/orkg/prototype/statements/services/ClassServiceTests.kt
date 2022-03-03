package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createClassWithoutURI
import eu.tib.orkg.prototype.statements.api.AlreadyInUse
import eu.tib.orkg.prototype.statements.api.ClassNotFound
import eu.tib.orkg.prototype.statements.api.InvalidLabel
import eu.tib.orkg.prototype.statements.api.InvalidURI
import eu.tib.orkg.prototype.statements.api.UpdateNotAllowed
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClassServiceTests {

    private val repository: ClassRepository = mockk()

    private val service = ClassService(repository)

    @Test
    fun `given a class does not exist, when updating the label, then it returns an appropriate error`() {
        every { repository.findByClassId(any()) } returns Optional.empty()

        val actual = service.updateLabel(ClassId("non-existent"), "new label")

        assertThat(actual).isEqualTo(Failure(ClassNotFound))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is valid, it returns success`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.updateLabel(ClassId("OK"), "new label")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists, when updating the label and the label is invalid, it returns an appropriate error`() {
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(createClass())

        val actual = service.updateLabel(ClassId("OK"), "some\ninvalid\nlabel")

        assertThat(actual).isEqualTo(Failure(InvalidLabel))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists, when updating the label with the same text, it skips the action and returns success`() {
        val originalClass = createClass()
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateLabel(ClassId("OK"), "some label")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class does not exist, when updating the URI, then it returns an appropriate error`() {
        every { repository.findByClassId(any()) } returns Optional.empty()

        val actual = service.updateURI(ClassId("non-existent"), "https://example.org/foo")

        assertThat(actual).isEqualTo(Failure(ClassNotFound))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is not already used, it returns success`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.updateURI(ClassId("OK"), "https://example.org/NEW")

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = URI.create("https://example.org/NEW"))
        val differentWithSameURI = createClassWithoutURI().copy(id = ClassId("different"), uri = expectedClass.uri)
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.updateURI(ClassId("OK"), "https://example.org/NEW")

        assertThat(actual).isEqualTo(Failure(AlreadyInUse))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has no URI, when updating the URI and the URI is invalid, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateURI(ClassId("OK"), "\n")

        assertThat(actual).isEqualTo(Failure(InvalidURI))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class exists and has a URI, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass()
        every { repository.findByClassId(ClassId("OK")) } returns Optional.of(originalClass)

        val actual = service.updateURI(ClassId("OK"), "https://example.com/DIFFERENT")

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when only valid inputs are provided, then updates and returns success`() {
        val classToReplace = ClassId("ToReplace")
        val originalClass = createClass()
        val replacingClass = createClass().copy(label = "other label")
        val expectedClass = originalClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findByClassId(classToReplace) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when an invalid label is provided, then returns an error`() {
        val classToReplace = ClassId("ToReplace")
        val replacingClass = createClass().copy(label = "invalid\nlabel", uri = URI.create("https://example.com/NEW"))

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Failure(InvalidLabel))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has a URI, then returns an error`() {
        val classToReplace = ClassId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(label = "other label")
        val existingClass = createClass().copy(id = classToReplace)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ClassId("ToReplace")
        val existingClass = createClass().copy(id = classToReplace)
        val replacingClass = createClass().copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is not already used, then updates return success`() {
        val classToReplace = ClassId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass().copy(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then returns an error`() {
        val classToReplace = ClassId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass =
            createClass().copy(uri = URI.create("https://example.com/NEW"), label = existingClass.label)
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val differentWithSameURI = createClassWithoutURI().copy(id = ClassId("different"), uri = expectedClass.uri)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Failure(AlreadyInUse))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when a URI is provided and the class has a different URI, then returns an error`() {
        val classToReplace = ClassId("ToReplace")
        val replacingClass = createClass().copy(label = "other label", uri = URI.create("https://example.com/NEW"))
        val existingClass = createClass().copy(id = classToReplace)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Failure(UpdateNotAllowed))
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a class is replaced, when no URI is provided and the class has no URI, then updates and returns success`() {
        val classToReplace = ClassId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = createClassWithoutURI().copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        every { repository.findByClassId(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } returns expectedClass

        val actual = service.replace(classToReplace, with = replacingClass)

        assertThat(actual).isEqualTo(Success(Unit))
        verify(exactly = 1) { repository.save(expectedClass) }
    }
}
