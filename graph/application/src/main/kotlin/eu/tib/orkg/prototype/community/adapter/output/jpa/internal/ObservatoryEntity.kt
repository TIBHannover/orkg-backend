package eu.tib.orkg.prototype.community.adapter.output.jpa.internal
import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.UserEntity
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "observatories")
class ObservatoryEntity() {
    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    var description: String? = null

    @Column(name = "research_field")
    var researchField: String? = null

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "observatory_id")
    var users: MutableSet<UserEntity>? = null

    @Column(name = "display_id")
    var displayId: String? = null

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "observatories_organizations",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")]
    )
    var organizations: MutableSet<OrganizationEntity>? = mutableSetOf()
}

fun ObservatoryEntity.toObservatory() =
    Observatory(
        id = ObservatoryId(id!!),
        name = name!!,
        description = description,
        researchField = researchField?.let { ThingId(it) },
        members = users.orEmpty().map { ContributorId(it.id!!) }.toSet(),
        organizationIds = organizations.orEmpty().map { OrganizationId(it.id!!) }.toSet(),
        displayId = displayId!!
    )

fun User.toContributor() =
    Contributor(
        id = ContributorId(this.id),
        name = this.displayName,
        email = this.email,
        joinedAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
        organizationId = this.organizationId?.let(::OrganizationId) ?: OrganizationId.createUnknownOrganization(),
        observatoryId = this.observatoryId?.let(::ObservatoryId) ?: ObservatoryId.createUnknownObservatory(),
    )
