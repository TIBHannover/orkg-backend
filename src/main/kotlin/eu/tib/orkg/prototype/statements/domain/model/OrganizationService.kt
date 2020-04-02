package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity

interface OrganizationService {

    /**
     * Create a new company with a given name.
     *
     */
    fun create(OrganizationName: String, Organizationlogo: String): OrganizationEntity

    fun listOrganizations(): List<OrganizationEntity>
}
