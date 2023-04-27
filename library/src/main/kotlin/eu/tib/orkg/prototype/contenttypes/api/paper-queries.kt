package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePaperUseCase {
    fun findById(id: ThingId): PaperRepresentation
    fun findAll(pageable: Pageable): Page<PaperRepresentation>
    fun findAllByDOI(doi: String, pageable: Pageable): Page<PaperRepresentation>
    fun findAllByTitle(title: String, pageable: Pageable): Page<PaperRepresentation>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<PaperRepresentation>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<PaperRepresentation>
    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>
}
