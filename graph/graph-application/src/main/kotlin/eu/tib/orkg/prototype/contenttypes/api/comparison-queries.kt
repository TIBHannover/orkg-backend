package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveComparisonUseCase {
    fun findById(id: ThingId): Optional<Comparison>
    fun findAll(pageable: Pageable): Page<Comparison>
    fun findAllByDOI(doi: String, pageable: Pageable): Page<Comparison>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Comparison>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Comparison>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Comparison>
    fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource>
    fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource>
    fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure>
    fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure>
}

interface RetrieveComparisonContributionsUseCase {
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
