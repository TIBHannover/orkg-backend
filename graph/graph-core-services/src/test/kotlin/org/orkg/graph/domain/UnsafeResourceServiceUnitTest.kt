package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class UnsafeResourceServiceUnitTest : MockkBaseTest {
    private val repository: ResourceRepository = mockk()

    private val service = UnsafeResourceService(repository, fixedClock)

    @Test
    fun `Given a resource create command, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            classes = setOf(Classes.paper),
            extractionMethod = ExtractionMethod.MANUAL,
            observatoryId = ObservatoryId("1255bbe4-1850-4033-ba10-c80d4b370e3e"),
            organizationId = OrganizationId("56a4b65e-de56-0d4b-255b-255b372b65ef"),
            modifiable = false
        )

        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe command.id
                    it.label shouldBe command.label
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.classes shouldBe command.classes
                    it.createdBy shouldBe command.contributorId
                    it.observatoryId shouldBe command.observatoryId
                    it.extractionMethod shouldBe command.extractionMethod
                    it.organizationId shouldBe command.organizationId
                    it.visibility shouldBe Visibility.DEFAULT
                    it.verified shouldBe null
                    it.unlistedBy shouldBe null
                    it.modifiable shouldBe command.modifiable
                }
            )
        }
    }

    @Test
    fun `Given a resource create command, when using minimal inputs, it assigns a new id and creates a new resource`() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "label"
        )

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe command.label
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.classes shouldBe command.classes
                    it.createdBy shouldBe contributorId
                    it.observatoryId shouldBe ObservatoryId.UNKNOWN
                    it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                    it.organizationId shouldBe OrganizationId.UNKNOWN
                    it.visibility shouldBe Visibility.DEFAULT
                    it.verified shouldBe null
                    it.unlistedBy shouldBe null
                    it.modifiable shouldBe command.modifiable
                }
            )
        }
    }

    @Test
    fun `Given a resource update command, when updating all properties, it returns success`() {
        val resource = createResource()
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val classes = setOf(Classes.paper)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.AUTOMATIC
        val modifiable = false
        val visibility = Visibility.FEATURED
        val verified = true

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                id = resource.id,
                contributorId = contributorId,
                label = label,
                classes = classes,
                observatoryId = observatoryId,
                organizationId = organizationId,
                extractionMethod = extractionMethod,
                modifiable = modifiable,
                visibility = visibility,
                verified = verified
            )
        )

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.label shouldBe label
                    it.classes shouldBe classes
                    it.observatoryId shouldBe observatoryId
                    it.organizationId shouldBe organizationId
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                    it.visibility shouldBe visibility
                    it.verified shouldBe verified
                }
            )
        }
    }

    @Test
    fun `Given a resource update command, when updating no properties, it does nothing`() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        service.update(UpdateResourceUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a resource update command, when updating with a reserved class, it updates the resource`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            classes = setOf(Classes.list)
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.classes shouldBe setOf(Classes.list) }) }
    }

    @Test
    fun `Given a resource update command, when updating with an invalid label, it updates the resource`() {
        val resource = createResource()
        val label = "a".repeat(MAX_LABEL_LENGTH + 1)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            label = label
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe label }) }
    }

    @Test
    fun `Given a resource update command, when resource is unmodifiable resource, it updates the resource`() {
        val resource = createResource(modifiable = false)
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), label = "new label")

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe "new label" }) }
    }

    @Test
    fun `Given a resource update command, when updating with a visibility that requires a curator role, it updates the resource`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), visibility = Visibility.FEATURED)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.visibility shouldBe Visibility.FEATURED }) }
    }

    @Test
    fun `Given a resource update command, when updating the verified flag without being a curator, it updates the resource`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), verified = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.verified shouldBe true }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed to unlisted, it sets the unlisted by metadata`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            visibility = Visibility.UNLISTED
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe command.contributorId }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed from unlisted to something else, it clears the unlisted by metadata`() {
        val resource = createResource(
            visibility = Visibility.UNLISTED,
            unlistedBy = ContributorId(MockUserId.USER)
        )
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            visibility = Visibility.DEFAULT
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe null }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed from unlisted to unlisted, it keeps the unlisted by metadata`() {
        val resource = createResource(
            visibility = Visibility.UNLISTED,
            unlistedBy = ContributorId(MockUserId.ADMIN)
        )
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            label = "some change",
            visibility = Visibility.UNLISTED
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe resource.unlistedBy }) }
    }

    @Test
    fun `Given a resource update command, when updating a rosetta stone statement resource, it updates the resource`() {
        val resource = createResource(classes = setOf(Classes.rosettaStoneStatement))
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), label = "new label")

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe command.label }) }
    }

    @Test
    fun `Given a resource, when deleting, it deletes the resource from the repository`() {
        val id = ThingId("R2145")
        val couldBeAnyone = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")

        every { repository.deleteById(id) } just runs

        service.delete(id, couldBeAnyone)

        verify(exactly = 1) { repository.deleteById(id) }
    }
}
