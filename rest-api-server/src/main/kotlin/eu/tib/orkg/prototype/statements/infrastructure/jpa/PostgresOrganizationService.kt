package eu.tib.orkg.prototype.statements.infrastructure.jpa

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.OrganizationController
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import eu.tib.orkg.prototype.statements.domain.model.jpa.ConferenceMetadataEntity
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
    override fun create(organizationName: String, createdBy: ContributorId, url: String, displayId: String, type: OrganizationType): Organization {
        return createOrganization(organizationName, createdBy, url, displayId, type).toOrganization()
    }

    override fun createConference(organizationName: String, createdBy: ContributorId, url: String, displayId: String, type: OrganizationType, metadata: OrganizationController.Metadata): Organization {
        val organization = createOrganization(organizationName, createdBy, url, displayId, type)
        organization.metadata = ConferenceMetadataEntity().apply {
                id = organization.id
                date = metadata.date
                isDoubleBlind = metadata.isDoubleBlind
            }
        return postgresOrganizationRepository.save(organization).toOrganization()
    }

    override fun listOrganizations(): List<Organization> {
        return postgresOrganizationRepository.findAll()
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

        if (organization.metadata?.date != entity.metadata?.date)
            entity.metadata?.date = organization.metadata?.date

        if (organization.metadata?.isDoubleBlind != entity.metadata?.isDoubleBlind)
            entity.metadata?.isDoubleBlind = organization.metadata?.isDoubleBlind

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
