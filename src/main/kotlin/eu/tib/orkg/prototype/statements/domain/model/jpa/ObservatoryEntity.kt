package eu.tib.orkg.prototype.statements.domain.model.jpa
import com.fasterxml.jackson.annotation.JsonManagedReference
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.User
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
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

    @Column(name = "organization_id")
    var organizationId: UUID? = null

    @JsonManagedReference
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "userobservatories",
        joinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: Set<UserEntity>? = null

    fun toObservatory() = Observatory(id, name, organizationId)

    fun toUser() = User(users)
}
