package eu.tib.orkg.prototype.files.services

import eu.tib.orkg.prototype.community.application.OrganizationController
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.api.CreateImageUseCase
import eu.tib.orkg.prototype.files.application.InvalidMimeType
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.files.spi.ImageRepository
import eu.tib.orkg.prototype.loadRawImage
import eu.tib.orkg.prototype.statements.domain.model.Clock
import eu.tib.orkg.prototype.testImage
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.activation.MimeType
import org.junit.jupiter.api.Test

class ImageServiceTest {
    private val repository: ImageRepository = mockk()
    private val staticClock: Clock = object : Clock {
        override fun now(): OffsetDateTime = OffsetDateTime.of(2022, 11, 14, 14, 9, 23, 12345, ZoneOffset.ofHours(1))
    }
    private val service = ImageService(repository, staticClock)

    @Test
    fun `given an image is created, then it gets an id and is saved to the repository`() {
        val id = ImageId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val contributor = ContributorId(UUID.randomUUID())
        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } returns Unit

        service.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor))

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { repository.save(Image(id, image.data, image.mimeType, contributor, staticClock.now())) }
    }

    @Test
    fun `given an image is created, when the mime type is invalid, then an exception is thrown`() {
        val image = OrganizationController.RawImage(ImageData(ByteArray(0)), MimeType("application/json"))
        val contributor = ContributorId(UUID.randomUUID())

        shouldThrow<InvalidMimeType> {
            service.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }
}
