package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldUseCase {
    fun findById(id: ThingId): Optional<Resource>

    fun getResearchProblemsOfField(id: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun getContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>
    fun getContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun findAllPapersByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllComparisonsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllVisualizationsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllSmartReviewsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllLiteratureListsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllEntitiesBasedOnClassesByResearchField(
        id: ThingId,
        classesList: List<String>,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun withBenchmarks(pageable: Pageable): Page<ResearchField>

    data class PaperCountPerResearchProblem(
        val problem: Resource,
        val papers: Long
    )
}
