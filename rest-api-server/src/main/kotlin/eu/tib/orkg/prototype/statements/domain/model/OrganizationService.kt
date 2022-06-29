package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.OrganizationController
import java.util.Optional

interface OrganizationService {

    /**
     * Create a new organization with a given name.
     */
    fun create(organizationName: String, createdBy: ContributorId, url: String, displayId: String, type: OrganizationType, doi: String?): Organization

    fun createConference(organizationName: String, createdBy: ContributorId, url: String, displayId: String, type: OrganizationType, metadata: OrganizationController.Metadata, doi: String?): Organization

    fun listOrganizations(): List<Organization>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun listConferences(): List<Organization>

    fun updateOrganization(organization: Organization): Organization

    /**
     * Delete all organizations
     */
    fun removeAll()
}
