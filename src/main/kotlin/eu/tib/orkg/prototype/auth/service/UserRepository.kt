package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    override fun findById(id: UUID): Optional<UserEntity>

    @Query(value = "SELECT * FROM users INNER JOIN userobservatories uo on users.id = uo.user_id where uo.observatory_id=:id", nativeQuery = true)
    fun findUsersByObservatoryId(id: UUID): Iterable<UserEntity>
}
