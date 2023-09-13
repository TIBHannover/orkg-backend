package org.orkg.statements.testing

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

fun createList(
    id: ThingId = ThingId("List1"),
    label: String = "Default Label",
    elements: kotlin.collections.List<ThingId> = emptyList(),
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
) = List(
    id = id,
    label = label,
    elements = elements,
    createdAt = createdAt,
    createdBy = createdBy
)
