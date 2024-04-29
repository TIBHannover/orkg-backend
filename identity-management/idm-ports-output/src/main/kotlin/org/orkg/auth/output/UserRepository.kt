package org.orkg.auth.output

import java.util.*
import org.orkg.auth.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRepository {
    fun save(user: User)

    fun findByEmailIgnoreCase(email: String): Optional<User>

    fun findById(id: UUID): Optional<User>

    fun deleteAll()

    fun findAll(pageable: Pageable): Page<User>

    fun count(): Long
}
