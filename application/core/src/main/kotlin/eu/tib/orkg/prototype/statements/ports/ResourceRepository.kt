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

    fun nextIdentity(): ResourceId

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
    fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Resource>

    fun findAllByLabelExactly(label: String): Iterable<Resource>

    fun findByLabelExactly(label: String): Optional<Resource>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource>

    /**
     * Find all resources belonging to a given class.
     */
    fun findAllByClass(id: ClassId, pageable: Pageable): Page<Resource>

    /**
     * Find all resources belonging to a given class and a creator.
     */
    fun findAllByClassAndCreatedBy(id: ClassId, createdBy: ContributorId, pageable: Pageable): Page<Resource>

    /**
     * Find all resources belonging to a given class and matching a label.
     */
    fun findAllByClassAndLabel(id: ClassId, label: String, pageable: Pageable): Page<Resource>

    /**
     * Find all resources belonging to a given class and matching a label and created by a creator.
     */
    fun findAllByClassAndLabelAndCreatedBy(
        id: ClassId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource>

    /**
     * Find all resources belonging to a given class and containing a label.
     */
    fun findAllByClassAndLabelContaining(id: ClassId, part: String, pageable: Pageable): Page<Resource>

    /**
     * Find all resources belonging to a given class and containing a label and created by a creator.
     */
    fun findAllByClassAndLabelContainingAndCreatedBy(
        id: ClassId,
        part: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class.
     */
    fun findAllExcludingClass(ids: Array<ClassId>, pageable: Pageable): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class and matching a label.
     */
    fun findAllExcludingClassByLabel(ids: Array<ClassId>, label: String, pageable: Pageable): Page<Resource>

    /**
     * Find all resources except the ones belonging to a given class and containing a label.
     */
    fun findAllExcludingClassByLabelContaining(ids: Array<ClassId>, part: String, pageable: Pageable): Page<Resource>

    fun findByDOI(doi: String): Optional<Resource>

    fun findByTitle(title: String?): Optional<Resource>

    fun findAllByDOI(doi: String): Iterable<Resource>

    fun findAllByTitle(title: String?): Iterable<Resource>

    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findPaperByResourceId(id: ResourceId): Optional<Resource>

    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>

    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>

    fun delete(id: ResourceId)

    fun findAllVerifiedResources(pageable: Pageable): Page<Resource>

    fun findAllUnverifiedResources(pageable: Pageable): Page<Resource>

    fun findAllVerifiedPapers(pageable: Pageable): Page<Resource>

    fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource>

    fun checkIfResourceHasStatements(id: ResourceId): Boolean

    data class ResourceContributors(
        val id: String,
        val createdBy: String,
        val createdAt: String
    )
}
