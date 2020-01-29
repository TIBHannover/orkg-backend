package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface ResourceService {
    /**
     * Create a new resource with a given label.
     *
     * @return the newly created resource
     */
    fun create(label: String): Resource

    /**
     * Create a new resource with a given label belonging to a given user.
     */
    fun create(userId: UUID, label: String): Resource

    /**
     * Create a new resource from a request.
     */
    fun create(request: CreateResourceRequest): Resource

    /**
     * Create a new resource belonging to a given user.
     */
    fun create(userId: UUID, request: CreateResourceRequest): Resource

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
     * Find all resources belonging to a given class and matching a label.
     */
    fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and containing a label.
     */
    fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class.
     */
    fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and matching a label.
     */
    fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Iterable<Resource>

    /**
     * Find all resources belonging to a given class and containing a label.
     */
    fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Iterable<Resource>

    fun findByDOI(doi: String): Optional<Resource>

    fun findByTitle(title: String?): Optional<Resource>

    /**
     * Update a resource.
     */
    fun update(request: UpdateResourceRequest): Resource
}
