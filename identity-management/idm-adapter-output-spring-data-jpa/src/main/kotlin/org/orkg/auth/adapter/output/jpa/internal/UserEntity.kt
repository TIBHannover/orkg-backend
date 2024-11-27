package org.orkg.auth.adapter.output.jpa.internal

import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.orkg.auth.domain.User

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    @Email
    @Column(columnDefinition = "citext")
    var email: String? = null

    @NotBlank
    var password: String? = null

    @NotBlank
    @Column(name = "display_name")
    var displayName: String? = "an anonymous user"

    @NotNull
    var enabled: Boolean = false

    @NotNull
    var created: LocalDateTime? = null

    @Column(name = "organization_id")
    var organizationId: UUID? = null

    @Column(name = "observatory_id")
    var observatoryId: UUID? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<RoleEntity> = mutableSetOf()

    @PrePersist
    @PreUpdate
    private fun validate() {
        email = email?.lowercase()
    }

    fun toUser() = User(
        id = this.id!!,
        email = this.email!!,
        displayName = this.displayName!!,
        password = password!!,
        enabled = this.enabled,
        createdAt = this.created!!,
        roles = this.roles.map(RoleEntity::toRole).toSet(),
        organizationId = this.organizationId,
        observatoryId = this.observatoryId,
    )
}
