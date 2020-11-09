package eu.tib.orkg.prototype.statements.domain.model.jpa
import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "observatories")
class ObservatoryEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    var description: String? = null

    @Column(name = "research_field")
    var researchField: String? = null

    @ManyToMany
    @JoinTable(
        name = "observatory_members",
        joinColumns = [JoinColumn(name = "observatory_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var members: MutableSet<UserEntity>? = mutableSetOf()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "observatories_organizations",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")]
    )
    var organizations: MutableSet<OrganizationEntity>? = mutableSetOf()

    fun toObservatory() =
        Observatory(
            id = id,
            name = name,
            description = description,
            researchField = researchField,
            members = members!!.map(UserEntity::toContributor).map(Contributor::id).toSet(),
            organizationIds = organizations!!.mapNotNull(OrganizationEntity::id).toSet()
        )
}
