package eu.tib.orkg.prototype.statements.domain.model.jpa

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Metadata
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.OneToOne
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import java.time.LocalDate
import javax.persistence.EnumType
import javax.persistence.Enumerated

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

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", referencedColumnName = "id", insertable = false, updatable = false)
    @PrimaryKeyJoinColumn
    var metadata: ConferenceMetadataEntity? = null

    fun toOrganization() =
        Organization(
            id = OrganizationId(id!!),
            name = name,
            logo = null,
            createdBy = ContributorId(createdBy!!),
            homepage = url,
            observatoryIds = observatories!!.map { ObservatoryId(it.id!!) }.toSet(),
            displayId = displayId,
            type = type!!,
            metadata = Metadata(metadata?.date, metadata?.isDoubleBlind)
        )
}

@Entity
@Table(name = "conferences_metadata")
class ConferenceMetadataEntity() {
    @Id
    @Column(name = "organization_id", unique = true, nullable = false)
    var id: UUID? = null

    var date: LocalDate? = null

    @Column(name = "is_double_blind")
    var isDoubleBlind: Boolean? = false

    @OneToOne(mappedBy = "metadata", fetch = FetchType.LAZY)
    var organization: OrganizationEntity? = null
}
