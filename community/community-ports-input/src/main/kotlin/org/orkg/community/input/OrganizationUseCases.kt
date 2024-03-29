package org.orkg.community.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId

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
        logoId: ImageId?
    ): OrganizationId

    fun listOrganizations(): List<Organization>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun listConferences(): List<Organization>

    fun updateOrganization(organization: Organization)

    fun findLogo(id: OrganizationId): Optional<Image>

    fun updateLogo(id: OrganizationId, image: UpdateOrganizationUseCases.RawImage, contributor: ContributorId?)

    /**
     * Delete all organizations
     */
    fun removeAll()
}
