package eu.tib.orkg.prototype.statements.application.port.out

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadResourceAdapter {
    fun loadVerifiedResources(pageable: Pageable): Page<Resource>
    fun loadUnverifiedResources(pageable: Pageable): Page<Resource>

    fun loadFeaturedResources(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedResources(pageable: Pageable): Page<Resource>

    fun loadUnlistedPapers(pageable: Pageable): Page<Resource>

}

interface LoadPaperAdapter {
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource>
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource>

    fun loadFeaturedPapers(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource>

    fun loadUnlistedPapers(pageable: Pageable): Page<Resource>
}


interface LoadComparisonAdapter {
    fun loadFeaturedComparisons(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource>
}

interface LoadContributionAdapter {
    fun loadFeaturedContributions(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource>
}

interface LoadVisualizationAdapter {
    fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource>
}

interface LoadSmartReviewAdapter {
    fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource>
}

interface LoadProblemAdapter {
    fun loadFeaturedProblems(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource>
}

interface GetPaperFlagQuery {
    fun getPaperVerifiedFlag(id: ResourceId): Boolean?
    fun getFeaturedPaperFlag(id: ResourceId): Boolean?
}

interface GetResourceFlagQuery {
    fun getFeaturedResourceFlag(id: ResourceId): Boolean?
}

interface GetProblemFlagQuery {
    fun getFeaturedProblemFlag(id: ResourceId): Boolean?
}

interface GetContributionFlagQuery {
    fun getFeaturedContributionFlag(id: ResourceId): Boolean?
}

interface GetComparisonFlagQuery {
    fun getFeaturedComparisonFlag(id: ResourceId): Boolean?
}

interface GetVisualizationFlagQuery {
    fun getFeaturedVisualizationFlag(id: ResourceId): Boolean?
}

interface GetSmartReviewFlagQuery {
    fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean?
}
