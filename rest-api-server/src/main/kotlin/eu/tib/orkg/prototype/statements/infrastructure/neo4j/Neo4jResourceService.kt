package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository.ResourceContributors
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResourceService(
    private val resourceRepository: ResourceRepository
) : ResourceService {

    override fun create(label: String) = create(
        ContributorId.createUnknownContributor(),
        label,
        ObservatoryId.createUnknownObservatory(),
        ExtractionMethod.UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(userId: ContributorId, label: String, observatoryId: ObservatoryId, extractionMethod: ExtractionMethod, organizationId: OrganizationId): Resource {
        var resourceId = resourceRepository.nextIdentity()

        // Should be moved to the Generator in the future
        while (resourceRepository.findById(resourceId).isPresent) {
            resourceId = resourceRepository.nextIdentity()
        }
        val resource = Resource(
            id = resourceId,
            label = label,
            createdBy = userId,
            observatoryId = observatoryId,
            organizationId = organizationId,
            extractionMethod = extractionMethod
        )
        return resourceRepository.save(resource)
    }

    override fun create(request: CreateResourceRequest) = create(
        ContributorId.createUnknownContributor(),
        request,
        ObservatoryId.createUnknownObservatory(),
        ExtractionMethod.UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(
        userId: ContributorId,
        request: CreateResourceRequest,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource {
        val id = request.id ?: resourceRepository.nextIdentity()
        val resource = Resource(
            label = request.label,
            id = id,
            createdBy = userId,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            classes = request.classes
        )
        return resourceRepository.save(resource)
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        resourceRepository.findAll(pageable)

    override fun findById(id: ResourceId?): Optional<Resource> =
        resourceRepository.findById(id)

    override fun findAllByLabelExactly(pageable: Pageable, label: String): Page<Resource> =
        resourceRepository.findAllByLabelExactly(label, pageable) // TODO: See declaration

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource> {
        return resourceRepository.findAllByLabelContaining(part, pageable) // TODO: See declaration
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource> =
        resourceRepository.findAllByClass(id, pageable)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource> =
        resourceRepository.findAllByClassAndCreatedBy(id, createdBy, pageable)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource> =
        resourceRepository.findAllByClassAndLabel(id, label, pageable)

    override fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: ContributorId): Page<Resource> =
        resourceRepository.findAllByClassAndLabelAndCreatedBy(id, label, createdBy, pageable)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource> =
        resourceRepository.findAllByClassAndLabelContaining(id, part, pageable)

    override fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: ContributorId): Page<Resource> =
        resourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id, part, createdBy, pageable)

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource> =
        resourceRepository.findAllExcludingClass(ids, pageable)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource> =
        resourceRepository.findAllExcludingClassByLabel(ids, label, pageable)

    override fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Page<Resource> =
        resourceRepository.findAllExcludingClassByLabelContaining(ids, part, pageable)

    override fun findByDOI(doi: String): Optional<Resource> =
        resourceRepository.findByDOI(doi)

    // TODO: really single result? exact match?
    override fun findByTitle(title: String?): Optional<Resource> =
        resourceRepository.findByLabelExactly(title!!)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        resourceRepository.findAllByDOI(doi)

    // FIXME: should be pageable
    override fun findAllByTitle(title: String?): Iterable<Resource> =
        resourceRepository.findAllByLabelExactly(title!!)

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        resourceRepository.findPapersByObservatoryId(id)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        resourceRepository.findComparisonsByObservatoryId(id)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        resourceRepository.findProblemsByObservatoryId(id)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        resourceRepository.findContributorsByResourceId(id)

    override fun update(request: UpdateResourceRequest): Resource {
        // already checked by service
        var found = resourceRepository.findById(request.id).get()

        // update all the properties
        if (request.label != null)
            found = found.copy(label = request.label)
        if (request.classes != null)
            found = found.copy(classes = request.classes)

        return resourceRepository.save(found)
    }

    override fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): Resource {
        var found = resourceRepository.findById(id).get()

        found = found.copy(observatoryId = request.observatoryId)
        found = found.copy(organizationId = request.organizationId)

        return resourceRepository.save(found)
    }

    override fun hasStatements(id: ResourceId) =
        resourceRepository.checkIfResourceHasStatements(id)

    override fun delete(id: ResourceId) {
        val found = resourceRepository.findById(id).get()
        resourceRepository.delete(found.id!!)
    }

    override fun removeAll() = Unit

    override fun markAsVerified(resourceId: ResourceId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ResourceId) = setVerifiedFlag(resourceId, false)

    override fun loadVerifiedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllVerifiedResources(pageable)

    override fun loadUnverifiedResources(pageable: Pageable): Page<Resource> =
        resourceRepository.findAllUnverifiedResources(pageable)

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
            return result.get().verified
        }
        return null
    }

    // TODO: This should go to the Resource -> behavior
    private fun setVerifiedFlag(resourceId: ResourceId, verified: Boolean): Optional<Resource> {
        val result = resourceRepository.findById(resourceId)
        if (result.isPresent) {
            val resource = result.get().copy(verified = verified)
            return Optional.of(resourceRepository.save(resource))
        }
        return Optional.empty()
    }
}
