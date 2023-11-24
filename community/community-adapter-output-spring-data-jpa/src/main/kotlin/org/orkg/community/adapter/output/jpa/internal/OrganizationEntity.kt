package org.orkg.community.adapter.output.jpa.internal

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.mediastorage.domain.ImageId

@Entity
@Table(name = "organizations")
class OrganizationEntity() {

    constructor(id: UUID) : this() {
        this.id = id
    }

    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    @Column(name = "created_by")
    var createdBy: UUID? = null

    var url: String? = null

    @Column(name = "display_id")
    var displayId: String? = null

    @Enumerated(EnumType.STRING)
    var type: OrganizationType? = null

    @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY)
    var observatories: MutableSet<ObservatoryEntity>? = mutableSetOf()

    @OneToMany
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    var conferenceSeries: MutableSet<ConferenceSeriesEntity>? = mutableSetOf()

    @Column(name = "logo_id")
    var logoId: UUID? = null

    fun toOrganization() =
        Organization(
            id = OrganizationId(id!!),
            name = name,
            createdBy = ContributorId(createdBy!!),
            homepage = url,
            observatoryIds = observatories!!.map { ObservatoryId(it.id!!) }.toSet(),
            displayId = displayId,
            type = type!!,
            logoId = if (logoId != null) ImageId(logoId!!) else null
        )
}
