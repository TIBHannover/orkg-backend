package eu.tib.orkg.prototype.auth.persistence

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    @Email
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

    fun toContributor() = Contributor(
        id = this.id!!,
        name = this.displayName!!,
        joinedAt = OffsetDateTime.of(this.created, UTC),
        organizationId = this.organizationId ?: UUID(0, 0),
        observatoryId = this.observatoryId ?: UUID(0, 0),
        email = this.email!!
    )
}

@Entity
@Table(name = "roles")
open class RoleEntity {
    @Id
    @Column(name = "role_id", nullable = false)
    open var id: Int? = null

    @NotBlank
    @Column(name = "name", nullable = false)
    open var name: String? = null

    @ManyToMany(mappedBy = "roles")
    private var users: MutableSet<UserEntity> = mutableSetOf()
}

/**
 * Decorator for user entities.
 */
data class UserPrincipal(private val userEntity: UserEntity) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        userEntity.roles
            .map(RoleEntity::name)
            .map(::SimpleGrantedAuthority)
            .toMutableSet()

    override fun isEnabled() = userEntity.enabled

    override fun getUsername() = userEntity.id.toString()

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = userEntity.password!!

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    val displayName get() = userEntity.displayName!!
}
