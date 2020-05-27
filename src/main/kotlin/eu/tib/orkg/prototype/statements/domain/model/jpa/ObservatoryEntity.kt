package eu.tib.orkg.prototype.statements.domain.model.jpa
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.persistence.ManyToMany


@Entity
@Table(name = "observatories")
class ObservatoryEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var users: Set<UserEntity>? = null

    @ManyToMany(mappedBy = "observatories")
    var organizations: MutableCollection<OrganizationEntity> = mutableSetOf()


    fun toObservatory() = Observatory(id, name, organizations)
}
