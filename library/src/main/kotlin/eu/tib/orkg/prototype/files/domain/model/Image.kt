package eu.tib.orkg.prototype.files.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime
import javax.activation.MimeType

data class Image(
    val id: ImageId,
    val data: ImageData,
    val mimeType: MimeType,
    val createdBy: ContributorId?,
    val createdAt: OffsetDateTime
)
