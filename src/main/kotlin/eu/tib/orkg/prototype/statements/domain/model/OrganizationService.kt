package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.Optional
import java.util.UUID

interface OrganizationService {

    /**
     * Create a new company with a given name.
     *
     */
    fun create(OrganizationName: String, CreatedBy: UUID, Url: String): Organization

    fun listOrganizations(): List<Organization>

    fun findById(id: UUID): Optional<OrganizationEntity>
}
