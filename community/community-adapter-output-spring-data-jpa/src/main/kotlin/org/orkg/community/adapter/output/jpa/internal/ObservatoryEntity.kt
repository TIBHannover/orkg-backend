package org.orkg.community.adapter.output.jpa.internal

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import org.orkg.auth.adapter.output.jpa.internal.UserEntity
import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Observatory

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

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "observatories_organizations",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")]
    )
    var organizations: MutableSet<OrganizationEntity>? = mutableSetOf()

    @ElementCollection
    @CollectionTable(
        name = "observatories_sdgs",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")]
    )
    @Column(name = "sdg_id")
    var sustainableDevelopmentGoals: MutableSet<String>? = mutableSetOf()
}

fun ObservatoryEntity.toObservatory() =
    Observatory(
        id = ObservatoryId(id!!),
        name = name!!,
        description = description,
        researchField = researchField?.let { ThingId(it) },
        members = users.orEmpty().map { ContributorId(it.id!!) }.toSet(),
        organizationIds = organizations.orEmpty().map { OrganizationId(it.id!!) }.toSet(),
        displayId = displayId!!,
        sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty().map(::ThingId).toSet()
    )

// TODO: should be internal, but is used by input adapter (legacy controller)
fun User.toContributor() =
    Contributor(
        id = ContributorId(this.id),
        name = this.displayName,
        email = this.email,
        joinedAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
        organizationId = this.organizationId?.let(::OrganizationId) ?: OrganizationId.UNKNOWN,
        observatoryId = this.observatoryId?.let(::ObservatoryId) ?: ObservatoryId.UNKNOWN,
        isCurator = (roles intersect setOf(Role.CURATOR, Role.ADMIN)).isNotEmpty(), // FIXME: might need changing when split
        isAdmin = Role.ADMIN in this.roles,
    )
