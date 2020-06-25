package eu.tib.orkg.prototype.statements.infrastructure.jpa

import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresOrganizationService(
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : OrganizationService {
    override fun create(OrganizationName: String, CreatedBy: UUID, Url: String): Organization {
        val organizationId = UUID.randomUUID()
        val newOrganization = OrganizationEntity().apply {
            id = organizationId
            name = OrganizationName
            createdBy = CreatedBy
            url = Url
        }
        return postgresOrganizationRepository.save(newOrganization).toOrganization()
    }

    override fun listOrganizations(): List<Organization> {
        return postgresOrganizationRepository.findAll()
            .map(OrganizationEntity::toOrganization)
    }

    override fun findById(id: UUID): Optional<OrganizationEntity> {
        return postgresOrganizationRepository.findById(id)
    }
}
