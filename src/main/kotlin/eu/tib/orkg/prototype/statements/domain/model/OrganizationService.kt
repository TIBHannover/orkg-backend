package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity

interface OrganizationService {

    /**
     * Create a new company with a given name.
     *
     */
    fun create(companyName: String, Companylogo: String): OrganizationEntity
}
