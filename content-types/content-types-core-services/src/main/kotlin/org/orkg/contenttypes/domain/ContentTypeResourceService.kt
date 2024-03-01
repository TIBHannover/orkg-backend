package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContentTypeResourcesUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContentTypeResourceService(
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
        repository.findAll(
            includeClasses = setOf(Classes.paper),
            visibility = VisibilityFilter.FEATURED,
            pageable = pageable
        )

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.paper),
            visibility = VisibilityFilter.NON_FEATURED,
            pageable = pageable
        )

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.paper),
            visibility = VisibilityFilter.UNLISTED,
            pageable = pageable
        )

    override fun loadListedPapers(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.paper),
            visibility = VisibilityFilter.ALL_LISTED,
            pageable = pageable
        )

    override fun getFeaturedPaperFlag(id: ThingId): Boolean =
        repository.findPaperById(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedPaperFlag(id: ThingId): Boolean =
        repository.findPaperById(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.comparison),
            visibility = VisibilityFilter.FEATURED,
            pageable = pageable
        )

    override fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.comparison),
            visibility = VisibilityFilter.NON_FEATURED,
            pageable = pageable
        )

    override fun loadUnlistedComparisons(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.comparison),
            visibility = VisibilityFilter.UNLISTED,
            pageable = pageable
        )

    override fun loadListedComparisons(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.comparison),
            visibility = VisibilityFilter.ALL_LISTED,
            pageable = pageable
        )

    override fun loadFeaturedContributions(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.contribution),
            visibility = VisibilityFilter.FEATURED,
            pageable = pageable
        )

    override fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.contribution),
            visibility = VisibilityFilter.NON_FEATURED,
            pageable = pageable
        )

    override fun loadUnlistedContributions(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.contribution),
            visibility = VisibilityFilter.UNLISTED,
            pageable = pageable
        )

    override fun loadListedContributions(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.contribution),
            visibility = VisibilityFilter.ALL_LISTED,
            pageable = pageable
        )

    override fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.visualization),
            visibility = VisibilityFilter.FEATURED,
            pageable = pageable
        )

    override fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.visualization),
            visibility = VisibilityFilter.NON_FEATURED,
            pageable = pageable
        )

    override fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.visualization),
            visibility = VisibilityFilter.UNLISTED,
            pageable = pageable
        )

    override fun loadListedVisualizations(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.visualization),
            visibility = VisibilityFilter.ALL_LISTED,
            pageable = pageable
        )

    override fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.smartReviewPublished),
            visibility = VisibilityFilter.FEATURED,
            pageable = pageable
        )

    override fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.smartReviewPublished),
            visibility = VisibilityFilter.NON_FEATURED,
            pageable = pageable
        )

    override fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.smartReviewPublished),
            visibility = VisibilityFilter.UNLISTED,
            pageable = pageable
        )

    override fun loadListedSmartReviews(pageable: Pageable): Page<Resource> =
        repository.findAll(
            includeClasses = setOf(Classes.smartReviewPublished),
            visibility = VisibilityFilter.ALL_LISTED,
            pageable = pageable
        )

    override fun getFeaturedContributionFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.contribution in it.classes }
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedContributionFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.contribution in it.classes }
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedComparisonFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.comparison in it.classes }
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedComparisonFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.comparison in it.classes }
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedVisualizationFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.visualization in it.classes }
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedVisualizationFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.visualization in it.classes }
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedSmartReviewFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.smartReviewPublished in it.classes }
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedSmartReviewFlag(id: ThingId): Boolean =
        repository.findById(id)
            .filter { Classes.smartReviewPublished in it.classes }
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }
}
