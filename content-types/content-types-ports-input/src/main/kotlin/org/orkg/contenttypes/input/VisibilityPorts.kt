package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadPaperPort {
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
    fun getPaperVerifiedFlag(id: ThingId): Boolean?
    fun getFeaturedPaperFlag(id: ThingId): Boolean
    fun getUnlistedPaperFlag(id: ThingId): Boolean
}

interface GetProblemFlagQuery {
    fun getFeaturedProblemFlag(id: ThingId): Boolean
    fun getUnlistedProblemFlag(id: ThingId): Boolean
}

interface GetContributionFlagQuery {
    fun getFeaturedContributionFlag(id: ThingId): Boolean
    fun getUnlistedContributionFlag(id: ThingId): Boolean
}

interface GetComparisonFlagQuery {
    fun getFeaturedComparisonFlag(id: ThingId): Boolean
    fun getUnlistedComparisonFlag(id: ThingId): Boolean
}

interface GetVisualizationFlagQuery {
    fun getFeaturedVisualizationFlag(id: ThingId): Boolean
    fun getUnlistedVisualizationFlag(id: ThingId): Boolean
}

interface GetSmartReviewFlagQuery {
    fun getFeaturedSmartReviewFlag(id: ThingId): Boolean
    fun getUnlistedSmartReviewFlag(id: ThingId): Boolean
}
