package org.orkg.auth.domain

data class Role(val name: String) {
    companion object {
        val ADMIN = Role("ROLE_ADMIN")
        val CURATOR = Role("ROLE_ADMIN") // FIXME: should be its own status
        val USER = Role("ROLE_USER")
    }
}
