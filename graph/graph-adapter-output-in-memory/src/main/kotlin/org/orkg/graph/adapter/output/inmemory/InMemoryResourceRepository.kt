package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

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

    override fun deleteById(id: ThingId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findById(id: ThingId) =
        Optional.ofNullable(entities[id])

    override fun findAll(pageable: Pageable): Page<Resource> =
        findAll(
            pageable = pageable,
            label = null,
            visibility = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null,
            includeClasses = emptySet(),
            excludeClasses = emptySet(),
            observatoryId = null,
            organizationId = null
        )

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<Resource> =
        findAllFilteredAndPaged(
            pageable = pageable,
            comparator = if (label != null) {
                compareBy { it.label.length }
            } else {
                pageable.withDefaultSort { Sort.by("created_at") }.sort.resourceComparator
            },
            predicate = {
                (label == null || it.label.matches(label))
                    && (visibility == null || when (visibility) {
                        VisibilityFilter.ALL_LISTED -> it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED
                        VisibilityFilter.UNLISTED -> it.visibility == Visibility.UNLISTED
                        VisibilityFilter.FEATURED -> it.visibility == Visibility.FEATURED
                        VisibilityFilter.NON_FEATURED -> it.visibility == Visibility.DEFAULT
                        VisibilityFilter.DELETED -> it.visibility == Visibility.DELETED
                    })
                    && (createdBy == null || it.createdBy == createdBy)
                    && (createdAtStart == null || it.createdAt >= createdAtStart)
                    && (createdAtEnd == null || it.createdAt <= createdAtEnd)
                    && (includeClasses.isEmpty() || includeClasses.all { `class` -> `class` in it.classes })
                    && (excludeClasses.isEmpty() || excludeClasses.none { `class` -> `class` in it.classes })
                    && (observatoryId == null || it.observatoryId == observatoryId)
                    && (organizationId == null || it.organizationId == organizationId)
            }
        )

    override fun findAllPapersByLabel(label: String) =
        entities.values.filter { it.label.equals(label, ignoreCase = true) && paperClass in it.classes && paperDeletedClass !in it.classes}

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        entities.values
            .filter { it.label.matches(labelSearchString) }
            .sortedWith(compareBy { it.label.length })
            .paged(pageable)

    override fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { `class` in it.classes }

    override fun findAllByClassAndCreatedBy(
        `class`: ThingId,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> = findAllFilteredAndPaged(pageable) {
        it.createdBy == createdBy && `class` in it.classes
    }

    override fun findAllByClassAndLabel(
        `class`: ThingId,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource> = entities.values
        .filter { it.label.matches(labelSearchString) && `class` in it.classes }
        .sortedWith(compareBy { it.label.length })
        .paged(pageable)

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        labelSearchString: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> = entities.values
        .filter { it.createdBy == createdBy && it.label.matches(labelSearchString) && `class` in it.classes }
        .sortedWith(compareBy { it.label.length })
        .paged(pageable)

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.classes.containsAll(includeClasses) && it.classes.none { id ->
            id in excludeClasses
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource> = entities.values
        .filter {
            it.label.matches(labelSearchString) && it.classes.containsAll(includeClasses) && it.classes.none { id ->
                id in excludeClasses
            }
        }
        .sortedWith(compareBy { it.label.length })
        .paged(pageable)

    override fun findPaperByLabel(label: String) =
        Optional.ofNullable(entities.values.firstOrNull { it.label.equals(label, ignoreCase = true) && paperClass in it.classes })

    override fun findAllByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) {
            `class` in it.classes && it.observatoryId == id
        }

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findPaperById(id: ThingId) =
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
    override fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) {
            comparisonClass in it.classes && it.organizationId == id
        }

    override fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == visibility }

    override fun findAllListed(pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED }

    override fun findAllByClassAndVisibility(classId: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { it.visibility == visibility && classId in it.classes }

    override fun findAllListedByClass(classId: ThingId, pageable: Pageable): Page<Resource> =
        findAllFilteredAndPaged(pageable) { (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) && classId in it.classes }

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
