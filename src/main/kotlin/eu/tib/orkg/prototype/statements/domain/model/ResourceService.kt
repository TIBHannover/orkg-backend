package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import java.util.Optional
import org.springframework.data.domain.Pageable

// FIXME: acts as port and adapter now -> separate!
interface ResourceService : MarkAsVerifiedUseCase, LoadResourcePort {
    /**
     * Create a new resource with a given label.
     *
     * @return the newly created resource
     */
    fun create(label: String): Resource

    /**
     * Create a new resource with a given label belonging to a given user.
     */
    fun create(userId: ContributorId, label: String, observatoryId: ObservatoryId, extractionMethod: ExtractionMethod, organizationId: OrganizationId): Resource

    /**
     * Create a new resource from a request.
     */
    fun create(request: CreateResourceRequest): Resource

    /**
     * Create a new resource belonging to a given user.
     */
    fun create(userId: ContributorId, request: CreateResourceRequest, observatoryId: ObservatoryId, extractionMethod: ExtractionMethod, organizationId: OrganizationId): Resource

    /**
     * Find all resources.
     */
    fun findAll(pageable: Pageable): Iterable<Resource>

    /**
     * Find a resource by its ID.
     */
    fun findById(id: ResourceId?): Optional<Resource>

    /**
     * Find all resources matching a label.
     */
    fun findAllByLabel(pageable: Pageable, label: String): Iterable<Resource>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class.
     */
    fun findAllByClass(pageable: Pageable, id: ClassId): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and a creator.
     */
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and matching a label.
     */
    fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and matching a label and created by a creator.
     */
    fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: ContributorId): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and containing a label.
     */
    fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and containing a label and created by a creator.
     */
    fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: ContributorId): Iterable<Resource>

    /**
     * Find all resources except the ones belonging to a given class.
     */
    fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Iterable<Resource>

    /**
     * Find all resources except the ones belonging to a given class and matching a label.
     */
    fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Iterable<Resource>

    /**
     * Find all resources except the ones belonging to a given class and containing a label.
     */
    fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Iterable<Resource>

    fun findByDOI(doi: String): Optional<Resource>

    fun findByTitle(title: String?): Optional<Resource>

    fun findAllByDOI(doi: String): Iterable<Resource>

    fun findAllByTitle(title: String?): Iterable<Resource>

    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>

    /**
     * Update a resource.
     */
    fun update(request: UpdateResourceRequest): Resource

    fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId, userId: ContributorId): Resource

    fun hasStatements(id: ResourceId): Boolean

    fun delete(id: ResourceId)
}
