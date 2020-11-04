package eu.tib.orkg.prototype.auth.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("userDetailsService")
@Transactional
class OrkgUserDetailsService(
    private val repository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = repository.findByEmail(username)
        if (user.isPresent) {
            return user.get().toUserPrincipal()
        }
        throw UsernameNotFoundException("No user found with email $username")
    }
}
