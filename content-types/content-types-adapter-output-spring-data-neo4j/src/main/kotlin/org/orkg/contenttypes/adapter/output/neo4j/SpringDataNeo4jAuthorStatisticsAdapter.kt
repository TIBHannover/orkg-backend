package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.common.remapSort
import org.orkg.common.withDefaultSort
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jAuthorStatisticsRepository
import org.orkg.contenttypes.domain.AuthorRecord
import org.orkg.contenttypes.output.AuthorStatisticsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

private val authorRecordSortMapping = mapOf(
    "author_id" to "authorId",
    "author_name" to "authorName",
    "comparison_count" to "comparisonCount",
    "paper_count" to "paperCount",
    "visualization_count" to "visualizationCount",
    "total_count" to "totalCount",
)

@Component
class SpringDataNeo4jAuthorStatisticsAdapter(
    private val neo4jRepository: Neo4jAuthorStatisticsRepository,
) : AuthorStatisticsRepository {
    override fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<AuthorRecord> {
        val afterString = after.toAfterString()
        val beforeString = before.toBeforeString()
        val pageableWithSort = pageable.withDefaultAndMappedSort()
        return neo4jRepository.findAllByResearchProblemId(
            id = researchProblem,
            after = afterString,
            before = beforeString,
            pageable = pageableWithSort,
        ).map { it.toAuthorRecord() }
    }

    private fun Pageable.withDefaultAndMappedSort(): Pageable =
        withDefaultSort {
            Sort.by("total_count").descending()
                .and(Sort.by("author_name").ascending())
                .and(Sort.by("author_id").ascending())
        }.remapSort(authorRecordSortMapping)
}
