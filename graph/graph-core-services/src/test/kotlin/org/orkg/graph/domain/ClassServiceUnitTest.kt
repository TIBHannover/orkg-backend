package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase.ReplaceCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createClassWithoutURI
import org.orkg.testing.MockUserId
import java.util.Optional

internal class ClassServiceUnitTest : MockkBaseTest {
    private val repository: ClassRepository = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()

    private val service = ClassService(repository, unsafeClassUseCases)

    @Test
    fun `Given a class create command, when inputs are valid, it creates a new class`() {
        val id = ThingId("R123")
        val command = CreateClassUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            modifiable = false
        )

        every { repository.findById(id) } returns Optional.empty()
        every { unsafeClassUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { unsafeClassUseCases.create(command) }
    }

    @Test
    fun `Given a class create command, when inputs are minimal, it creates a new class`() {
        val id = ThingId("R123")
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "label"
        )

        every { unsafeClassUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { unsafeClassUseCases.create(command) }
    }

    @Test
    fun `Given a class is created, when an already existing id is given, then an exception is thrown`() {
        val mockClassId = ThingId("1")
        val command = CreateClassUseCase.CreateCommand(
            id = mockClassId,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant"
        )

        every { repository.findById(mockClassId) } returns createClass(id = mockClassId).toOptional()

        assertThrows<ClassAlreadyExists> { service.create(command) }

        verify(exactly = 1) { repository.findById(mockClassId) }
    }

    @Test
    fun `Given a class is created, when a reserved id is given, then an exception is thrown`() {
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateClassUseCase.CreateCommand(
            id = reservedClassIds.first(),
            contributorId = contributorId,
            label = "irrelevant"
        )
        assertThrows<ReservedClassId> { service.create(command) }
    }

    @Test
    fun `Given a class is created, when the label is invalid, then an exception is thrown`() {
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = " \t "
        )
        assertThrows<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `Given a class is created, when uri is not absolute, then an exception is thrown`() {
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            uri = ParsedIRI.create("invalid")
        )
        assertThrows<URINotAbsolute> { service.create(command) }
    }

    @Test
    fun `Given a class is created, when an already existing uri is given, then an exception is thrown`() {
        val mockClass = createClass(uri = ParsedIRI.create("https://orkg.org/class/C1"))
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            uri = mockClass.uri
        )

        every { repository.findByUri(mockClass.uri.toString()) } returns mockClass.toOptional()

        assertThrows<URIAlreadyInUse> { service.create(command) }

        verify(exactly = 1) { repository.findByUri(mockClass.uri.toString()) }
    }

    @Test
    fun `Given a class does not exist, when updating the label, then it returns an appropriate error`() {
        val id = ThingId("non-existent")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = id,
            contributorId = contributorId,
            label = "new label"
        )

        every { repository.findById(any()) } returns Optional.empty()

        assertThrows<ClassNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given a class update command, when updating all properties, it returns success`() {
        val `class` = createClass(uri = null)
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val uri = ParsedIRI.create("https://example.org/C1")
        val modifiable = false

        every { repository.findById(`class`.id) } returns Optional.of(`class`)
        every { repository.findByUri(uri.toString()) } returns Optional.empty()
        every { repository.save(any()) } just runs

        service.update(
            UpdateClassUseCase.UpdateCommand(
                id = `class`.id,
                contributorId = contributorId,
                label = label,
                uri = uri,
                modifiable = modifiable,
            )
        )

        verify(exactly = 1) { repository.findById(`class`.id) }
        verify(exactly = 1) { repository.findByUri(uri.toString()) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe `class`.id
                    it.label shouldBe label
                    it.uri shouldBe uri
                    it.createdAt shouldBe `class`.createdAt
                    it.createdBy shouldBe `class`.createdBy
                    it.modifiable shouldBe modifiable
                }
            )
        }
    }

    @Test
    fun `Given a class exists, when updating the label and the label is valid, it returns success`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            label = "new label"
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } returns Unit

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class exists, when updating the label and the label is invalid, it returns an appropriate error`() {
        val command = UpdateClassUseCase.UpdateCommand(
            id = ThingId("OK"),
            contributorId = ContributorId(MockUserId.USER),
            label = "some\ninvalid\nlabel"
        )
        assertThrows<InvalidLabel> { service.update(command) }
    }

    @Test
    fun `Given a class exists, when updating the label with the same text, it skips the action and returns success`() {
        val originalClass = createClass()
        val id = ThingId("OK")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = id,
            contributorId = contributorId,
            label = "some label"
        )

        every { repository.findById(id) } returns Optional.of(originalClass)

        service.update(command)

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given a class is unmodifiable, when updating the label, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            label = "some label"
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<ClassNotModifiable> { service.update(command) }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `Given a class does not exist, when updating the URI, then it returns an appropriate error`() {
        val id = ThingId("non-existent")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/foo")
        )

        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ClassNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given a class exists and has no URI, when updating the URI and the URI is valid and the URI is not already used, it returns success`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = ParsedIRI.create("https://example.org/NEW"))
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/NEW")
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it returns an appropriate error`() {
        val originalClass = createClassWithoutURI()
        val expectedClass = originalClass.copy(uri = ParsedIRI.create("https://example.org/NEW"))
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/NEW")
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        assertThrows<URIAlreadyInUse> { service.update(command) }

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `Given a class exists and has a URI, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass()
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/DIFFERENT")
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<CannotResetURI> { service.update(command) }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `Given a class exists, when updating with no URI and no label, it does nothing`() {
        val id = ThingId("EXISTS")
        val contributorId = ContributorId(MockUserId.USER)
        service.update(UpdateClassUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a class is unmodifiable, when updating the URI, it returns an appropriate error`() {
        val originalClass = createClass(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.com/DIFFERENT")
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)

        assertThrows<ClassNotModifiable> { service.update(command) }

        verify(exactly = 1) { repository.findById(originalClass.id) }
    }

    @Test
    fun `Given a class replace command, when replacing all properties, it returns success`() {
        val `class` = createClass(uri = null)
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val uri = ParsedIRI.create("https://example.org/C1")
        val modifiable = false

        every { repository.findById(`class`.id) } returns Optional.of(`class`)
        every { repository.findByUri(uri.toString()) } returns Optional.empty()
        every { repository.save(any()) } just runs

        service.replace(
            ReplaceCommand(
                id = `class`.id,
                contributorId = contributorId,
                label = label,
                uri = uri,
                modifiable = modifiable,
            )
        )

        verify(exactly = 1) { repository.findById(`class`.id) }
        verify(exactly = 1) { repository.findByUri(uri.toString()) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe `class`.id
                    it.label shouldBe label
                    it.uri shouldBe uri
                    it.createdAt shouldBe `class`.createdAt
                    it.createdBy shouldBe `class`.createdBy
                    it.modifiable shouldBe modifiable
                }
            )
        }
    }

    @Test
    fun `Given a class is replaced, when only valid inputs are provided, then updates and returns success`() {
        val originalClass = createClass()
        val replacingClass = createClass(label = "other label")
        val expectedClass = originalClass.copy(id = originalClass.id, label = replacingClass.label)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when an invalid label is provided, then returns an error`() {
        val replacingClass = createClass(label = "invalid\nlabel", uri = ParsedIRI.create("https://example.com/NEW"))
        val contributorId = ContributorId(MockUserId.USER)

        assertThrows<InvalidLabel> { service.replace(replacingClass.toReplaceCommand(contributorId)) }
    }

    @Test
    fun `Given a class is replaced, when no URI is provided and the class has a URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(id = classToReplace, label = "other label")
        val existingClass = createClass(id = classToReplace)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<CannotResetURI> { service.replace(replacingClass.toReplaceCommand(contributorId)) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has no URI and the URI is not already used, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(uri = ParsedIRI.create("https://example.com/NEW"))
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns Optional.empty()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(uri = ParsedIRI.create("https://example.com/NEW"))
        val expectedClass = existingClass.copy(id = classToReplace, uri = replacingClass.uri)
        val differentWithSameURI = createClassWithoutURI().copy(id = ThingId("different"), uri = expectedClass.uri)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { service.findByURI(expectedClass.uri!!) } returns differentWithSameURI.toOptional()

        assertThrows<URIAlreadyInUse> { service.replace(replacingClass.toReplaceCommand(contributorId)) }

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { service.findByURI(expectedClass.uri!!) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has a different URI, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClass(id = classToReplace, label = "other label", uri = ParsedIRI.create("https://example.com/NEW"))
        val existingClass = createClass(id = classToReplace)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<CannotResetURI> { service.replace(replacingClass.toReplaceCommand(contributorId)) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    @Test
    fun `Given a class is replaced, when no URI is provided and the class has no URI, then updates and returns success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } returns Unit

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when class is unmodifiable, then returns an error`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace, modifiable = false)
        val replacingClass = existingClass.copy(label = "other label")
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()

        assertThrows<ClassNotModifiable> { service.replace(replacingClass.toReplaceCommand(contributorId)) }

        verify(exactly = 1) { repository.findById(classToReplace) }
    }

    private fun Class.toReplaceCommand(contributorId: ContributorId): ReplaceCommand =
        ReplaceCommand(id, contributorId, label, uri)
}
