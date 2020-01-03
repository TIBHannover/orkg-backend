package eu.tib.orkg.prototype.statements.auth

import org.springframework.boot.test.context.TestComponent
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@TestComponent("mockUserDetailsService")
class MockUserDetailsService : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        return when (username) {
            "user" -> activeUserWithId("b7c81eed-52e1-4f7a-93bf-e6d331b8df7b")
            else -> throw UsernameNotFoundException("No test user defined with name \"$username\". Feel free to add it.")
        }
    }

    private fun activeUserWithId(userId: String): UserDetails {
        return User
            .withUsername(userId)
            .password("invalid, not a hash")
            .authorities(emptySet())
            .build()
    }
}
