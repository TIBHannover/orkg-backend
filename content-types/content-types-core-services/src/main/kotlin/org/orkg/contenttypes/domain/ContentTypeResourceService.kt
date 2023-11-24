package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContentTypeResourcesUseCase
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ContributionRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContentTypeResourceService(
    private val comparisonRepository: ComparisonRepository,
    private val contributionRepository: ContributionRepository,
    private val visualizationRepository: VisualizationRepository,
    private val smartReviewRepository: SmartReviewRepository,
    private val repository: ResourceRepository
) : ContentTypeResourcesUseCase {
    override fun loadVerifiedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVerified(true, pageable)

    override fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVerified(false, pageable)

    override fun getPaperVerifiedFlag(id: ThingId): Boolean? =
        repository.findPaperById(id)
            .map { it.verified }
            .orElseThrow { ResourceNotFound.withId(id) }
    override fun loadFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllByClassAndVisibility(Classes.paper, Visibility.FEATURED, pageable)

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllByClassAndVisibility(Classes.paper, Visibility.DEFAULT, pageable)

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllByClassAndVisibility(Classes.paper, Visibility.UNLISTED, pageable)

    override fun loadListedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllListedByClass(Classes.paper, pageable)

    override fun getFeaturedPaperFlag(id: ThingId): Boolean =
        repository.findPaperById(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedPaperFlag(id: ThingId): Boolean =
        repository.findPaperById(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllComparisonsByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllComparisonsByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllComparisonsByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllListedComparisons(pageable)

    override fun loadFeaturedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllContributionsByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllContributionsByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllContributionsByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllListedContributions(pageable)

    override fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllVisualizationsByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllVisualizationsByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllVisualizationsByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllListedVisualizations(pageable)

    override fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllSmartReviewsByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllSmartReviewsByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllSmartReviewsByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllListedSmartReviews(pageable)

    override fun getFeaturedContributionFlag(id: ThingId): Boolean =
        contributionRepository.findContributionByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedContributionFlag(id: ThingId): Boolean =
        contributionRepository.findContributionByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedComparisonFlag(id: ThingId): Boolean =
        comparisonRepository.findComparisonByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedComparisonFlag(id: ThingId): Boolean =
        comparisonRepository.findComparisonByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedVisualizationFlag(id: ThingId): Boolean =
        visualizationRepository.findVisualizationByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedVisualizationFlag(id: ThingId): Boolean =
        visualizationRepository.findVisualizationByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedSmartReviewFlag(id: ThingId): Boolean =
        smartReviewRepository.findSmartReviewByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedSmartReviewFlag(id: ThingId): Boolean =
        smartReviewRepository.findSmartReviewByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }
}
