package eu.tib.orkg.prototype.statements.domain.model

import java.util.Optional
import java.util.UUID

interface OrganizationService {

    /**
     * Create a new organization with a given name.
     */
    fun create(OrganizationName: String, CreatedBy: UUID, Url: String): Organization

    fun listOrganizations(): List<Organization>

    fun findById(id: UUID): Optional<Organization>
}
