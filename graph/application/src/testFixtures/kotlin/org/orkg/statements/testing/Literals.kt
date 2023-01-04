package org.orkg.statements.testing

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.time.OffsetDateTime

fun createLiteral(
    id: LiteralId? = LiteralId(1),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
    datatype: String = "xsd:string"
) = Literal(
    id = id,
    label = label,
    datatype = datatype,
    createdAt = createdAt,
    createdBy = createdBy,
)
