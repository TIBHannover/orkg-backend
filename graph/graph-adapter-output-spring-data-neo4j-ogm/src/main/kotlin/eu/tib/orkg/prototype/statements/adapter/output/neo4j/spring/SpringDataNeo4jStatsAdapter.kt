package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributorRecord
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ChangeLogResponse
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jStatsAdapter(
    private val neo4jRepository: Neo4jStatsRepository
) : StatsRepository {
    override fun getGraphMetaData(): Iterable<HashMap<String, Any>> =
        neo4jRepository.getGraphMetaData()

    override fun getResearchFieldsPapersCount(): Iterable<FieldsStats> =
        neo4jRepository.getResearchFieldsPapersCount()

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryComparisonsCount(id)

    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        neo4jRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats =
        neo4jRepository.findObservatoryStatsById(id)

    override fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): ResearchFieldStats =
        if (includeSubfields) {
            neo4jRepository.findResearchFieldStatsByIdIncludingSubfields(id)
        } else {
            neo4jRepository.findResearchFieldStatsById(id)
        }.let {
            ResearchFieldStats(
                id = ThingId(it.id),
                papers = it.papers,
                comparisons = it.comparisons,
                total = it.total
            )
        }

    override fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurrentContributorIdsAndContributionsCount(date, pageable)
            .map { it.toContributionRecord() }

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, date, pageable)
            .map { it.toContributionRecord() }

    override fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id = id,
            date = date,
            pageable = pageable
        ).map { it.toContributionRecord() }

    override fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse> =
        neo4jRepository.getChangeLog(pageable)

    override fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLogResponse> =
        neo4jRepository.getChangeLogByResearchField(id, pageable)

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> =
        neo4jRepository.getTrendingResearchProblems(pageable)

    override fun getOrphanedNodesCount(): Long = neo4jRepository.getOrphanedNodesCount()

    private fun Neo4jContributorRecord.toContributionRecord() = RetrieveStatisticsUseCase.ContributorRecord(
        contributor = ContributorId(contributor),
        comparisons = comparisons,
        papers = papers,
        contributions = contributions,
        problems = problems,
        visualizations = visualizations,
        total = total
    )
}
