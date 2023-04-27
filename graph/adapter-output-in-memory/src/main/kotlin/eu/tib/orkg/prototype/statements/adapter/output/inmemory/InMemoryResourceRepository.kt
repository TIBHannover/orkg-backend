package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

private val paperClass = ThingId("Paper")
private val paperDeletedClass = ThingId("PaperDeleted")
private val comparisonClass = ThingId("Comparison")
private val unknownUUID = UUID(0, 0)

class InMemoryResourceRepository : InMemoryRepository<ThingId, Resource>(
    compareBy(Resource::createdAt)
), ResourceRepository {
    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource? =
        entities[id]?.takeIf { it.classes.any {`class` -> `class` in classes } }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("R$count")
        while(id in entities) {
            id = ThingId("R${++count}")
        }
        return id
    }

    override fun save(resource: Resource) {
        entities[resource.id] = resource
    }

    override fun deleteByResourceId(id: ThingId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findByResourceId(id: ThingId) =
        Optional.ofNullable(entities[id])

    // TODO: rename to findAllPapersByLabel or replace with a generic method with a classes parameter
    override fun findAllByLabel(label: String) =
        entities.values.filter { it.label == label && paperClass in it.classes && paperDeletedClass !in it.classes}

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.matches(Regex(label)) }

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.contains(part) }

    override fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.classes.contains(`class`) }

    override fun findAllByClassAndCreatedBy(
        `class`: ThingId,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        it.createdBy == createdBy && it.classes.contains(`class`)
    }

    override fun findAllByClassAndLabel(`class`: ThingId, label: String, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) {
            it.label == label && it.classes.contains(`class`)
        }

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        it.createdBy == createdBy && it.label == label && it.classes.contains(`class`)
    }

    override fun findAllByClassAndLabelMatchesRegex(
        `class`: ThingId,
        label: String,
        pageable: Pageable
    ): Page<Resource> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.contains(`class`)
        }
    }

    override fun findAllByClassAndLabelMatchesRegexAndCreatedBy(
        `class`: ThingId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) {
            it.label.matches(regex) && it.classes.contains(`class`) && it.createdBy == createdBy
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
    override fun findByLabel(label: String) =
        Optional.ofNullable(entities.values.firstOrNull { it.label == label })

    // TODO: rename to findAllByClassAndObservatoryId
    override fun findByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId): Iterable<Resource> =
        entities.values.filter { `class` in it.classes && it.observatoryId == id }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findPaperByResourceId(id: ThingId) =
        Optional.ofNullable(entities.values.firstOrNull {
            it.id == id && paperClass in it.classes
        })

    override fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { (it.verified ?: false) == verified && paperClass in it.classes }


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

    override fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == visibility }

    override fun findAllListed(pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED }

    override fun findAllPapersByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == visibility && paperClass in it.classes }

    override fun findAllListedPapers(pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) && paperClass in it.classes }

    override fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        it.visibility == visibility && it.classes.any { `class` -> `class` in classes }
    }

    override fun findAllListedByClassIn(
        classes: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) && it.classes.any { `class` -> `class` in classes }
    }

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: Visibility,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        it.visibility == visibility && it.observatoryId == id && it.classes.any { `class` -> `class` in classes }
    }

    override fun findAllListedByClassInAndObservatoryId(
        classes: Set<ThingId>,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) && it.observatoryId == id
            && it.classes.any { `class` -> `class` in classes }
    }
}
