package org.orkg.community.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
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
                            command.logo!!.data,
                            command.logo!!.mimeType,
                            contributorId
                        )
                    ).value
                }
            }
        postgresOrganizationRepository.save(organization)
    }
}
