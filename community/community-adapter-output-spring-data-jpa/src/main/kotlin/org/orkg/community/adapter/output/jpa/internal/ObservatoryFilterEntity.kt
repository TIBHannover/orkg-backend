package org.orkg.community.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "observatory_filters")
class ObservatoryFilterEntity {
    @Id
    var id: UUID? = null

    @Column(name = "observatory_id")
    var observatoryId: UUID? = null

    var label: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(name = "created_at_offset_total_seconds")
    var createdAtOffsetTotalSeconds: Int? = null

    var path: String? = null

    var range: String? = null

    var exact: Boolean? = null

    var featured: Boolean? = null
}

fun ObservatoryFilterEntity.toObservatoryFilter() =
    ObservatoryFilter(
        id = ObservatoryFilterId(id!!),
        observatoryId = ObservatoryId(observatoryId!!),
        label = label!!,
        createdBy = ContributorId(createdBy!!),
        createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
        path = path!!.split(",").map(::ThingId),
        range = ThingId(range!!),
        exact = exact!!,
        featured = featured!!
    )
