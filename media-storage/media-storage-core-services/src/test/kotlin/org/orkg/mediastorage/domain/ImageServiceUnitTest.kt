package org.orkg.mediastorage.domain

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.output.ImageRepository
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage
import org.springframework.util.MimeType

internal class ImageServiceUnitTest : MockkBaseTest {
    private val repository: ImageRepository = mockk()
    private val service = ImageService(repository, fixedClock)

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
                    OffsetDateTime.now(fixedClock),
                )
            )
        }
    }

    @Test
    fun `given an image is created, when the mime type is invalid, then an exception is thrown`() {
        val image = UpdateOrganizationUseCases.RawImage(ImageData(ByteArray(0)), MimeType.valueOf("application/json"))
        val contributor = ContributorId(UUID.randomUUID())

        shouldThrow<InvalidMimeType> {
            service.create(CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }
}
