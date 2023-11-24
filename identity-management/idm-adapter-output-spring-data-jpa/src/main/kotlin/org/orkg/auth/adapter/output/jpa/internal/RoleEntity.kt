package org.orkg.auth.adapter.output.jpa.internal

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import org.orkg.auth.domain.Role

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
