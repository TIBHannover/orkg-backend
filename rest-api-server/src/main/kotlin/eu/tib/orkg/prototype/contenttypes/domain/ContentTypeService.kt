package eu.tib.orkg.prototype.contenttypes.domain

import eu.tib.orkg.prototype.contenttypes.api.ContentTypeUseCase
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jComparisonRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jContributionRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jSmartReviewRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jVisualizationRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val Contribution = ClassId("Contribution")
private val Comparison = ClassId("Comparison")
private val ResearchProblem = ClassId("ResearchProblem")
private val SmartReview = ClassId("SmartReviewPublished")
private val Visualization = ClassId("Visualization")

@Service
@Transactional
class ContentTypeService(
    private val resourceRepository: ResourceRepository,
    private val neo4jComparisonRepository: Neo4jComparisonRepository,
    private val neo4jContributionRepository: Neo4jContributionRepository,
    private val neo4jVisualizationRepository: Neo4jVisualizationRepository,
    private val neo4jSmartReviewRepository: Neo4jSmartReviewRepository,
    private val neo4jProblemRepository: Neo4jProblemRepository,
) : ContentTypeUseCase {
    override fun loadFeaturedContributions(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedResourcesByClassId(Contribution, pageable)

    override fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedResourcesByClassId(Contribution, pageable)

    override fun loadUnlistedContributions(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedResourcesByClassId(Contribution, pageable)

    override fun loadListedContributions(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByClassId(Contribution, pageable)

    override fun getFeaturedContributionFlag(id: ResourceId): Boolean {
        val result = neo4jContributionRepository.findContributionByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedContributionFlag(id: ResourceId): Boolean {
        val result = neo4jContributionRepository.findContributionByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedResourcesByClassId(Comparison, pageable)

    override fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedResourcesByClassId(Comparison, pageable)

    override fun loadUnlistedComparisons(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedResourcesByClassId(Comparison, pageable)

    override fun loadListedComparisons(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByClassId(Comparison, pageable)

    override fun getFeaturedComparisonFlag(id: ResourceId): Boolean {
        val result = neo4jComparisonRepository.findComparisonByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedComparisonFlag(id: ResourceId): Boolean {
        val result = neo4jComparisonRepository.findComparisonByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedResourcesByClassId(Visualization, pageable)

    override fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedResourcesByClassId(Visualization, pageable)

    override fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedResourcesByClassId(Visualization, pageable)

    override fun loadListedVisualizations(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByClassId(Visualization, pageable)

    override fun getFeaturedVisualizationFlag(id: ResourceId): Boolean {
        val result = neo4jVisualizationRepository.findVisualizationByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedVisualizationFlag(id: ResourceId): Boolean {
        val result = neo4jVisualizationRepository.findVisualizationByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedResourcesByClassId(SmartReview, pageable)

    override fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedResourcesByClassId(SmartReview, pageable)

    override fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedResourcesByClassId(SmartReview, pageable)

    override fun loadListedSmartReviews(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByClassId(SmartReview, pageable)

    override fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean {
        val result = neo4jSmartReviewRepository.findSmartReviewByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedSmartReviewFlag(id: ResourceId): Boolean {
        val result = neo4jSmartReviewRepository.findSmartReviewByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun markAsVerified(resourceId: ResourceId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ResourceId) = setVerifiedFlag(resourceId, false)

    override fun loadVerifiedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByVerifiedIsTrue(pageable)

    override fun loadUnverifiedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByVerifiedIsFalse(pageable)

    override fun loadVerifiedPapers(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllVerifiedPapers(pageable)

    override fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnverifiedPapers(pageable)

    /**
     * Get the "verified" flag of a paper resource.
     *
     * @param id The ID of a resource of class "Paper".
     * @return The value of the flag, or `null` if the resource is not found or not a paper.
     */
    override fun getPaperVerifiedFlag(id: ResourceId): Boolean? {
        val result = resourceRepository.findPaperByResourceId(id)
        if (result.isPresent) {
            val paper = result.get()
            return paper.verified ?: false
        }
        return null
    }

    override fun markAsFeatured(resourceId: ResourceId): Optional<Resource> {
        setUnlistedFlag(resourceId, false)
        return setFeaturedFlag(resourceId, true)
    }

    override fun markAsNonFeatured(resourceId: ResourceId) = setFeaturedFlag(resourceId, false)

    override fun markAsUnlisted(resourceId: ResourceId): Optional<Resource> {
        setFeaturedFlag(resourceId, false)
        return setUnlistedFlag(resourceId, true)
    }

    override fun markAsListed(resourceId: ResourceId) = setUnlistedFlag(resourceId, false)

    override fun loadFeaturedPapers(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedPapers(pageable)

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedPapers(pageable)

    override fun loadFeaturedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByVerifiedIsTrue(pageable)

    override fun loadNonFeaturedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByVerifiedIsFalse(pageable)

    override fun loadUnlistedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByUnlistedIsTrue(pageable)

    override fun loadListedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByUnlistedIsFalse(pageable)

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedPapers(pageable)

    override fun loadListedPapers(pageable: Pageable): Page<Resource> = resourceRepository.findAllListedPapers(pageable)

    override fun getFeaturedPaperFlag(id: ResourceId): Boolean {
        val result = resourceRepository.findPaperByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedPaperFlag(id: ResourceId): Boolean {
        val result = resourceRepository.findPaperByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun getFeaturedResourceFlag(id: ResourceId): Boolean {
        val result = resourceRepository.findByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedResourceFlag(id: ResourceId): Boolean {
        val result = resourceRepository.findByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    private fun setFeaturedFlag(resourceId: ResourceId, featured: Boolean): Optional<Resource> {
        val result = resourceRepository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(featured = featured)
        return Optional.of(resourceRepository.save(resultObj))
    }

    private fun setVerifiedFlag(resourceId: ResourceId, verified: Boolean): Optional<Resource> {
        val result = resourceRepository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(verified = verified)
        return Optional.of(resourceRepository.save(resultObj))
    }

    private fun setUnlistedFlag(resourceId: ResourceId, unlisted: Boolean): Optional<Resource> {
        val result = resourceRepository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(unlisted = unlisted)
        return Optional.of(resourceRepository.save(resultObj))
    }

    override fun getFeaturedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun loadFeaturedProblems(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllFeaturedResourcesByClassId(ResearchProblem, pageable)

    override fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllNonFeaturedResourcesByClassId(ResearchProblem, pageable)

    override fun loadUnlistedProblems(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnlistedResourcesByClassId(ResearchProblem, pageable)

    override fun loadListedProblems(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllByClassId(ResearchProblem, pageable)
}
