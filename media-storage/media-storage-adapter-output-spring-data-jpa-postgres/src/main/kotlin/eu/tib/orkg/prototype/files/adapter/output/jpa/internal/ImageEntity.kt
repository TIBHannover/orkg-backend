package eu.tib.orkg.prototype.files.adapter.output.jpa.internal

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import java.time.OffsetDateTime
import java.util.*
import javax.activation.MimeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "images")
class ImageEntity {
    @Id
    var id: UUID? = null

    @NotEmpty
    @Column(nullable = false)
    var data: ByteArray? = null

    @NotBlank
    @Column(name = "mime_type", nullable = false)
    var mimeType: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    fun toImage() = Image(
        id = ImageId(id!!),
        data = ImageData(data!!),
        mimeType = MimeType(mimeType!!),
        createdBy = if (createdBy != null) ContributorId(createdBy!!) else null,
        createdAt = createdAt!!
    )
}
