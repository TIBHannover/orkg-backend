package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional
import java.util.UUID

interface OrganizationServiceNeo {

    /**
     * Create a new organization with a given name.
     */
    fun create(OrganizationName: String, CreatedBy: ContributorId, Url: String): Organization

    fun create(id: UUID, OrganizationName: String, CreatedBy: ContributorId, Url: String): Organization

    fun listOrganizations(): List<Organization>

    fun findById(id: OrganizationId): Optional<Organization>

    fun updateOrganization(organization: Organization): Organization
}
