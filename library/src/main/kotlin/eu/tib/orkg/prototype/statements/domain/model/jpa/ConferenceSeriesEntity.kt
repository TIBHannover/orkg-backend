package eu.tib.orkg.prototype.statements.domain.model.jpa

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.statements.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.statements.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.statements.domain.model.Metadata
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PeerReviewType
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import java.time.LocalDate
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Table(name = "conferences_series")
class ConferenceSeriesEntity() {

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
