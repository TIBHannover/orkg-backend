package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.common.remapSort
import org.orkg.common.withDefaultSort
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jContributorStatisticsRepository
import org.orkg.contenttypes.domain.ContributorRecord
import org.orkg.contenttypes.output.ContributorStatisticsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

private val contributorRecordSortMapping = mapOf(
    "contributor_id" to "contributorId",
    "comparison_count" to "comparisonCount",
    "paper_count" to "paperCount",
    "contribution_count" to "contributionCount",
    "research_problem_count" to "researchProblemCount",
    "visualization_count" to "visualizationCount",
    "total_count" to "totalCount",
)

@Component
class SpringDataNeo4jContributorStatisticsAdapter(
    private val neo4jRepository: Neo4jContributorStatisticsRepository,
) : ContributorStatisticsRepository {
    override fun findAll(
        pageable: Pageable,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> {
        val afterString = after.toAfterString()
        val beforeString = before.toBeforeString()
        val pageableWithSort = pageable.withDefaultAndMappedSort()
        return neo4jRepository.findAll(afterString, beforeString, pageableWithSort)
    }

    override fun findAllByResearchFieldId(
        pageable: Pageable,
        researchField: ThingId,
        includeSubfields: Boolean,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> {
        val afterString = after.toAfterString()
        val beforeString = before.toBeforeString()
        val pageableWithSort = pageable.withDefaultAndMappedSort()
        return if (includeSubfields) {
            neo4jRepository.findAllByResearchFieldIdIncludingSubfields(
                id = researchField,
                after = afterString,
                before = beforeString,
                pageable = pageableWithSort
            )
        } else {
            neo4jRepository.findAllByResearchFieldId(
                id = researchField,
                after = afterString,
                before = beforeString,
                pageable = pageableWithSort
            )
        }
    }

    override fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        includeSubproblems: Boolean,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> {
        val afterString = after.toAfterString()
        val beforeString = before.toBeforeString()
        val pageableWithSort = pageable.withDefaultAndMappedSort()
        return if (includeSubproblems) {
            neo4jRepository.findAllByResearchProblemIdIncludingSubProblems(
                id = researchProblem,
                after = afterString,
                before = beforeString,
                pageable = pageableWithSort
            )
        } else {
            neo4jRepository.findAllByResearchProblemId(
                id = researchProblem,
                after = afterString,
                before = beforeString,
                pageable = pageableWithSort
            )
        }
    }

    private fun Pageable.withDefaultAndMappedSort(): Pageable =
        withDefaultSort { Sort.by("total_count").descending() }.remapSort(contributorRecordSortMapping)
}
