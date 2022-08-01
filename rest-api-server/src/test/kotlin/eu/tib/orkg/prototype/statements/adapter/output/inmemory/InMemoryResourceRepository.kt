package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.toExactSearchString
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class InMemoryResourceRepository : ResourceRepository {

    private val idCounter = AtomicLong(1)

    private val entities = mutableMapOf<ResourceId, Resource>()

    override fun findAll(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllFeaturedResourcesByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllNonFeaturedResourcesByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllUnlistedResourcesByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun nextIdentity(): ResourceId = ResourceId(idCounter.getAndIncrement())

    override fun save(resource: Resource): Resource {
        entities[resource.id!!] = resource.copy() // Simulate retrieving a new instance by copying
        return findByResourceId(resource.id).get() // We know it is there
    }

    override fun delete(id: ResourceId) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun findByResourceId(id: ResourceId?): Optional<Resource> = Optional.ofNullable(entities[id])

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Resource> {
        val labels = entities
            .filter { (id, entry) -> entry.label.matches(label.toExactSearchString().toRegex()) }
            .map { (id, entry) -> entry }
            .take(pageable.pageSize)
        return PageImpl(labels, pageable, labels.size.toLong())
    }

    override fun findAllByLabel(label: String): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClass(`class`: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndCreatedBy(
        `class`: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContaining(`class`: String, label: String, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabel(
        classes: List<String>,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllExcludingClassByLabelContaining(
        classes: List<String>,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long> {
        TODO("Not yet implemented")
    }

    override fun findByDOI(doi: String): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByDOI(doi: String): Iterable<Resource> {
        TODO("Not yet implemented")
    }

    override fun findByLabel(label: String?): Optional<Resource> {
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

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceRepository.ResourceContributors> {
        TODO("Not yet implemented")
    }

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findPaperByResourceId(id: ResourceId): Optional<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllVerifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllFeaturedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllNonFeaturedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllUnlistedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllListedPapers(pageable: Pageable): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllFeaturedResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }

    override fun findAllResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        TODO("Not yet implemented")
    }
}
