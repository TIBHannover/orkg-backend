package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
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

internal class UnsafeResourceServiceUnitTest : MockkBaseTest {

    private val repository: ResourceRepository = mockk()

    private val service = UnsafeResourceService(repository, fixedClock)

    @Test
    fun `Given a resource create command, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            label = "label",
            classes = setOf(Classes.paper),
            extractionMethod = ExtractionMethod.MANUAL,
            contributorId = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId("1255bbe4-1850-4033-ba10-c80d4b370e3e"),
            organizationId = OrganizationId("56a4b65e-de56-0d4b-255b-255b372b65ef"),
            modifiable = false
        )

        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) {
            repository.save(withArg {
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
            })
        }
    }

    @Test
    fun `Given a resource create command, when using minimal inputs, it assigns a new id and creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            label = "label"
        )

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe id
                it.label shouldBe command.label
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.classes shouldBe command.classes
                it.createdBy shouldBe ContributorId.UNKNOWN
                it.observatoryId shouldBe ObservatoryId.UNKNOWN
                it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                it.organizationId shouldBe OrganizationId.UNKNOWN
                it.visibility shouldBe Visibility.DEFAULT
                it.verified shouldBe null
                it.unlistedBy shouldBe null
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `Given a resource update command, when updating all properties, it returns success`() {
        val resource = createResource()
        val label = "updated label"
        val classes = setOf(Classes.paper)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.AUTOMATIC
        val modifiable = false

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                resource.id, label, classes, observatoryId, organizationId, extractionMethod, modifiable
            )
        )

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.label shouldBe label
                it.classes shouldBe classes
                it.observatoryId shouldBe observatoryId
                it.organizationId shouldBe organizationId
                it.extractionMethod shouldBe extractionMethod
                it.modifiable shouldBe modifiable
            })
        }
    }

    @Test
    fun `Given a resource update command, when updating no properties, it returns success`() {
        val resource = createResource()

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(UpdateResourceUseCase.UpdateCommand(resource.id))

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.label shouldBe resource.label
                it.classes shouldBe resource.classes
                it.observatoryId shouldBe resource.observatoryId
                it.organizationId shouldBe resource.organizationId
                it.extractionMethod shouldBe resource.extractionMethod
                it.modifiable shouldBe resource.modifiable
            })
        }
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
