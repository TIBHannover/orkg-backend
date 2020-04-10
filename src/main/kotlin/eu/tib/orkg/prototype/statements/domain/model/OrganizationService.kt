package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.Optional
import java.util.UUID

interface OrganizationService {

    /**
     * Create a new company with a given name.
     *
     */
    fun create(OrganizationName: String, Organizationlogo: String): OrganizationEntity

    fun listOrganizations(): List<OrganizationEntity>

    fun findById(id: UUID): Optional<OrganizationEntity>
}
