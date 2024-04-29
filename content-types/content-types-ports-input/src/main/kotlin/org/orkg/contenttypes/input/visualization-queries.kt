package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Visualization
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveVisualizationUseCase {
    fun findById(id: ThingId): Optional<Visualization>
    fun findAll(pageable: Pageable): Page<Visualization>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Visualization>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Visualization>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Visualization>
    fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Visualization>
}
