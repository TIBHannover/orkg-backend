package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldUseCase {
    fun findById(id: ThingId): Optional<ResourceRepresentation>

    fun getResearchProblemsOfField(id: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun getContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>
    fun getContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun findAllPapersByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllComparisonsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllVisualizationsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllSmartReviewsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllLiteratureListsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun findAllEntitiesBasedOnClassesByResearchField(
        id: ThingId,
        classesList: List<String>,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<ResourceRepresentation>

    fun withBenchmarks(pageable: Pageable): Page<ResearchField>

    data class PaperCountPerResearchProblem(
        val problem: ResourceRepresentation,
        val papers: Long
    )
}
