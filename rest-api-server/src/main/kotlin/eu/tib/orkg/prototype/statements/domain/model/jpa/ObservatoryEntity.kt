package eu.tib.orkg.prototype.statements.domain.model.jpa
import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import java.util.UUID
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

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var users: MutableSet<UserEntity>? = mutableSetOf()

    @Column(name = "display_id")
    var displayId: String? = null

    @Column(name = "topic_id")
    var topicId: Int? = null

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
            id = ObservatoryId(id!!),
            name = name,
            description = description,
            researchField = ResearchField(researchField, null),
            members = users!!.map(UserEntity::toContributor).toSet(),
            organizationIds = organizations!!.map { OrganizationId(it.id!!) }.toSet(),
            displayId = displayId,
            topicId = topicId
        )
}
