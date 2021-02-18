package eu.tib.orkg.prototype.statements.infrastructure.jpa

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
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
    override fun create(OrganizationName: String, CreatedBy: ContributorId, Url: String, uriName: String): Organization {
        val organizationId = UUID.randomUUID()
        val newOrganization = OrganizationEntity().apply {
            id = organizationId
            name = OrganizationName
            createdBy = CreatedBy.value
            url = Url
            this.uriName = uriName
        }
        return postgresOrganizationRepository.save(newOrganization).toOrganization()
    }

    override fun listOrganizations(): List<Organization> {
        return postgresOrganizationRepository.findAll()
            .map(OrganizationEntity::toOrganization)
    }

    override fun findById(id: OrganizationId): Optional<Organization> =
        postgresOrganizationRepository
            .findById(id.value)
            .map(OrganizationEntity::toOrganization)

    override fun findByName(name: String): Optional<Organization> =
        postgresOrganizationRepository
            .findByName(name)
            .map(OrganizationEntity::toOrganization)

    override fun findByUriName(name: String): Optional<Organization> =
        postgresOrganizationRepository
            .findByUriName(name)
            .map(OrganizationEntity::toOrganization)

    override fun updateOrganization(organization: Organization): Organization {
        val entity = postgresOrganizationRepository.findById(organization.id!!.value).get()

        if (organization.name != entity.name)
            entity.name = organization.name

        if (organization.homepage != entity.url)
            entity.url = organization.homepage

        return postgresOrganizationRepository.save(entity).toOrganization()
    }
}
