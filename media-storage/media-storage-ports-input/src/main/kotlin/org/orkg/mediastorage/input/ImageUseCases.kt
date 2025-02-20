package org.orkg.mediastorage.input

import org.orkg.common.ContributorId
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId
import org.springframework.util.MimeType
import java.util.Optional

interface ImageUseCases :
    CreateImageUseCase,
    RetrieveImageUseCase

interface RetrieveImageUseCase {
    fun findById(id: ImageId): Optional<Image>
}

interface CreateImageUseCase {
    fun create(command: CreateCommand): ImageId

    data class CreateCommand(
        val data: ImageData,
        val mimeType: MimeType,
        val createdBy: ContributorId?,
    )
}
