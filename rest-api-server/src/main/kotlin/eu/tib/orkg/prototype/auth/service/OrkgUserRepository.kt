package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.ORKGUserEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface OrkgUserRepository: JpaRepository<ORKGUserEntity, UUID> {
    fun findAllByOrganizationId(id: UUID): List<ORKGUserEntity>
    fun findAllByObservatoryId(id: UUID): List<ORKGUserEntity>

    @Query("SELECT u FROM ORKGUserEntity u WHERE u.id in ?1")
    fun findByIdIn(@Param("ids")ids: Array<UUID>): List<ORKGUserEntity>

    @Query("SELECT u FROM ORKGUserEntity u WHERE u.oldID in ?1")
    fun findByOldIDIn(@Param("ids")ids: Array<UUID>): List<ORKGUserEntity>

    fun findByOldID(id: UUID): ORKGUserEntity

    fun findByKeycloakID(id: UUID): Optional<ORKGUserEntity>

    fun findByDisplayName(name: String): Optional<ORKGUserEntity>

    fun findByEmail(email: String): Optional<ORKGUserEntity>

    @Query("SELECT u from ORKGUserEntity u WHERE u.oldID in ?1 OR u.keycloakID in ?1")
    fun findUserInOldIDOrKeycloakID(id:UUID): Optional<ORKGUserEntity>

}
