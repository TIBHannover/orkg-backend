package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.OrganizationUserMapperEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrgUserMapperRepository :
    JpaRepository<OrganizationUserMapperEntity, UUID> {
    fun findAllByUserId(id: UUID): List<OrganizationUserMapperEntity>
}
