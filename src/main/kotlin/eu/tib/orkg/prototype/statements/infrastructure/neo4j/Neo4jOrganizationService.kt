package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationServiceNeo
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jOrganization
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jOrganizationService(
    private val neo4jOrganizationRepository: Neo4jOrganizationRepository
) : OrganizationServiceNeo {
    override fun create(OrganizationName: String, CreatedBy: ContributorId, Url: String): Organization {
        val organizationId = UUID.randomUUID()
        return neo4jOrganizationRepository.save(Neo4jOrganization(organizationId = OrganizationId(organizationId), name = OrganizationName, createdBy = CreatedBy, url = Url)).toOrganization()
    }

    override fun create(id: UUID, OrganizationName: String, CreatedBy: ContributorId, Url: String): Organization {
        return neo4jOrganizationRepository.save(Neo4jOrganization(organizationId = OrganizationId(id), name = OrganizationName, createdBy = CreatedBy, url = Url)).toOrganization()
    }

    override fun listOrganizations(): List<Organization> {
        return neo4jOrganizationRepository.findAll()
            .map(Neo4jOrganization::toOrganization)
    }

    override fun findById(id: OrganizationId): Optional<Organization> =
        neo4jOrganizationRepository
            .findByOrganizationId(id.value)
            .map(Neo4jOrganization::toOrganization)

    override fun updateOrganization(organization: Organization): Organization {
        val entity = neo4jOrganizationRepository.findById(organization.id!!.value).get()

        if (organization.name != entity.name)
            entity.name = organization.name

        if (organization.homepage != entity.url)
            entity.url = organization.homepage

        return neo4jOrganizationRepository.save(entity).toOrganization()
    }
}
