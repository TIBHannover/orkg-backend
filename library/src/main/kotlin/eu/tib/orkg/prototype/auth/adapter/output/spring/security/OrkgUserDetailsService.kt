package eu.tib.orkg.prototype.auth.adapter.output.spring.security

import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.auth.spi.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("userDetailsService")
@Transactional
class OrkgUserDetailsService(
    private val service: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = service.findByEmailIgnoreCase(username)
        if (user.isPresent) {
            return user.get().toUserPrincipal()
        }
        throw UsernameNotFoundException("No user found with email $username")
    }

    private fun User.toUserPrincipal(): UserDetails =
        UserPrincipal(
            username = id,
            password = password,
            roles = roles.map { SimpleGrantedAuthority(it.name) }.toMutableSet(),
            enabled = enabled,
            displayName = displayName
        )
}
