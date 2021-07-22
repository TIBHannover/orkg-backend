package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jComparisonRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jContributionRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jSmartReviewRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jVisualizationRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResourceService(
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jComparisonRepository: Neo4jComparisonRepository,
    private val neo4jContributionRepository: Neo4jContributionRepository,
    private val neo4jVisualizationRepository: Neo4jVisualizationRepository,
    private val neo4jSmartReviewRepository: Neo4jSmartReviewRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator
) : ResourceService {

    override fun create(label: String) = create(
        ContributorId.createUnknownContributor(),
        label,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(userId: ContributorId, label: String, observatoryId: ObservatoryId, extractionMethod: ExtractionMethod, organizationId: OrganizationId): Resource {
        var resourceId = neo4jResourceIdGenerator.nextIdentity()

        // Should be moved to the Generator in the future
        while (neo4jResourceRepository.findByResourceId(resourceId).isPresent) {
            resourceId = neo4jResourceIdGenerator.nextIdentity()
        }

        return neo4jResourceRepository.save(Neo4jResource(label = label, resourceId = resourceId, createdBy = userId, observatoryId = observatoryId, extractionMethod = extractionMethod, organizationId = organizationId))
            .toResource()
    }

    override fun create(request: CreateResourceRequest) = create(
        ContributorId.createUnknownContributor(),
        request,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(userId: ContributorId, request: CreateResourceRequest, observatoryId: ObservatoryId, extractionMethod: ExtractionMethod, organizationId: OrganizationId): Resource {
        val id = request.id ?: neo4jResourceIdGenerator.nextIdentity()
        val resource = Neo4jResource(label = request.label, resourceId = id, createdBy = userId, observatoryId = observatoryId, extractionMethod = extractionMethod, organizationId = organizationId)
        request.classes.forEach { resource.assignTo(it.toString()) }
        return neo4jResourceRepository.save(resource).toResource()
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAll(pageable)
            .map(Neo4jResource::toResource)

    override fun findById(id: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(id)
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource> {
        return neo4jResourceRepository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration
            .map(Neo4jResource::toResource)
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: ContributorId): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), part.toSearchString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: ContributorId): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id.toString(), part.toSearchString(), createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.map { it.value }, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(ids.map { it.value }, part.toSearchString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jResourceRepository.findByDOI(doi)
            .map(Neo4jResource::toResource)

    override fun findByTitle(title: String?): Optional<Resource> =
        neo4jResourceRepository.findByLabel(title)
            .map(Neo4jResource::toResource)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByDOI(doi)
            .map(Neo4jResource::toResource)

    override fun findAllByTitle(title: String?): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(title!!)
            .map(Neo4jResource::toResource)

    override fun findAllByFeatured(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByFeaturedIsTrue(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByNonFeatured(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByFeaturedIsFalse(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByUnlisted(pageable: Pageable):
        Page<Resource> =
        neo4jResourceRepository.findAllByUnlistedIsTrue(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByListed(pageable: Pageable):
        Page<Resource> =
        neo4jResourceRepository.findAllByUnlistedIsFalse(pageable)
            .map(Neo4jResource::toResource)

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findPapersByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findComparisonsByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findProblemsByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        neo4jResourceRepository.findContributorsByResourceId(id)

    override fun update(request: UpdateResourceRequest): Resource {
        // already checked by service
        val found = neo4jResourceRepository.findByResourceId(request.id).get()

        // update all the properties
        if (request.label != null)
            found.label = request.label
        if (request.classes != null)
            found.classes = request.classes

        return neo4jResourceRepository.save(found).toResource()
    }

    override fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): Resource {
        val found = neo4jResourceRepository.findByResourceId(id).get()
            found.observatoryId = request.observatoryId
            found.organizationId = request.organizationId

        return neo4jResourceRepository.save(found).toResource()
    }

    override fun hasStatements(id: ResourceId) =
        neo4jResourceRepository.checkIfResourceHasStatements(id)

    override fun delete(id: ResourceId) {
        val found = neo4jResourceRepository.findByResourceId(id).get()
        neo4jResourceRepository.delete(found)
    }

    override fun removeAll() = neo4jResourceRepository.deleteAll()

    override fun markAsVerified(resourceId: ResourceId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ResourceId) = setVerifiedFlag(resourceId, false)

    override fun loadVerifiedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByVerifiedIsTrue(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnverifiedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByVerifiedIsFalse(pageable)
            .map(Neo4jResource::toResource)

    override fun loadVerifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllVerifiedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllUnverifiedPapers(pageable)
            .map(Neo4jResource::toResource)

    /**
     * Get the "verified" flag of a paper resource.
     *
     * @param id The ID of a resource of class "Paper".
     * @return The value of the flag, or `null` if the resource is not found or not a paper.
     */
    override fun getPaperVerifiedFlag(id: ResourceId): Boolean? {
        val result = neo4jResourceRepository.findPaperByResourceId(id)
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
        neo4jResourceRepository
            .findAllFeaturedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllNonFeaturedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun loadFeaturedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByVerifiedIsTrue(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByVerifiedIsFalse(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByUnlistedIsTrue(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllByUnlistedIsFalse(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllUnlistedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository
            .findAllListedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun getFeaturedPaperFlag(id: ResourceId): Boolean =
        neo4jResourceRepository.findPaperByResourceId(id).get().featured

    override fun getUnlistedPaperFlag(id: ResourceId): Boolean =
        neo4jResourceRepository.findPaperByResourceId(id).get().unlisted

    override fun getFeaturedResourceFlag(id: ResourceId): Boolean =
        neo4jResourceRepository.findByResourceId(id).get().featured

    override fun getUnlistedResourceFlag(id: ResourceId): Boolean =
        neo4jResourceRepository.findByResourceId(id).get().unlisted

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        neo4jComparisonRepository
            .findAllFeaturedComparisons(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedComparisons(pageable: Pageable):
        Page<Resource> =
        neo4jComparisonRepository
            .findAllNonFeaturedComparsions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedComparisons(pageable: Pageable):
        Page<Resource> =
        neo4jComparisonRepository
            .findAllUnlistedComparisons(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedComparisons(pageable: Pageable):
        Page<Resource> =
        neo4jComparisonRepository
            .findAllListedComparsions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadFeaturedContributions(pageable: Pageable):
        Page<Resource> =
        neo4jContributionRepository
            .findAllFeaturedContributions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedContributions(pageable: Pageable):
        Page<Resource> =
        neo4jContributionRepository
            .findAllNonFeaturedContributions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedContributions(pageable: Pageable):
        Page<Resource> =
        neo4jContributionRepository
            .findAllUnlistedContributions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedContributions(pageable: Pageable):
        Page<Resource> =
        neo4jContributionRepository
            .findAllListedContributions(pageable)
            .map(Neo4jResource::toResource)

    override fun loadFeaturedVisualizations(pageable: Pageable):
        Page<Resource> =
        neo4jVisualizationRepository
            .findAllFeaturedVisualizations(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedVisualizations(pageable: Pageable):
        Page<Resource> =
        neo4jVisualizationRepository
            .findAllNonFeaturedVisualizations(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedVisualizations(pageable: Pageable):
        Page<Resource> =
        neo4jVisualizationRepository
            .findAllUnlistedVisualizations(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedVisualizations(pageable: Pageable):
        Page<Resource> =
        neo4jVisualizationRepository
            .findAllListedVisualizations(pageable)
            .map(Neo4jResource::toResource)

    override fun loadFeaturedSmartReviews(pageable: Pageable):
        Page<Resource> =
        neo4jSmartReviewRepository
            .findAllFeaturedSmartReviews(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedSmartReviews(pageable: Pageable):
        Page<Resource> =
        neo4jSmartReviewRepository
            .findAllNonFeaturedSmartReviews(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedSmartReviews(pageable: Pageable):
        Page<Resource> =
        neo4jSmartReviewRepository
            .findAllUnlistedSmartReviews(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedSmartReviews(pageable: Pageable):
        Page<Resource> =
        neo4jSmartReviewRepository
            .findAllListedSmartReviews(pageable)
            .map(Neo4jResource::toResource)

    override fun getFeaturedContributionFlag(id: ResourceId): Boolean =
        neo4jContributionRepository.findContributionByResourceId(id).get().featured

    override fun getUnlistedContributionFlag(id: ResourceId): Boolean? =
        neo4jContributionRepository.findContributionByResourceId(id).get().unlisted

    override fun getFeaturedComparisonFlag(id: ResourceId): Boolean =
        neo4jComparisonRepository.findComparisonByResourceId(id).get().featured

    override fun getUnlistedComparisonFlag(id: ResourceId): Boolean? =
        neo4jComparisonRepository.findComparisonByResourceId(id).get().unlisted

    override fun getFeaturedVisualizationFlag(id: ResourceId): Boolean =
        neo4jVisualizationRepository.findVisualizationByResourceId(id).get().featured

    override fun getUnlistedVisualizationFlag(id: ResourceId): Boolean? =
        neo4jVisualizationRepository.findVisualizationByResourceId(id).get().unlisted

    override fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean =
        neo4jSmartReviewRepository.findSmartReviewByResourceId(id).get().featured

    override fun getUnlistedSmartReviewFlag(id: ResourceId): Boolean? =
        neo4jSmartReviewRepository.findSmartReviewByResourceId(id).get().unlisted

    private fun setFeaturedFlag(resourceId: ResourceId, featured: Boolean): Optional<Resource> {
        val result = neo4jResourceRepository.findByResourceId(resourceId)
        if (result.isPresent) {
            val resource = result.get()
            resource.featured = featured
            return Optional.of(neo4jResourceRepository.save(resource).toResource())
        }
        return Optional.empty()
    }

    private fun setVerifiedFlag(resourceId: ResourceId, verified: Boolean): Optional<Resource> {
        val result = neo4jResourceRepository.findByResourceId(resourceId)
        if (result.isPresent) {
            val resource = result.get()
            resource.verified = verified
            return Optional.of(neo4jResourceRepository.save(resource).toResource())
        }
        return Optional.empty()
    }

    private fun setUnlistedFlag(resourceId: ResourceId, unlisted: Boolean): Optional<Resource> {
        val result = neo4jResourceRepository.findByResourceId(resourceId)
            if (result.isPresent) {
                val resource = result.get()
                resource.unlisted = unlisted
                return Optional.of(neo4jResourceRepository.save(resource).toResource())
            }
            return Optional.empty()
    }

    private fun String.toSearchString() = "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() = "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
