package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal

import eu.tib.orkg.prototype.auth.domain.Role
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "roles")
class RoleEntity {
    @Id
    @Column(name = "role_id", nullable = false)
    var id: Int? = null

    @NotBlank
    @Column(name = "name", nullable = false)
    var name: String? = null

    @Suppress("unused") // Not currently used, but necessary for JPA mapping
    @ManyToMany(mappedBy = "roles")
    private var users: MutableSet<UserEntity> = mutableSetOf()

    fun toRole(): Role = Role(name = this.name!!)
}
