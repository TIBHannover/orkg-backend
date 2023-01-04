package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import java.util.function.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

private val paperClass = ClassId("Paper")

class InMemoryResourceRepository : InMemoryRepository<ResourceId, Resource>(
    compareBy(Resource::createdAt)
), ResourceRepository {
    override fun findByIdAndClasses(id: ResourceId, classes: Set<ClassId>): Resource? =
        entities[id]?.takeIf { it.classes.containsAll(classes) }

    override fun nextIdentity(): ResourceId {
        var id = ResourceId(entities.size.toLong())
        while(id in entities) {
            id = ResourceId(id.value.toLong() + 1)
        }
        return id
    }

    override fun save(resource: Resource) {
        entities[resource.id!!] = resource
    }

    override fun deleteByResourceId(id: ResourceId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findByResourceId(id: ResourceId?) =
        Optional.ofNullable(entities[id])

    override fun findAllByLabel(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label == label }

    override fun findAllByLabel(label: String) =
        entities.values.filter { it.label == label }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.matches(Regex(label)) }

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.contains(part) }

    override fun findAllByClass(`class`: String, pageable: Pageable): Page<Resource> {
        val classId = ClassId(`class`)
        return findAllFilteredAndPaged(pageable) { it.classes.contains(classId) }
    }

    override fun findAllByClassAndCreatedBy(
        `class`: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ClassId(`class`)
        return findAllFilteredAndPaged(pageable) {
            it.createdBy == createdBy && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Resource> {
        val classId = ClassId(`class`)
        return findAllFilteredAndPaged(pageable) {
            it.label == label && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ClassId(`class`)
        return findAllFilteredAndPaged(pageable) {
            it.createdBy == createdBy && it.label == label && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabelMatchesRegex(
        `class`: String,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ClassId(`class`)
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabelMatchesRegexAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ClassId(`class`)
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.contains(classId) && it.createdBy == createdBy
        }
    }

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.classes.containsAll(includeClasses) && it.classes.none { id ->
            excludeClasses.contains(id)
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        label: String,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.label == label && it.classes.containsAll(includeClasses) && it.classes.none { id ->
            excludeClasses.contains(id)
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.containsAll(includeClasses) && it.classes.none { id ->
                excludeClasses.contains(id)
            }
        }
    }

    // TODO: rename to countIncomingStatements
    override fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long> {
        TODO("This method should be moved to the StatementRepository")
    }

    override fun findByDOI(doi: String): Optional<Resource> {
        TODO("This method should be moved to the StatementRepository (or PaperRepository?)")
    }

    override fun findAllByDOI(doi: String): Iterable<Resource> {
        TODO("This method should be moved to the StatementRepository (or PaperRepository?)")
    }

    override fun findByLabel(label: String?) =
        Optional.ofNullable(entities.values.firstOrNull { it.label == label })

    // TODO: rename to findAllByClassAndObservatoryId
    override fun findByClassAndObservatoryId(`class`: String, id: ObservatoryId): Iterable<Resource> {
        val classId = ClassId(`class`)
        return entities.values.filter { classId in it.classes && it.observatoryId == id }
    }

    // TODO: rename to findAllProblemsByObservatoryId
    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        TODO("This method should be moved to the StatementRepository (or ProblemRepository?)")
    }

    // TODO: rename to findAllContributorsByResourceId
    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceRepository.ResourceContributors> {
        TODO("This method should be moved to the StatementRepository")
    }

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean {
        TODO("This method should be moved to the StatementRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByVerifiedIsTrue(pageable: Pageable) =
        findAllByClassAndFlags(pageable, verified = true)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByVerifiedIsFalse(pageable: Pageable) =
        findAllByClassAndFlags(pageable, verified = false)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByFeaturedIsTrue(pageable: Pageable) =
        findAllByClassAndFlags(pageable, featured = true)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByFeaturedIsFalse(pageable: Pageable) =
        findAllByClassAndFlags(pageable, featured = false)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByUnlistedIsTrue(pageable: Pageable) =
        findAllByClassAndFlags(pageable, unlisted = true)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllByUnlistedIsFalse(pageable: Pageable) =
        findAllByClassAndFlags(pageable, unlisted = false)

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findPaperByResourceId(id: ResourceId) =
        Optional.ofNullable(entities.values.firstOrNull {
            it.id == id && paperClass in it.classes
        })

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllVerifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllFeaturedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllNonFeaturedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllUnlistedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findAllListedPapers(pageable: Pageable): Page<Resource> {
        TODO("This method should be moved to a PaperRepository")
    }

    private fun findAllByClassAndFlags(
        pageable: Pageable,
        classes: Set<ClassId>? = null,
        unlisted: Boolean? = null,
        featured: Boolean? = null,
        verified: Boolean? = null
    ) = findAllFilteredAndPaged(pageable) predicate@ {
            if (!classes.isNullOrEmpty() && !it.classes.containsAll(classes)) {
                return@predicate false
            }
            if (unlisted != null && (it.unlisted == true) != unlisted) {
                return@predicate false
            }
            if (featured != null && (it.featured == true) != featured) {
                return@predicate false
            }
            if (verified != null && (it.verified == true) != verified) {
                return@predicate false
            }
            true
        }

    // TODO: Create a method with featured and unlisted parameter (see above)
    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllByClassAndFlags(pageable, classes = classes.map(::ClassId).toSet(), unlisted = unlisted)

    // TODO: Create a method with featured and unlisted parameter (see 2 above)
    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllByClassAndFlags(pageable, classes = classes.map(::ClassId).toSet(), unlisted = unlisted, featured = featured)

    override fun findAllFeaturedResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.unlisted == unlisted && it.featured == featured && it.observatoryId == id && it.classes.any { id ->
            id.value in classes
        }
    }

    override fun findAllResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.unlisted == unlisted && it.observatoryId == id && it.classes.any { id ->
            id.value in classes
        }
    }
}
