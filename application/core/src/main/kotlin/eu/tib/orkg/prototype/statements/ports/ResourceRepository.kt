package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ResourceRepository {
    fun save(resource: Resource): Resource

    fun count(): Long

    /**
     * Find all resources.
     */
    fun findAll(pageable: Pageable): Page<Resource>

    /**
     * Find a resource by its ID.
     */
    fun findById(resourceId: ResourceId?): Optional<Resource>

    /**
     * Find all resources matching a label.
     */
    fun findAllByLabel(pageable: Pageable, label: String): Page<Resource>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource>

    /**
     * Find all resources belonging to a given class.
     */
    fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource>

    /**
     * Find all resources belonging to a given class and a creator.
     */
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource>

    /**
     * Find all resources belonging to a given class and matching a label.
     */
    fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource>

    /**
     * Find all resources belonging to a given class and matching a label and created by a creator.
     */
    fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: ContributorId): Page<Resource>

    /**
     * Find all resources belonging to a given class and containing a label.
     */
    fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource>

    /**
     * Find all resources belonging to a given class and containing a label and created by a creator.
     */
    fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: ContributorId): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class.
     */
    fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class and matching a label.
     */
    fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class and containing a label.
     */
    fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Page<Resource>

    fun findByDOI(doi: String): Optional<Resource>

    fun findByTitle(title: String?): Optional<Resource>

    fun findAllByDOI(doi: String): Iterable<Resource>

    fun findAllByTitle(title: String?): Iterable<Resource>

    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>
}
