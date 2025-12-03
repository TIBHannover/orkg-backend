package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.LegacyNeo4jResearchFieldRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jProblemsPerField
import org.orkg.contenttypes.output.LegacyResearchFieldRepository
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class LegacySpringDataNeo4JResearchFieldAdapter(
    private val neo4jRepository: LegacyNeo4jResearchFieldRepository,
) : LegacyResearchFieldRepository {
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map { it.toResource() }

    override fun findAllPaperCountsPerResearchProblem(fieldId: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem> =
        neo4jRepository.findAllPaperCountsPerResearchProblem(fieldId, pageable).map { it.toPaperCountPerResearchProblem() }

    override fun findAllContributorIdsIncludingSubFields(
        id: ThingId,
        pageable: Pageable,
    ): Page<ContributorId> =
        neo4jRepository.findAllContributorIdsIncludingSubFields(id, pageable)

    override fun findAllWithBenchmarks(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllWithBenchmarks(pageable).map { it.toResource() }

    override fun findAllContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorIdsExcludingSubFields(id, pageable)

    override fun findAllListedProblemsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable,
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedProblemsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedProblemsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllProblemsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable,
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllProblemsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllProblemsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    private fun Neo4jProblemsPerField.toPaperCountPerResearchProblem() =
        PaperCountPerResearchProblem(
            problem = problem.toResource(),
            papers = papers
        )
}
