package eu.tib.orkg.prototype.statements.application.port.out

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadResourcePort {
    fun loadVerifiedResources(pageable: Pageable): Page<Resource>
    fun loadUnverifiedResources(pageable: Pageable): Page<Resource>

    fun loadFeaturedResources(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedResources(pageable: Pageable): Page<Resource>

    fun loadUnlistedResources(pageable: Pageable): Page<Resource>
    fun loadListedResources(pageable: Pageable): Page<Resource>
}

interface LoadPaperAdapter {
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource>
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource>

    fun loadFeaturedPapers(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource>

    fun loadUnlistedPapers(pageable: Pageable): Page<Resource>
    fun loadListedPapers(pageable: Pageable): Page<Resource>
}

interface LoadComparisonPort {
    fun loadFeaturedComparisons(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource>

    fun loadUnlistedComparisons(pageable: Pageable): Page<Resource>
    fun loadListedComparisons(pageable: Pageable): Page<Resource>
}

interface LoadContributionPort {
    fun loadFeaturedContributions(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource>

    fun loadUnlistedContributions(pageable: Pageable): Page<Resource>
    fun loadListedContributions(pageable: Pageable): Page<Resource>
}

interface LoadVisualizationPort {
    fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource>

    fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource>
    fun loadListedVisualizations(pageable: Pageable): Page<Resource>
}

interface LoadSmartReviewPort {
    fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource>

    fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource>
    fun loadListedSmartReviews(pageable: Pageable): Page<Resource>
}

interface LoadProblemPort {
    fun loadFeaturedProblems(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource>

    fun loadUnlistedProblems(pageable: Pageable): Page<Resource>
    fun loadListedProblems(pageable: Pageable): Page<Resource>
}

interface GetPaperFlagQuery {
    fun getPaperVerifiedFlag(id: ResourceId): Boolean?
    fun getFeaturedPaperFlag(id: ResourceId): Boolean
    fun getUnlistedPaperFlag(id: ResourceId): Boolean?
}

interface GetResourceFlagQuery {
    fun getFeaturedResourceFlag(id: ResourceId): Boolean
    fun getUnlistedResourceFlag(id: ResourceId): Boolean?
}

interface GetProblemFlagQuery {
    fun getFeaturedProblemFlag(id: ResourceId): Boolean
    fun getUnlistedProblemFlag(id: ResourceId): Boolean?
}

interface GetContributionFlagQuery {
    fun getFeaturedContributionFlag(id: ResourceId): Boolean
    fun getUnlistedContributionFlag(id: ResourceId): Boolean?
}

interface GetComparisonFlagQuery {
    fun getFeaturedComparisonFlag(id: ResourceId): Boolean
    fun getUnlistedComparisonFlag(id: ResourceId): Boolean
}

interface GetVisualizationFlagQuery {
    fun getFeaturedVisualizationFlag(id: ResourceId): Boolean
    fun getUnlistedVisualizationFlag(id: ResourceId): Boolean
}

interface GetSmartReviewFlagQuery {
    fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean
    fun getUnlistedSmartReviewFlag(id: ResourceId): Boolean
}
