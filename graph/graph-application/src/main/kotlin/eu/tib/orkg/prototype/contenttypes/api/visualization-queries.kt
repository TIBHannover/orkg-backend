package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contenttypes.domain.model.Visualization
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveVisualizationUseCase {
    fun findById(id: ThingId): Optional<Visualization>
    fun findAll(pageable: Pageable): Page<Visualization>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Visualization>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Visualization>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Visualization>
}
