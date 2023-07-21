package org.orkg.statements.testing

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

fun createLiteral(
    id: ThingId = ThingId("L1"),
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
