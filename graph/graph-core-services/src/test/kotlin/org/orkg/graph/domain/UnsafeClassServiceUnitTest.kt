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
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase.ReplaceCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createClassWithoutURI
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class UnsafeClassServiceUnitTest : MockkBaseTest {
    private val repository: ClassRepository = mockk()
    private val service = UnsafeClassService(repository, fixedClock)

    @Test
    fun `Given a class is created, when no id is given, then it gets an id from the repository`() {
        val mockClassId = ThingId("1")
        val command = CreateClassUseCase.CreateCommand(
            id = null,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant"
        )

        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } just runs

        service.create(command) shouldBe mockClassId

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `Given a class is created, when an id is given, then it does not get a new id`() {
        val mockClassId = ThingId("1")
        val command = CreateClassUseCase.CreateCommand(
            id = mockClassId,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant"
        )

        every { repository.save(any()) } just runs

        service.create(command) shouldBe mockClassId

        verify(exactly = 1) { repository.save(withArg { it.id shouldBe command.id }) }
    }

    @Test
    fun `Given a class is created, when an already existing id is given, then it overrides the existing class`() {
        val mockClassId = ThingId("1")
        val command = CreateClassUseCase.CreateCommand(
            id = mockClassId,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant"
        )

        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.save(withArg { it.id shouldBe command.id }) }
    }

    @Test
    fun `Given a class is created, when a reserved id is given, then it creates the class`() {
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateClassUseCase.CreateCommand(
            id = reservedClassIds.first(),
            contributorId = contributorId,
            label = "irrelevant"
        )

        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.save(withArg { it.id shouldBe reservedClassIds.first() }) }
    }

    @Test
    fun `Given a class is created, when the label is invalid, then it creates the class`() {
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = " \t "
        )
        val id = ThingId("C123")

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe command.label
                }
            )
        }
    }

    @Test
    fun `Given a class is created, when uri is not absolute, then it creates the class`() {
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            uri = ParsedIRI.create("invalid")
        )
        val id = ThingId("C123")

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe id
                    it.uri shouldBe command.uri
                }
            )
        }
    }

    @Test
    fun `Given a class is created, when an already existing uri is given, then it creates the class`() {
        val mockClass = createClass(uri = ParsedIRI.create("https://orkg.org/class/C1"))
        val command = CreateClassUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            uri = mockClass.uri
        )
        val id = ThingId("C123")

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe id
                    it.uri shouldBe command.uri
                }
            )
        }
    }

    @Test
    fun `Given a class is created, when a contributor is given, the contributor id is used`() {
        val mockClassId = ThingId("1")
        every { repository.nextIdentity() } returns mockClassId
        every { repository.save(any()) } just runs

        val randomContributorId = ContributorId(UUID.randomUUID())
        service.create(CreateClassUseCase.CreateCommand(contributorId = randomContributorId, label = "irrelevant"))

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
    fun `Given a class exists, when updating the label and the label is valid, it updates the label`() {
        val originalClass = createClass()
        val expectedClass = originalClass.copy(label = "new label")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            label = "new label"
        )

        every { repository.findById(originalClass.id) } returns Optional.of(originalClass)
        every { repository.save(expectedClass) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class exists, when updating the label and the label is invalid, it updates the label`() {
        val originalClass = createClass()
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "some\ninvalid\nlabel"
        )

        every { repository.findById(originalClass.id) } returns originalClass.toOptional()
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe command.label }) }
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
    fun `Given a class is unmodifiable, when updating the label, it updates the label`() {
        val originalClass = createClass(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            label = "changed label"
        )

        every { repository.findById(originalClass.id) } returns originalClass.toOptional()
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe originalClass.id
                    it.label shouldBe command.label
                    it.uri shouldBe originalClass.uri
                    it.createdAt shouldBe originalClass.createdAt
                    it.createdBy shouldBe originalClass.createdBy
                    it.modifiable shouldBe false
                }
            )
        }
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
        every { repository.save(expectedClass) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class exists and has no URI, when updating the URI and the URI is valid and the URI is already used, it updates the URI`() {
        val originalClass = createClassWithoutURI()
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/NEW")
        )

        every { repository.findById(originalClass.id) } returns originalClass.toOptional()
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe command.id
                    it.uri shouldBe command.uri
                }
            )
        }
    }

    @Test
    fun `Given a class exists and has a URI, when updating the URI, it updates the URI`() {
        val originalClass = createClass()
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.org/DIFFERENT")
        )

        every { repository.findById(originalClass.id) } returns originalClass.toOptional()
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.uri shouldBe command.uri }) }
    }

    @Test
    fun `Given a class exists, when updating with no URI and no label, it does nothing`() {
        val id = ThingId("EXISTS")
        val contributorId = ContributorId(MockUserId.USER)
        service.update(UpdateClassUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a class is unmodifiable, when updating the URI, it updates the URI`() {
        val originalClass = createClass(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateClassUseCase.UpdateCommand(
            id = originalClass.id,
            contributorId = contributorId,
            uri = ParsedIRI.create("https://example.com/DIFFERENT")
        )

        every { repository.findById(originalClass.id) } returns originalClass.toOptional()
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe originalClass.id
                    it.label shouldBe originalClass.label
                    it.uri shouldBe command.uri
                    it.createdAt shouldBe originalClass.createdAt
                    it.createdBy shouldBe originalClass.createdBy
                    it.modifiable shouldBe false
                }
            )
        }
    }

    @Test
    fun `Given a class replace command, when replacing all properties, it returns success`() {
        val `class` = createClass(uri = null)
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val uri = ParsedIRI.create("https://example.org/C1")
        val modifiable = false

        every { repository.findById(`class`.id) } returns Optional.of(`class`)
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
        every { repository.save(expectedClass) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(originalClass.id) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when an invalid label is provided, then it updates the label`() {
        val replacingClass = createClass(label = "invalid\nlabel", uri = ParsedIRI.create("https://example.com/NEW"))
        val existingClass = createClass()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(replacingClass.id) } returns existingClass.toOptional()
        every { repository.save(any()) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(replacingClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe replacingClass.label }) }
    }

    @Test
    fun `Given a class is replaced, when no URI is provided and the class has a URI, then it removes the URI`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClassWithoutURI().copy(id = classToReplace, label = "other label")
        val existingClass = createClass(id = classToReplace)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(replacingClass.id) } returns existingClass.toOptional()
        every { repository.save(any()) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(replacingClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.uri shouldBe replacingClass.uri }) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has the same URI, then updates return success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
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
        every { repository.save(expectedClass) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has no URI and the URI is already used, then it updates the URI`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(uri = ParsedIRI.create("https://example.com/NEW"))
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(replacingClass.id) } returns existingClass.toOptional()
        every { repository.save(any()) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(replacingClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.uri shouldBe replacingClass.uri }) }
    }

    @Test
    fun `Given a class is replaced, when a URI is provided and the class has a different URI, then it updates the URI`() {
        val classToReplace = ThingId("ToReplace")
        val replacingClass = createClass(id = classToReplace, label = "other label", uri = ParsedIRI.create("https://example.com/NEW"))
        val existingClass = createClass(id = classToReplace)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(replacingClass.id) } returns existingClass.toOptional()
        every { repository.save(any()) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(replacingClass.id) }
        verify(exactly = 1) { repository.save(withArg { it.uri shouldBe replacingClass.uri }) }
    }

    @Test
    fun `Given a class is replaced, when no URI is provided and the class has no URI, then updates and returns success`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClassWithoutURI().copy(id = classToReplace)
        val replacingClass = existingClass.copy(label = "other label")
        val expectedClass = existingClass.copy(id = classToReplace, label = replacingClass.label)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(classToReplace) } returns existingClass.toOptional()
        every { repository.save(expectedClass) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(classToReplace) }
        verify(exactly = 1) { repository.save(expectedClass) }
    }

    @Test
    fun `Given a class is replaced, when class is unmodifiable, then it updates the class`() {
        val classToReplace = ThingId("ToReplace")
        val existingClass = createClass(id = classToReplace, modifiable = false)
        val replacingClass = existingClass.copy(label = "other label")
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(replacingClass.id) } returns existingClass.toOptional()
        every { repository.save(any()) } just runs

        service.replace(replacingClass.toReplaceCommand(contributorId))

        verify(exactly = 1) { repository.findById(replacingClass.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe existingClass.id
                    it.label shouldBe replacingClass.label
                    it.uri shouldBe replacingClass.uri
                    it.createdAt shouldBe existingClass.createdAt
                    it.createdBy shouldBe existingClass.createdBy
                    it.modifiable shouldBe false
                }
            )
        }
    }

    private fun Class.toReplaceCommand(contributorId: ContributorId): ReplaceCommand =
        ReplaceCommand(id, contributorId, label, uri)
}
