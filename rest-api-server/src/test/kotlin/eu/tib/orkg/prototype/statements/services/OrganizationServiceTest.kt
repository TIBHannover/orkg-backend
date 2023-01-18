package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.services.OrganizationService
import eu.tib.orkg.prototype.testImage
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.api.CreateImageUseCase
import eu.tib.orkg.prototype.files.api.ImageUseCases
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.loadImage
import eu.tib.orkg.prototype.loadRawImage
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
        val contributor = ContributorId(UUID.randomUUID())
        val command = CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor)

        every { repository.findById(id.value) } returns Optional.of(organization)
        every { imageService.create(command) } returns imageId
        every { repository.save(organization) } returns organization

        service.updateLogo(id, image.data, image.mimeType, contributor)

        verify(exactly = 1) { imageService.create(command) }
    }

    @Test
    fun `given an organization id and an image, when updating the organization logo for a missing organization, it returns an appropriate error`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val contributor = ContributorId(UUID.randomUUID())

        every { repository.findById(id.value) } throws OrganizationNotFound(id)

        assertThrows<OrganizationNotFound> {
            service.updateLogo(id, image.data, image.mimeType, contributor)
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

        service.updateLogo(id, image.data, image.mimeType, contributor)

        verify(exactly = 1) { imageService.create(command) }
        verify(exactly = 1) { repository.save(organization) }
    }
}
