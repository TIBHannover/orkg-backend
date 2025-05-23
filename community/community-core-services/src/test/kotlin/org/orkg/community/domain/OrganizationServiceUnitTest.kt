package org.orkg.community.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.mediastorage.testing.fixtures.loadImage
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage
import java.util.Optional
import java.util.UUID

internal class OrganizationServiceUnitTest : MockkBaseTest {
    private val repository: OrganizationRepository = mockk()
    private val imageService: ImageUseCases = mockk()
    private val service = OrganizationService(repository, imageService)

    @Test
    fun `given an organization id, when the logo of the organization is requested, the logo is returned`() {
        val image = loadImage(testImage)
        val organization = createOrganization(logoId = image.id)
        val id = organization.id!!

        every { repository.findById(id) } returns Optional.of(organization)
        every { imageService.findById(image.id) } returns Optional.of(image)

        val result = service.findLogoById(id)
        result.isPresent shouldBe true
        result.get() shouldBe image

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { imageService.findById(image.id) }
    }

    @Test
    fun `given an organization id, when the organization has no logo id, the logo is not searched in the repository`() {
        val organization = createOrganization()
        val id = organization.id!!

        every { repository.findById(id) } returns Optional.of(organization)

        val result = service.findLogoById(id)
        result.isPresent shouldBe false

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 0) { imageService.findById(any()) }
    }

    @Test
    fun `given an organization id, when the logo of a missing organization is requested, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        every { repository.findById(id) } throws OrganizationNotFound(id)

        assertThrows<OrganizationNotFound> {
            service.findLogoById(id)
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given an organization update command, when repository reports organization not found, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = null,
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id) } returns Optional.empty()

        assertThrows<OrganizationNotFound> {
            service.update(contributorId, command)
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given an organization update command, when updating the organization name, it succeeds`() {
        val organization = createOrganization()
        val id = organization.id!!
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = "newName",
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals("newName", it.name)
                    assertEquals(organization.homepage, it.homepage)
                    assertEquals(organization.type, it.type)
                    assertEquals(organization.logoId, it.logoId)
                }
            )
        }
    }

    @Test
    fun `given an organization update command, when updating the organization url, it succeeds`() {
        val organization = createOrganization()
        val id = organization.id!!
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = null,
            url = "https://example.org",
            type = null,
            logo = null
        )

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals(organization.name, it.name)
                    assertEquals("https://example.org", it.homepage)
                    assertEquals(organization.type, it.type)
                    assertEquals(organization.logoId, it.logoId)
                }
            )
        }
    }

    @Test
    fun `given an organization update command, when updating the organization type, it succeeds`() {
        val organization = createOrganization()
        val id = organization.id!!
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = null,
            url = null,
            type = OrganizationType.GENERAL,
            logo = null
        )

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals(organization.name, it.name)
                    assertEquals(organization.homepage, it.homepage)
                    assertEquals(OrganizationType.GENERAL, it.type)
                    assertEquals(organization.logoId, it.logoId)
                }
            )
        }
    }

    @Test
    fun `given an organization update command, when updating the organization logo, it succeeds`() {
        val organization = createOrganization()
        val id = organization.id!!
        val image = loadRawImage(testImage)
        val imageId = ImageId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = null,
            url = null,
            type = null,
            logo = image
        )
        val createImageCommand = CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs
        every { imageService.create(createImageCommand) } returns imageId

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals(organization.name, it.name)
                    assertEquals(organization.homepage, it.homepage)
                    assertEquals(organization.type, it.type)
                    assertEquals(imageId, it.logoId)
                }
            )
        }
        verify(exactly = 1) { imageService.create(createImageCommand) }
    }

    @Test
    fun `given an organization update command, when updating all properties, it succeeds`() {
        val organization = createOrganization()
        val id = organization.id!!
        val image = loadRawImage(testImage)
        val imageId = ImageId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = "newName",
            url = "https://example.org",
            type = OrganizationType.CONFERENCE,
            logo = image
        )

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs
        every { imageService.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)) } returns imageId

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals("newName", it.name)
                    assertEquals("https://example.org", it.homepage)
                    assertEquals(OrganizationType.CONFERENCE, it.type)
                    assertEquals(imageId, it.logoId)
                }
            )
        }
        verify(exactly = 1) { imageService.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributorId)) }
    }

    @Test
    fun `given an organization update command, when updating one property, it keeps the old properties`() {
        val imageId = ImageId(UUID.randomUUID())
        val contributorId = ContributorId(UUID.randomUUID())
        val organization = createOrganization(
            name = "oldName",
            homepage = "https://example.org",
            type = OrganizationType.CONFERENCE,
            logoId = imageId,
        )
        val id = organization.id!!
        val command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
            id = id,
            name = "newName",
            url = null,
            type = null,
            logo = null
        )

        every { repository.findById(id) } returns Optional.of(organization)
        every { repository.save(organization) } just runs

        service.update(contributorId, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    assertEquals(organization.id, it.id)
                    assertEquals("newName", it.name)
                    assertEquals("https://example.org", it.homepage)
                    assertEquals(OrganizationType.CONFERENCE, it.type)
                    assertEquals(imageId, it.logoId)
                }
            )
        }
    }
}
