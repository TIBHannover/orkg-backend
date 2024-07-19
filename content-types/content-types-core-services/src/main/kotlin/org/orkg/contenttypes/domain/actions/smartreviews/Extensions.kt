package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource

fun Map<ThingId, List<GeneralStatement>>.findContributionId(smartReviewId: ThingId): ThingId? =
    this[smartReviewId]?.single {
        it.predicate.id == Predicates.hasContribution && it.`object` is Resource &&
            Classes.contributionSmartReview in (it.`object` as Resource).classes
    }?.`object`?.id
