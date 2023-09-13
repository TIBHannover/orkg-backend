package org.orkg.statements.testing

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

fun createPredicate(
    id: ThingId = ThingId("P1"),
    label: String = "some predicate label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: ContributorId = ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c"),
    description: String? = null
) = Predicate(id, label, createdAt, createdBy, description)
