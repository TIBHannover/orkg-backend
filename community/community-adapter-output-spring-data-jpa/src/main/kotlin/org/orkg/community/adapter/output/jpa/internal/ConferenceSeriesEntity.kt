package org.orkg.community.adapter.output.jpa.internal

import java.time.LocalDate
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Metadata
import org.orkg.community.domain.PeerReviewType

@Entity
@Table(name = "conferences_series")
class ConferenceSeriesEntity {

    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    var url: String? = null

    @Column(name = "display_id")
    var displayId: String? = null

    @Column(name = "start_date")
    var startDate: LocalDate? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type")
    var reviewType: PeerReviewType? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    var organization: OrganizationEntity? = null

    fun toConferenceSeries() =
        ConferenceSeries(
            organizationId = OrganizationId(organization?.id!!),
            id = ConferenceSeriesId(id!!),
            name = name!!,
            homepage = url!!,
            displayId = displayId!!,
            metadata = Metadata(startDate!!, reviewType!!)
        )
}
