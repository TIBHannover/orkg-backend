package org.orkg.mediastorage.domain

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.activation.MimeType
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.output.ImageRepository
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage

class ImageServiceTest {
    private val repository: ImageRepository = mockk()
    private val fixedTime = OffsetDateTime.of(2022, 11, 14, 14, 9, 23, 12345, ZoneOffset.ofHours(1))
    private val staticClock = java.time.Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())
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
        verify(exactly = 1) {
            repository.save(
                Image(
                    id,
                    image.data,
                    image.mimeType,
                    contributor,
                    OffsetDateTime.now(staticClock),
                )
            )
        }
    }

    @Test
    fun `given an image is created, when the mime type is invalid, then an exception is thrown`() {
        val image = UpdateOrganizationUseCases.RawImage(ImageData(ByteArray(0)), MimeType("application/json"))
        val contributor = ContributorId(UUID.randomUUID())

        shouldThrow<InvalidMimeType> {
            service.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }
}
