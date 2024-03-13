package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jProblemsPerField
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jResearchFieldRepository
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResearchFieldAdapter(
    private val neo4jRepository: Neo4jResearchFieldRepository,
) : ResearchFieldRepository {
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map { it.toResource() }

    override fun getResearchProblemsOfField(fieldId: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem> =
        neo4jRepository.getResearchProblemsOfField(fieldId, pageable).map { it.toPaperCountPerResearchProblem() }

    override fun getContributorIdsFromResearchFieldAndIncludeSubfields(
        id: ThingId,
        pageable: Pageable
    ): Page<ContributorId> =
        neo4jRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable)

    override fun findResearchFieldsWithBenchmarks(pageable: Pageable): Page<Resource> =
        neo4jRepository.findResearchFieldsWithBenchmarks(pageable).map { it.toResource() }

    override fun getContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.getContributorIdsExcludingSubFields(id, pageable)

    override fun findAllListedPapersByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedPapersByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedPapersByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllPapersByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllPapersByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllPapersByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    override fun findAllListedProblemsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedProblemsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedProblemsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllProblemsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllProblemsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllProblemsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    override fun findAllListedVisualizationsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedVisualizationsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedVisualizationsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllVisualizationsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllVisualizationsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllVisualizationsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    override fun findAllListedSmartReviewsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedSmartReviewsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedSmartReviewsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllSmartReviewsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllSmartReviewsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllSmartReviewsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    override fun findAllListedLiteratureListsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedLiteratureListsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedLiteratureListsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    override fun findAllLiteratureListsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllLiteratureListsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllLiteratureListsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }

    private fun Neo4jProblemsPerField.toPaperCountPerResearchProblem() =
        PaperCountPerResearchProblem(
            problem = problem.toResource(),
            papers = papers
        )
}
