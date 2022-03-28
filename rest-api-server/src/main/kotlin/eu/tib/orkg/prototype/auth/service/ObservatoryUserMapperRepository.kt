package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.ObservatoryUserMapperEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ObservatoryUserMapperRepository :
    JpaRepository<ObservatoryUserMapperEntity, UUID> {
    fun findAllByUserId(id: UUID): List<ObservatoryUserMapperEntity>
}
