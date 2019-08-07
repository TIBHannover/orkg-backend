package eu.tib.orkg.prototype.auth.persistence

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
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
    var email: String? = null

    @NotBlank
    var password: String? = null

    @NotNull
    var enabled: Boolean = false

    @NotNull
    var created: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var roles: MutableCollection<RoleEntity> = mutableSetOf()
}

@Entity
@Table(name = "roles")
open class RoleEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    var name: String? = null
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

    override fun getUsername() = userEntity.email

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = userEntity.password!!

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true
}
