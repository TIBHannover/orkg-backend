package eu.tib.orkg.prototype.ranking.spi

import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface RankingService {
    fun findAllStatementsAboutPaper(id: ThingId): Set<Pair<ThingId, ThingId>>
    fun countSumOfDistinctPredicatesForContributions(contributionIds: Set<ThingId>): Long
    fun countComparisonsIncludingPaper(id: ThingId): Long
    fun countLiteratureListsIncludingPaper(id: ThingId): Long
}
