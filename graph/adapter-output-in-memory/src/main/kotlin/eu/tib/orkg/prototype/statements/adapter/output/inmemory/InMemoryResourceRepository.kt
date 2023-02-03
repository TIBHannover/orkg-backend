package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

private val paperClass = ThingId("Paper")
private val paperDeletedClass = ThingId("PaperDeleted")
private val comparisonClass = ThingId("Comparison")
private val unknownUUID = UUID(0, 0)

class InMemoryResourceRepository : InMemoryRepository<ResourceId, Resource>(
    compareBy(Resource::createdAt)
), ResourceRepository {
    override fun findByIdAndClasses(id: ResourceId, classes: Set<ThingId>): Resource? =
        entities[id]?.takeIf { it.classes.any {`class` -> `class` in classes } }

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

    // TODO: rename to findAllPapersByLabel or replace with a generic method with a classes parameter
    override fun findAllByLabel(label: String) =
        entities.values.filter { it.label == label && paperClass in it.classes && paperDeletedClass !in it.classes}

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.matches(Regex(label)) }

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.contains(part) }

    override fun findAllByClass(`class`: String, pageable: Pageable): Page<Resource> {
        val classId = ThingId(`class`)
        return findAllFilteredAndPaged(pageable) { it.classes.contains(classId) }
    }

    override fun findAllByClassAndCreatedBy(
        `class`: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ThingId(`class`)
        return findAllFilteredAndPaged(pageable) {
            it.createdBy == createdBy && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Resource> {
        val classId = ThingId(`class`)
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
        val classId = ThingId(`class`)
        return findAllFilteredAndPaged(pageable) {
            it.createdBy == createdBy && it.label == label && it.classes.contains(classId)
        }
    }

    override fun findAllByClassAndLabelMatchesRegex(
        `class`: String,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        val classId = ThingId(`class`)
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
        val classId = ThingId(`class`)
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.contains(classId) && it.createdBy == createdBy
        }
    }

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.classes.containsAll(includeClasses) && it.classes.none { id ->
            excludeClasses.contains(id)
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: String,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.label == label && it.classes.containsAll(includeClasses) && it.classes.none { id ->
            excludeClasses.contains(id)
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
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

    // TODO: rename to findPaperByLabel or replace with a generic method with a classes parameter
    override fun findByLabel(label: String?) =
        Optional.ofNullable(entities.values.firstOrNull { it.label == label })

    // TODO: rename to findAllByClassAndObservatoryId
    override fun findByClassAndObservatoryId(`class`: String, id: ObservatoryId): Iterable<Resource> {
        val classId = ThingId(`class`)
        return entities.values.filter { classId in it.classes && it.observatoryId == id }
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
        classes: Set<ThingId>? = null,
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

    // TODO: Check if usage is correct because name is findAllFeaturedResourcesByClass and not findAllUnlistedResourcesByClass
    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.unlisted == unlisted && it.classes.any { id ->
            id.value in classes
        }
    }

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.unlisted == unlisted  && it.featured == featured && it.classes.any { id ->
            id.value in classes
        }
    }

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

    override fun findAllContributorIds(pageable: Pageable) =
        entities.values
            .map { it.createdBy }
            .distinct()
            .filter { it.value != unknownUUID }
            .sortedBy { it.value.toString() }
            .paged(pageable)

    // TODO: Create a method with a generic class parameter
    override fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) {
            comparisonClass in it.classes && it.organizationId == id
        }
}
