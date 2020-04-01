package eu.tib.orkg.prototype.statements.infrastructure.jpa

import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresOrganizationService(
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : OrganizationService {
    override fun create(OrganizationName: String, OrganizationLogo: String): OrganizationEntity {
        val oId = UUID.randomUUID()
        val newOrganization = OrganizationEntity().apply {
            id = oId
            name = OrganizationName
            logo = OrganizationLogo
        }
        return postgresOrganizationRepository.save(newOrganization)
    }
}
