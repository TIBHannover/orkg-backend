package org.orkg.testing

import org.springframework.boot.test.context.TestComponent
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@TestComponent("mockUserDetailsService")
class MockUserDetailsService : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        return when (username) {
            "user" -> activeUserWithId(MockUserId.USER)
            "curator" -> activeCuratorWithId(MockUserId.CURATOR)
            "admin" -> activeAdminWithId(MockUserId.ADMIN)
            else -> throw UsernameNotFoundException("No test user defined with name \"$username\". Feel free to add it.")
        }
    }

    private fun activeUserWithId(userId: String): UserDetails =
        createUserWithId(userId, "ROLE_USER")

    private fun activeCuratorWithId(userId: String): UserDetails =
        createUserWithId(userId, "ROLE_CURATOR")

    private fun activeAdminWithId(userId: String): UserDetails =
        createUserWithId(userId, "ROLE_ADMIN")

    private fun createUserWithId(userId: String, role: String): UserDetails {
        return User
            .withUsername(userId)
            .password("invalid, not a hash")
            .authorities(role)
            .build()
    }
}
