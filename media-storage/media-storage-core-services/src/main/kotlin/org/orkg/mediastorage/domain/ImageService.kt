package org.orkg.mediastorage.domain

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.mediastorage.output.ImageRepository
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val repository: ImageRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ImageUseCases {
    override fun create(command: CreateImageUseCase.CreateCommand): ImageId {
        if (command.mimeType.type != "image") {
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
