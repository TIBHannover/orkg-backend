package org.orkg.community.adapter.output.jpa.internal

import org.orkg.community.domain.ObservatoryFilter
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.ObservatoryFilterId

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
    var createdAt: LocalDateTime? = null

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
        createdAt = createdAt!!,
        path = path!!.split(",").map(::ThingId),
        range = ThingId(range!!),
        exact = exact!!,
        featured = featured!!
    )
