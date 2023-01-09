package eu.tib.orkg.prototype.statements.domain.model.jpa

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.OneToMany

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

    fun toOrganization() =
        Organization(
            id = OrganizationId(id!!),
            name = name,
            logo = null,
            createdBy = ContributorId(createdBy!!),
            homepage = url,
            observatoryIds = observatories!!.map { ObservatoryId(it.id!!) }.toSet(),
            displayId = displayId,
            type = type!!
        )
}
