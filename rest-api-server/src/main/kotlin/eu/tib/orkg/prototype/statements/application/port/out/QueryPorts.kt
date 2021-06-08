package eu.tib.orkg.prototype.statements.application.port.out

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadResourcePort {
    fun loadVerifiedResources(pageable: Pageable): Page<Resource>

    fun loadUnverifiedResources(pageable: Pageable): Page<Resource>
}

interface LoadPaperPort {
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource>
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource>
}

interface GetPaperVerifiedFlagQuery {
    fun getPaperVerifiedFlag(id: ResourceId): Boolean?
}

interface LoadFeaturedResourceAdapter {
    fun loadFeaturedResources(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedResources(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedComparisonPort {
    fun loadFeaturedComparisons(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedContributionPort {
    fun loadFeaturedContributions(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedVisualizationPort {
    fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedSmartReviewPort {
    fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedPaperAdapter {
    fun loadFeaturedPapers(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedProblemAdapter {
    fun loadFeaturedProblems(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource>
}

interface GetFeaturedPaperFlagQuery {
    fun getFeaturedPaperFlag(id: ResourceId): Boolean?
}

interface GetFeaturedResourceFlagQuery {
    fun getFeaturedResourceFlag(id: ResourceId): Boolean? // Can combine this with the above
}

interface GetFeaturedProblemFlagQuery {
    fun getFeaturedProblemFlag(id: ResourceId): Boolean?
}

interface GetFeaturedContributionFlagQuery {
    fun getFeaturedContributionFlag(id: ResourceId): Boolean?
}

interface GetFeaturedComparisonFlagQuery {
    fun getFeaturedComparisonFlag(id: ResourceId): Boolean?
}

interface GetFeaturedVisualizationFlagQuery {
    fun getFeaturedVisualizationFlag(id: ResourceId): Boolean?
}

interface GetFeaturedSmartReviewFlagQuery {
    fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean?
}
