package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePaperUseCase {
    fun findById(id: ThingId): Optional<Paper>
    fun findAll(pageable: Pageable): Page<Paper>
    fun findAllByDOI(doi: String, pageable: Pageable): Page<Paper>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Paper>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Paper>
    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>
}
