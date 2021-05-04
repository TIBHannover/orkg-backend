package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional

interface OrganizationService {

    /**
     * Create a new organization with a given name.
     */
    fun create(OrganizationName: String, CreatedBy: ContributorId, Url: String, displayId: String): Organization

    fun listOrganizations(): List<Organization>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun updateOrganization(organization: Organization): Organization

    /**
     * Delete all organizations
     */
    fun removeAll()
}
