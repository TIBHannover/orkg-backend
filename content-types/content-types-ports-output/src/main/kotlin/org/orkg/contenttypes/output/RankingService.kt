package org.orkg.contenttypes.output

import org.orkg.common.ThingId

interface RankingService {
    fun findAllStatementsAboutPaper(id: ThingId): Set<Pair<ThingId, ThingId>>
    fun countSumOfDistinctPredicatesForContributions(contributionIds: Set<ThingId>): Long
    fun countComparisonsIncludingPaper(id: ThingId): Long
    fun countLiteratureListsIncludingPaper(id: ThingId): Long
    fun countRosettaStoneStatementsAssociatedToPaper(id: ThingId): Long
}
