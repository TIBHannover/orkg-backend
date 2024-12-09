package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
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
}
