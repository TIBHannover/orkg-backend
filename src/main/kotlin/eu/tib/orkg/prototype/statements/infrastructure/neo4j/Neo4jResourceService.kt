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
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
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
        val resourceId = neo4jResourceIdGenerator.nextIdentity()
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

    override fun findAll(pageable: Pageable): Iterable<Resource> =
        neo4jResourceRepository
            .findAll(pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findById(id: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(id)
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(pageable: Pageable, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Resource> {
        return neo4jResourceRepository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: ContributorId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), part.toSearchString(), pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: ContributorId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id.toString(), part.toSearchString(), createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.map { it.value }, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(ids.map { it.value }, part.toSearchString(), pageable)
            .content
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

    private fun setVerifiedFlag(resourceId: ResourceId, verified: Boolean): Optional<Resource> {
        val result = neo4jResourceRepository.findByResourceId(resourceId)
        if (result.isPresent) {
            val resource = result.get()
            resource.verified = verified
            return Optional.of(neo4jResourceRepository.save(resource).toResource())
        }
        return Optional.empty()
    }

    private fun String.toSearchString() = "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() = "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
