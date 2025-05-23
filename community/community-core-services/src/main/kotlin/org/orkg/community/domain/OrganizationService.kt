package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.community.output.OrganizationRepository
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
@TransactionalOnJPA
class OrganizationService(
    private val postgresOrganizationRepository: OrganizationRepository,
    private val imageService: ImageUseCases,
) : OrganizationUseCases {
    override fun create(
        id: OrganizationId?,
        organizationName: String,
        createdBy: ContributorId,
        url: String,
        displayId: String,
        type: OrganizationType,
        logoId: ImageId?,
    ): OrganizationId {
        val organizationId = id ?: OrganizationId(UUID.randomUUID())
        val newOrganization = Organization(
            id = organizationId,
            name = organizationName,
            createdBy = createdBy,
            homepage = url,
            displayId = displayId,
            type = type,
            logoId = logoId,
        )
        postgresOrganizationRepository.save(newOrganization)
        return organizationId
    }

    override fun findAll(): List<Organization> =
        postgresOrganizationRepository.findAllByType(OrganizationType.GENERAL)

    override fun findAllConferences(): List<Organization> =
        postgresOrganizationRepository.findAllByType(OrganizationType.CONFERENCE)

    override fun findById(id: OrganizationId): Optional<Organization> =
        postgresOrganizationRepository.findById(id)

    override fun findByName(name: String): Optional<Organization> =
        postgresOrganizationRepository.findByName(name)

    override fun findByDisplayId(name: String): Optional<Organization> =
        postgresOrganizationRepository.findByDisplayId(name)

    override fun deleteAll() = postgresOrganizationRepository.deleteAll()

    override fun findLogoById(id: OrganizationId): Optional<Image> {
        val organization = postgresOrganizationRepository.findById(id)
            .orElseThrow { OrganizationNotFound(id) }
        return if (organization.logoId != null) {
            imageService.findById(ImageId(organization.logoId!!.value))
        } else {
            Optional.empty()
        }
    }

    override fun update(
        contributorId: ContributorId,
        command: UpdateOrganizationUseCases.UpdateOrganizationCommand,
    ) {
        val organization = postgresOrganizationRepository.findById(command.id)
            .orElseThrow { OrganizationNotFound(command.id) }
            .apply {
                name = command.name ?: name
                homepage = command.url ?: homepage
                type = command.type ?: type
                if (command.logo != null) {
                    logoId = imageService.create(
                        CreateImageUseCase.CreateCommand(
                            command.logo!!.data,
                            command.logo!!.mimeType,
                            contributorId
                        )
                    )
                }
            }
        postgresOrganizationRepository.save(organization)
    }
}
