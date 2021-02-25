package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OrganizationIdConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Organization")
data class Neo4jOrganization(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Property("organization_id")
    @Convert(OrganizationIdConverter::class)
    var organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),

    @Property("name")
    var name: String? = null,

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    private var createdBy: ContributorId = ContributorId.createUnknownContributor(),

    @Property("url")
    var url: String? = null,

    var observatories: MutableSet<Neo4jObservatory>? = mutableSetOf()

) {

    fun toOrganization() =
        Organization(
            id = organizationId, // OrganizationId(id!!),
            name = name,
            logo = null,
            createdBy = createdBy,
            homepage = url,
            observatoryIds = observatories!!.map { it.observatoryId }.toSet()
        )
}
