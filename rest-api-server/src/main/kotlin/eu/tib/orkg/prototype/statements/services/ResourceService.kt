package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jComparisonRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jContributionRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jSmartReviewRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jVisualizationRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResourceService(
    private val neo4jComparisonRepository: Neo4jComparisonRepository,
    private val neo4jContributionRepository: Neo4jContributionRepository,
    private val neo4jVisualizationRepository: Neo4jVisualizationRepository,
    private val neo4jSmartReviewRepository: Neo4jSmartReviewRepository,
    private val repository: ResourceRepository
) : ResourceUseCases {

    override fun create(label: String) = create(
        ContributorId.createUnknownContributor(),
        label,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource {
        var resourceId = repository.nextIdentity()

        // Should be moved to the Generator in the future
        while (repository.findByResourceId(resourceId).isPresent) {
            resourceId = repository.nextIdentity()
        }

        val newResource = Resource(
            label = label,
            id = resourceId,
            createdBy = userId,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            createdAt = OffsetDateTime.now(),
        )
        return repository.save(newResource)
    }

    override fun create(request: CreateResourceRequest) = create(
        ContributorId.createUnknownContributor(),
        request,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(
        userId: ContributorId,
        request: CreateResourceRequest,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource {
        val id = request.id ?: repository.nextIdentity()
        val resource = Resource(
            label = request.label,
            id = id,
            createdBy = userId,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            createdAt = OffsetDateTime.now(),
            classes = request.classes
        )
        return repository.save(resource)
    }

    override fun findAll(pageable: Pageable): Page<Resource> = repository.findAll(pageable)

    override fun findById(id: ResourceId?): Optional<Resource> = repository.findByResourceId(id)

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Resource> =
        repository.findAllByLabelMatchesRegex(
            label.toExactSearchString(), pageable
        )

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource> {
        return repository.findAllByLabelMatchesRegex(
            part.toSearchString(), pageable
        )
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource> =
        repository.findAllByClass(id.toString(), pageable)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource> =
        repository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource> =
        repository.findAllByClassAndLabel(id.toString(), label, pageable)

    override fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<Resource> = repository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource> =
        repository.findAllByClassAndLabelContaining(id.toString(), part.toSearchString(), pageable)

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<Resource> = repository.findAllByClassAndLabelContainingAndCreatedBy(
        id.toString(), part.toSearchString(), createdBy, pageable
    )

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource> =
        repository.findAllExcludingClass(ids.map { it.value }, pageable)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource> =
        repository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable)

    override fun findAllExcludingClassByLabelContaining(
        pageable: Pageable,
        ids: Array<ClassId>,
        part: String
    ): Page<Resource> = repository.findAllExcludingClassByLabelContaining(
        ids.map { it.value }, part.toSearchString(), pageable
    )

    override fun findByDOI(doi: String): Optional<Resource> = repository.findByDOI(doi)

    override fun findByTitle(title: String?): Optional<Resource> = repository.findByLabel(title)

    override fun findAllByDOI(doi: String): Iterable<Resource> = repository.findAllByDOI(doi)

    override fun findAllByTitle(title: String?): Iterable<Resource> = repository.findAllByLabel(title!!)

    override fun findAllByFeatured(pageable: Pageable): Page<Resource> = repository.findAllByFeaturedIsTrue(pageable)

    override fun findAllByNonFeatured(pageable: Pageable): Page<Resource> =
        repository.findAllByFeaturedIsFalse(pageable)

    override fun findAllByUnlisted(pageable: Pageable): Page<Resource> = repository.findAllByUnlistedIsTrue(pageable)

    override fun findAllByListed(pageable: Pageable): Page<Resource> = repository.findAllByUnlistedIsFalse(pageable)

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        repository.findPapersByObservatoryId(id)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        repository.findComparisonsByObservatoryId(id)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        repository.findProblemsByObservatoryId(id)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        repository.findContributorsByResourceId(id)

    override fun update(request: UpdateResourceRequest): Resource {
        // already checked by service
        var found = repository.findByResourceId(request.id).get()

        // update all the properties
        if (request.label != null) found = found.copy(label = request.label)
        if (request.classes != null) found = found.copy(classes = request.classes)

        return repository.save(found)
    }

    override fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): Resource {
        var found = repository.findByResourceId(id).get()
        found = found.copy(observatoryId = request.observatoryId)
        found = found.copy(organizationId = request.organizationId)
        return repository.save(found)
    }

    override fun hasStatements(id: ResourceId) = repository.checkIfResourceHasStatements(id)

    override fun delete(id: ResourceId) {
        val found = repository.findByResourceId(id).get()
        repository.delete(found.id!!)
    }

    override fun removeAll() = repository.deleteAll()

    override fun getResourcesByClasses(
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        return if (classes.isNotEmpty()) {
            when (featured) {
                null -> repository.findAllFeaturedResourcesByClass(
                    classes, unlisted, pageable
                )
                else -> repository.findAllFeaturedResourcesByClass(
                    classes, featured, unlisted, pageable
                )
            }
        } else {
            Page.empty()
        }
    }

    override fun markAsVerified(resourceId: ResourceId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ResourceId) = setVerifiedFlag(resourceId, false)

    override fun loadVerifiedResources(pageable: Pageable): Page<Resource> =
        repository.findAllByVerifiedIsTrue(pageable)

    override fun loadUnverifiedResources(pageable: Pageable): Page<Resource> =
        repository.findAllByVerifiedIsFalse(pageable)

    override fun loadVerifiedPapers(pageable: Pageable): Page<Resource> = repository.findAllVerifiedPapers(pageable)

    override fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> = repository.findAllUnverifiedPapers(pageable)

    /**
     * Get the "verified" flag of a paper resource.
     *
     * @param id The ID of a resource of class "Paper".
     * @return The value of the flag, or `null` if the resource is not found or not a paper.
     */
    override fun getPaperVerifiedFlag(id: ResourceId): Boolean? {
        val result = repository.findPaperByResourceId(id)
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

    override fun loadFeaturedPapers(pageable: Pageable): Page<Resource> = repository.findAllFeaturedPapers(pageable)

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllNonFeaturedPapers(pageable)

    override fun loadFeaturedResources(pageable: Pageable): Page<Resource> =
        repository.findAllByVerifiedIsTrue(pageable)

    override fun loadNonFeaturedResources(pageable: Pageable): Page<Resource> =
        repository.findAllByVerifiedIsFalse(pageable)

    override fun loadUnlistedResources(pageable: Pageable): Page<Resource> =
        repository.findAllByUnlistedIsTrue(pageable)

    override fun loadListedResources(pageable: Pageable): Page<Resource> = repository.findAllByUnlistedIsFalse(pageable)

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> = repository.findAllUnlistedPapers(pageable)

    override fun loadListedPapers(pageable: Pageable): Page<Resource> = repository.findAllListedPapers(pageable)

    override fun getFeaturedPaperFlag(id: ResourceId): Boolean {
        val result = repository.findPaperByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedPaperFlag(id: ResourceId): Boolean {
        val result = repository.findPaperByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun getFeaturedResourceFlag(id: ResourceId): Boolean {
        val result = repository.findByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedResourceFlag(id: ResourceId): Boolean {
        val result = repository.findByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        neo4jComparisonRepository.findAllFeaturedComparisons(pageable).map(Neo4jResource::toResource)

    override fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        neo4jComparisonRepository.findAllNonFeaturedComparsions(pageable).map(Neo4jResource::toResource)

    override fun loadUnlistedComparisons(pageable: Pageable): Page<Resource> =
        neo4jComparisonRepository.findAllUnlistedComparisons(pageable).map(Neo4jResource::toResource)

    override fun loadListedComparisons(pageable: Pageable): Page<Resource> =
        neo4jComparisonRepository.findAllListedComparsions(pageable).map(Neo4jResource::toResource)

    override fun loadFeaturedContributions(pageable: Pageable): Page<Resource> =
        neo4jContributionRepository.findAllFeaturedContributions(pageable).map(Neo4jResource::toResource)

    override fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource> =
        neo4jContributionRepository.findAllNonFeaturedContributions(pageable).map(Neo4jResource::toResource)

    override fun loadUnlistedContributions(pageable: Pageable): Page<Resource> =
        neo4jContributionRepository.findAllUnlistedContributions(pageable).map(Neo4jResource::toResource)

    override fun loadListedContributions(pageable: Pageable): Page<Resource> =
        neo4jContributionRepository.findAllListedContributions(pageable).map(Neo4jResource::toResource)

    override fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jVisualizationRepository.findAllFeaturedVisualizations(pageable).map(Neo4jResource::toResource)

    override fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jVisualizationRepository.findAllNonFeaturedVisualizations(pageable).map(Neo4jResource::toResource)

    override fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jVisualizationRepository.findAllUnlistedVisualizations(pageable).map(Neo4jResource::toResource)

    override fun loadListedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jVisualizationRepository.findAllListedVisualizations(pageable).map(Neo4jResource::toResource)

    override fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jSmartReviewRepository.findAllFeaturedSmartReviews(pageable).map(Neo4jResource::toResource)

    override fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jSmartReviewRepository.findAllNonFeaturedSmartReviews(pageable).map(Neo4jResource::toResource)

    override fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jSmartReviewRepository.findAllUnlistedSmartReviews(pageable).map(Neo4jResource::toResource)

    override fun loadListedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jSmartReviewRepository.findAllListedSmartReviews(pageable).map(Neo4jResource::toResource)

    override fun getFeaturedContributionFlag(id: ResourceId): Boolean {
        val result = neo4jContributionRepository.findContributionByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedContributionFlag(id: ResourceId): Boolean {
        val result = neo4jContributionRepository.findContributionByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun getFeaturedComparisonFlag(id: ResourceId): Boolean {
        val result = neo4jComparisonRepository.findComparisonByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedComparisonFlag(id: ResourceId): Boolean {
        val result = neo4jComparisonRepository.findComparisonByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun getFeaturedVisualizationFlag(id: ResourceId): Boolean {
        val result = neo4jVisualizationRepository.findVisualizationByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedVisualizationFlag(id: ResourceId): Boolean {
        val result = neo4jVisualizationRepository.findVisualizationByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun getFeaturedSmartReviewFlag(id: ResourceId): Boolean {
        val result = neo4jSmartReviewRepository.findSmartReviewByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedSmartReviewFlag(id: ResourceId): Boolean {
        val result = neo4jSmartReviewRepository.findSmartReviewByResourceId(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    private fun setFeaturedFlag(resourceId: ResourceId, featured: Boolean): Optional<Resource> {
        val result = repository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(featured = featured)
        return Optional.of(repository.save(resultObj))
    }

    private fun setVerifiedFlag(resourceId: ResourceId, verified: Boolean): Optional<Resource> {
        val result = repository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(verified = verified)
        return Optional.of(repository.save(resultObj))
    }

    private fun setUnlistedFlag(resourceId: ResourceId, unlisted: Boolean): Optional<Resource> {
        val result = repository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound(resourceId.value) }
        resultObj = resultObj.copy(unlisted = unlisted)
        return Optional.of(repository.save(resultObj))
    }

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
