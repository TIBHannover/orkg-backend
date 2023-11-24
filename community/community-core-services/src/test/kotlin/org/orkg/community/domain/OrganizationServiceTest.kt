package org.orkg.community.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.mediastorage.testing.fixtures.loadImage
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage

class OrganizationServiceTest {
    private val repository: PostgresOrganizationRepository = mockk()
    private val imageService: ImageUseCases = mockk()
    private val service = OrganizationService(repository, imageService)

    @Test
    fun `given an organization id, when the logo of the organization is requested, the logo is returned`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadImage(testImage)
        val organization = OrganizationEntity().apply {
            logoId = image.id.value
        }

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { imageService.find(image.id) } returns Optional.of(image)

        val result = service.findLogo(id)
        result.isPresent shouldBe true
        result.get() shouldBe image

        verify(exactly = 1) { repository.findById(id.value) }
        verify(exactly = 1) { imageService.find(image.id) }
    }

    @Test
    fun `given an organization id, when the organization has no logo id, the logo is not searched in the repository`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)

        every { repository.findById(id.value) } returns Optional.of(organization)

        val result = service.findLogo(id)
        result.isPresent shouldBe false

        verify(exactly = 0) { imageService.find(any()) }
    }

    @Test
    fun `given an organization id, when the logo of a missing organization is requested, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        every { repository.findById(id.value) } throws OrganizationNotFound(id)

        assertThrows<OrganizationNotFound> {
            service.findLogo(id)
        }

        verify(exactly = 1) { repository.findById(id.value) }
    }

    @Test
    fun `given an organization id and an image, when creating the organization logo, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val imageId = ImageId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val organization = OrganizationEntity(id.value)
        val contributorId = ContributorId(UUID.randomUUID())
        val command = CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { imageService.create(command) } returns imageId
        every { repository.save(organization) } returns organization

        service.updateLogo(id, image, contributorId)

        verify(exactly = 1) { imageService.create(command) }
    }

    @Test
    fun `given an organization id and an image, when updating the organization logo for a missing organization, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val contributor = ContributorId(UUID.randomUUID())

        every { repository.findById(id.value) } throws OrganizationNotFound(id)

        assertThrows<OrganizationNotFound> {
            service.updateLogo(id, image, contributor)
        }

        verify(exactly = 1) { repository.findById(id.value) }
    }

    @Test
    fun `given an organization id and an image, when updating the organization logo, it receives a new logo id`() {
        val id = OrganizationId(UUID.randomUUID())
        val oldImageId = ImageId(UUID.randomUUID())
        val newImageId = ImageId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val organization = OrganizationEntity(id.value).apply {
            logoId = oldImageId.value
        }
        val contributor = ContributorId(UUID.randomUUID())
        val command = CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor)

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { imageService.create(command) } returns newImageId
        every { repository.save(organization) } returns organization

        service.updateLogo(id, image, contributor)

        verify(exactly = 1) { imageService.create(command) }
        verify(exactly = 1) { repository.save(organization) }
    }

    @Test
    fun `given an organization update command, when repository reports organization not found, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = null,
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id.value) } returns Optional.empty()

        assertThrows<OrganizationNotFound> {
            service.update(contributorId, command)
        }
    }

    @Test
    fun `given an organization update command, when updating the organization name, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = "newName",
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertEquals("newName", it.name)
                assertNull(it.url)
                assertNull(it.type)
                assertNull(it.logoId)
            })
        }
    }

    @Test
    fun `given an organization update command, when updating the organization url, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = null,
            url = "https://example.org",
            type = null,
            logo = null
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertNull(it.name)
                assertEquals("https://example.org", it.url)
                assertNull(it.type)
                assertNull(it.logoId)
            })
        }
    }

    @Test
    fun `given an organization update command, when updating the organization type, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = null,
            url = null,
            type = OrganizationType.GENERAL,
            logo = null
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertNull(it.name)
                assertNull(it.url)
                assertEquals(OrganizationType.GENERAL, it.type)
                assertNull(it.logoId)
            })
        }
    }

    @Test
    fun `given an organization update command, when updating the organization logo, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)
        val image = loadRawImage(testImage)
        val imageId = ImageId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = null,
            url = null,
            type = null,
            logo = image
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization
        every { imageService.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)) } returns imageId

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertNull(it.name)
                assertNull(it.url)
                assertNull(it.type)
                assertEquals(imageId.value, it.logoId)
            })
        }
    }

    @Test
    fun `given an organization update command, when updating all properties, it succeeds`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value)
        val image = loadRawImage(testImage)
        val imageId = ImageId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = "newName",
            url = "https://example.org",
            type = OrganizationType.CONFERENCE,
            logo = image
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization
        every { imageService.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)) } returns imageId

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertEquals("newName", it.name)
                assertEquals("https://example.org", it.url)
                assertEquals(OrganizationType.CONFERENCE, it.type)
                assertEquals(imageId.value, it.logoId)
            })
        }
    }

    @Test
    fun `given an organization update command, when updating one property, it keeps the old properties`() {
        val id = OrganizationId(UUID.randomUUID())
        val imageId = UUID.randomUUID()
        val contributorId = ContributorId(UUID.randomUUID())
        val organization = OrganizationEntity(id.value).apply {
            name = "oldName"
            url = "https://example.org"
            type = OrganizationType.CONFERENCE
            logoId = imageId
        }
        val command = UpdateOrganizationUseCases.UpdateOrganizationRequest(
            id = id,
            name = "newName",
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { repository.save(organization) } returns organization

        service.update(contributorId, command)

        verify(exactly = 1) {
            repository.save(withArg {
                assertEquals(organization.id, it.id)
                assertEquals("newName", it.name)
                assertEquals("https://example.org", it.url)
                assertEquals(OrganizationType.CONFERENCE, it.type)
                assertEquals(imageId, it.logoId)
            })
        }
    }
}
