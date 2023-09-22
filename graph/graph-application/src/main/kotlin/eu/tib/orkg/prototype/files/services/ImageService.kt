package eu.tib.orkg.prototype.files.services

import eu.tib.orkg.prototype.files.api.CreateImageUseCase
import eu.tib.orkg.prototype.files.api.ImageUseCases
import eu.tib.orkg.prototype.files.application.InvalidMimeType
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.files.spi.ImageRepository
import java.time.Clock
import java.time.OffsetDateTime
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val repository: ImageRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ImageUseCases {
    override fun create(command: CreateImageUseCase.CreateCommand): ImageId {
        if (command.mimeType.primaryType != "image") {
            throw InvalidMimeType(command.mimeType)
        }
        val uuid = repository.nextIdentity()
        val image = Image(uuid, command.data, command.mimeType, command.createdBy, OffsetDateTime.now(clock))
        repository.save(image)
        return uuid
    }

    override fun find(id: ImageId) =
        repository.findById(id)
}
