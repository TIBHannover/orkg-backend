package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

class InMemoryResourceRepository : ResourceRepository {

    private val resources: MutableSet<Resource> = mutableSetOf()

    override fun nextIdentity(): ResourceId {
        TODO("Not yet implemented")
    }

    override fun save(resource: Resource): Resource {
        resources.add(resource)
        return resource
    }

    override fun count(): Long {
        return resources.size.toLong()
    }

    override fun findAll(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findById(resourceId: ResourceId?): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelExactly(label: String): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findByLabelExactly(label: String): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClass(id: ClassId, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndCreatedBy(id: ClassId, createdBy: ContributorId, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabel(id: ClassId, label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelAndCreatedBy(
        id: ClassId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContaining(id: ClassId, part: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        id: ClassId,
        part: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClass(ids: Array<ClassId>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabel(ids: Array<ClassId>, label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabelContaining(
        ids: Array<ClassId>,
        part: String,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findByDOI(doi: String): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findByTitle(title: String?): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByDOI(doi: String): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByTitle(title: String?): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findPaperByResourceId(id: ResourceId): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceRepository.ResourceContributors> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ResourceId) {
        TODO("Not yet implemented")
    }

    override fun findAllVerifiedResources(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllUnverifiedResources(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllVerifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean {
        TODO("Not yet implemented")
    }
}
