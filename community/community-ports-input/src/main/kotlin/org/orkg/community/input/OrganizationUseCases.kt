package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.domain.RawImage
import java.util.Optional

interface UpdateOrganizationUseCases {
    fun update(contributorId: ContributorId, command: UpdateOrganizationCommand)

    data class UpdateOrganizationCommand(
        val id: OrganizationId,
        val name: String?,
        val url: String?,
        val type: OrganizationType?,
        val logo: RawImage?,
    )
}

interface OrganizationUseCases : UpdateOrganizationUseCases {
    /**
     * Create a new organization with a given name.
     */
    fun create(
        id: OrganizationId?,
        organizationName: String,
        createdBy: ContributorId,
        url: String,
        displayId: String,
        type: OrganizationType,
        logoId: ImageId?,
    ): OrganizationId

    fun findAll(): List<Organization>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun findAllConferences(): List<Organization>

    fun findLogoById(id: OrganizationId): Optional<Image>

    /**
     * Delete all organizations
     */
    fun deleteAll()
}
