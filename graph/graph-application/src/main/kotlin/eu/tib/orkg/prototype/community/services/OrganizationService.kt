package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.files.api.CreateImageUseCase
import eu.tib.orkg.prototype.files.api.ImageUseCases
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.api.UpdateOrganizationUseCases
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrganizationService(
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val imageService: ImageUseCases
) : OrganizationUseCases {
    override fun create(
        id: OrganizationId?,
        organizationName: String,
        createdBy: ContributorId,
        url: String,
        displayId: String,
        type: OrganizationType,
        logoId: ImageId?
    ): OrganizationId {
        val organizationId = id ?: OrganizationId(UUID.randomUUID())
        val newOrganization = OrganizationEntity().apply {
            this.id = organizationId.value
            name = organizationName
            this.createdBy = createdBy.value
            this.url = url
            this.displayId = displayId
            this.type = type
            this.logoId = logoId?.value
        }
        postgresOrganizationRepository.save(newOrganization)
        return organizationId
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

    override fun updateOrganization(organization: Organization) {
        val entity = postgresOrganizationRepository.findById(organization.id!!.value).get()

        if (organization.name != entity.name)
            entity.name = organization.name

        if (organization.homepage != entity.url)
            entity.url = organization.homepage

        if (organization.type != entity.type)
            entity.type = organization.type

        postgresOrganizationRepository.save(entity)
    }

    override fun removeAll() = postgresOrganizationRepository.deleteAll()

    override fun findLogo(id: OrganizationId): Optional<Image> {
        val organization = postgresOrganizationRepository.findById(id.value)
            .orElseThrow { OrganizationNotFound(id) }
        return if (organization.logoId != null) imageService.find(ImageId(organization.logoId!!))
        else Optional.empty()
    }

    override fun updateLogo(id: OrganizationId, image: UpdateOrganizationUseCases.RawImage, contributor: ContributorId?) {
        val organization = postgresOrganizationRepository.findById(id.value)
            .orElseThrow { OrganizationNotFound(id) }
        val command = CreateImageUseCase.CreateCommand(image.data, image.mimeType, contributor)
        organization.logoId = imageService.create(command).value
        postgresOrganizationRepository.save(organization)
    }

    override fun update(
        contributorId: ContributorId,
        command: UpdateOrganizationUseCases.UpdateOrganizationRequest
    ) {
        val organization = postgresOrganizationRepository.findById(command.id.value)
            .orElseThrow { OrganizationNotFound(command.id) }
            .apply {
                name = command.name ?: name
                url = command.url ?: url
                type = command.type ?: type
                if (command.logo != null) {
                    logoId = imageService.create(
                        CreateImageUseCase.CreateCommand(
                            command.logo.data,
                            command.logo.mimeType,
                            contributorId
                        )
                    ).value
                }
            }
        postgresOrganizationRepository.save(organization)
    }
}
