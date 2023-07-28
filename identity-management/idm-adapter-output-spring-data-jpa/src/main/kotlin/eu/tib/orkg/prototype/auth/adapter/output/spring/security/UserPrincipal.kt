package eu.tib.orkg.prototype.auth.adapter.output.spring.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class UserPrincipal(
    private val username: UUID,
    private val password: String,
    private val roles: MutableSet<GrantedAuthority>,
    private val enabled: Boolean = true,
    val displayName: String
) : UserDetails {
    override fun getAuthorities() = roles

    override fun getPassword() = password

    override fun getUsername() = username.toString()

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = enabled
}
