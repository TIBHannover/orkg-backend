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

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabelContaining(
        pageable: Pageable,
        ids: Array<ClassId>,
        part: String
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

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("Not yet implemented")
    }
}
