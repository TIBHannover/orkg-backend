package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.neo4j.ogm.typeconversion.AttributeConverter

class ClassIdConverter :
    AttributeConverter<ClassId?, String?> {
    override fun toGraphProperty(value: ClassId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ClassId(value) else null
}

class ContributorIdConverter :
    AttributeConverter<ContributorId?, String?> {
    override fun toGraphProperty(value: ContributorId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ContributorId(value) else null
}

class LiteralIdConverter :
    AttributeConverter<LiteralId?, String?> {
    override fun toGraphProperty(value: LiteralId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) LiteralId(value) else null
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

class ResourceIdConverter :
    AttributeConverter<ResourceId?, String?> {
    override fun toGraphProperty(value: ResourceId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ResourceId(value) else null
}

class StatementIdConverter :
    AttributeConverter<StatementId?, String?> {
    override fun toGraphProperty(value: StatementId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) StatementId(value) else null
}
