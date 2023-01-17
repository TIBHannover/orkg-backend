package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrganizationService(
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : OrganizationUseCases {
    override fun create(organizationName: String, createdBy: ContributorId, url: String, displayId: String, type: OrganizationType): Organization {
        return createOrganization(organizationName, createdBy, url, displayId, type).toOrganization()
    }
    override fun listOrganizations(): List<Organization> {
        return postgresOrganizationRepository
            .findByType(OrganizationType.GENERAL)
            .map(OrganizationEntity::toOrganization)
    }

    override fun listConferences(): List<Organization> =
        postgresOrganizationRepository
            .findByType(OrganizationType.CONFERENCE)
            .map(OrganizationEntity::toOrganization)

    override fun findById(id: OrganizationId): Optional<Organization> =
        postgresOrganizationRepository
            .findById(id.value)
            .map(OrganizationEntity::toOrganization)

    override fun findByName(name: String): Optional<Organization> =
        postgresOrganizationRepository
            .findByName(name)
            .map(OrganizationEntity::toOrganization)

    override fun findByDisplayId(name: String): Optional<Organization> =
        postgresOrganizationRepository
            .findByDisplayId(name)
            .map(OrganizationEntity::toOrganization)

    override fun updateOrganization(organization: Organization): Organization {
        val entity = postgresOrganizationRepository.findById(organization.id!!.value).get()

        if (organization.name != entity.name)
            entity.name = organization.name

        if (organization.homepage != entity.url)
            entity.url = organization.homepage

        if (organization.type != entity.type)
            entity.type = organization.type

        return postgresOrganizationRepository.save(entity).toOrganization()
    }

    override fun removeAll() = postgresOrganizationRepository.deleteAll()

    private fun createOrganization(organizationName: String, createdBy: ContributorId, Url: String, displayId: String, type: OrganizationType): OrganizationEntity {
        val organizationId = UUID.randomUUID()
        val newOrganization = OrganizationEntity().apply {
            id = organizationId
            name = organizationName
            this.createdBy = createdBy.value
            url = Url
            this.displayId = displayId
            this.type = type
        }
        return postgresOrganizationRepository.save(newOrganization)
    }
}
