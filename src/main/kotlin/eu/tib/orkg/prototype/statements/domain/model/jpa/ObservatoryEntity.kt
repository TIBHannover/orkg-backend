package eu.tib.orkg.prototype.statements.domain.model.jpa
import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import java.util.UUID
import javax.persistence.CascadeType
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
class ObservatoryEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    var description: String? = null

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var users: Set<UserEntity>? = mutableSetOf()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "observatories_organizations",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")])
    var organizations: Set<OrganizationEntity>? = mutableSetOf()

    fun toObservatory() = Observatory(id, name, description, users, organizations)
}
