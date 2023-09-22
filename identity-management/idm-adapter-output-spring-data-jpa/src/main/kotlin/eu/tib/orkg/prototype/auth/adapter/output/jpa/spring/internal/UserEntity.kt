package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal

import eu.tib.orkg.prototype.auth.domain.User
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

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
    var created: LocalDateTime = LocalDateTime.now()

    @Column(name = "organization_id")
    var organizationId: UUID? = null

    @Column(name = "observatory_id")
    var observatoryId: UUID? = null

    @ManyToMany
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
        createdAt = this.created,
        roles = this.roles.map(RoleEntity::toRole).toSet(),
        organizationId = this.organizationId,
        observatoryId = this.observatoryId,
    )
}
