package org.orkg.mediastorage.adapter.output.jpa.internal

import org.springframework.util.MimeType
import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.orkg.common.ContributorId
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId

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
        mimeType = MimeType.valueOf(mimeType!!),
        createdBy = if (createdBy != null) ContributorId(createdBy!!) else null,
        createdAt = createdAt!!
    )
}
