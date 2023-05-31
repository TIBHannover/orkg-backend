package eu.tib.orkg.prototype.community.api

import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.api.UpdateOrganizationUseCases
import java.util.*

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
