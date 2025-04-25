package org.orkg.contenttypes.adapter.output.jpa.internal

import com.fasterxml.jackson.databind.JsonNode
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type
import java.time.OffsetDateTime
import java.util.UUID

@MappedSuperclass
abstract class SnapshotEntity<V : Enum<V>> {
    @Id
    var id: String? = null

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null

    @Column(name = "created_at_offset_total_seconds")
    var createdAtOffsetTotalSeconds: Int? = null

    var createdBy: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "model_version")
    var modelVersion: V? = null

    @Type(JsonType::class)
    var data: JsonNode? = null
}
