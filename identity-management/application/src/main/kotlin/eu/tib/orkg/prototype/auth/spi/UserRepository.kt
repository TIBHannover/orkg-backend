package eu.tib.orkg.prototype.auth.spi

import eu.tib.orkg.prototype.auth.domain.User
import java.util.*

typealias UserId = UUID
typealias ObservatoryId = UUID
typealias OrganizationId = UUID

interface UserRepository {
    fun save(user: User)

    fun findByEmailIgnoreCase(email: String): Optional<User>

    fun findById(id: UserId): Optional<User>

    fun deleteAll()
}
