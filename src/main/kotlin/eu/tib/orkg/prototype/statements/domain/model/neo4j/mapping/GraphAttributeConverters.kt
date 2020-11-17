package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import org.neo4j.ogm.typeconversion.AttributeConverter

class ContributorIdConverter :
    AttributeConverter<ContributorId?, String?> {
    override fun toGraphProperty(value: ContributorId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ContributorId(value) else null
}

class ObservatoryIdConverter :
    AttributeConverter<ObservatoryId?, String?> {
    override fun toGraphProperty(value: ObservatoryId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ObservatoryId(value) else null
}

class OrganizationIdConverter :
    AttributeConverter<OrganizationId?, String?> {
    override fun toGraphProperty(value: OrganizationId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) OrganizationId(value) else null
}
