package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class InMemoryResourceRepository(private val inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<ThingId, Resource>(compareBy(Resource::createdAt)), ResourceRepository {

    override val entities: InMemoryEntityAdapter<ThingId, Resource> =
        object : InMemoryEntityAdapter<ThingId, Resource> {
            override val keys: Collection<ThingId> get() = inMemoryGraph.findAllResources().map { it.id }
            override val values: MutableCollection<Resource> get() = inMemoryGraph.findAllResources().toMutableSet()

            override fun remove(key: ThingId): Resource? = get(key)?.also { inMemoryGraph.delete(it.id) }
            override fun clear() = inMemoryGraph.findAllResources().forEach(inMemoryGraph::delete)

            override fun get(key: ThingId): Resource? = inMemoryGraph.findResourceById(key).getOrNull()
            override fun set(key: ThingId, value: Resource): Resource? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("R$count")
        while (id in entities) {
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
        baseClass: ThingId?,
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
                (label == null || it.label.matches(label)) &&
                    (visibility == null || it.visibility in visibility.targets) &&
                    (createdBy == null || it.createdBy == createdBy) &&
                    (createdAtStart == null || it.createdAt >= createdAtStart) &&
                    (createdAtEnd == null || it.createdAt <= createdAtEnd) &&
                    (includeClasses.isEmpty() || includeClasses.all { `class` -> `class` in it.classes }) &&
                    (excludeClasses.isEmpty() || excludeClasses.none { `class` -> `class` in it.classes }) &&
                    (observatoryId == null || it.observatoryId == observatoryId) &&
                    (organizationId == null || it.organizationId == organizationId) &&
                    (baseClass == null || it.isInstanceOf(baseClass))
            }
        )

    private fun Resource.isInstanceOf(baseClass: ThingId): Boolean {
        val visited: MutableSet<ThingId> = mutableSetOf()
        val frontier: LinkedList<ThingId> = LinkedList<ThingId>(classes)
        while (frontier.isNotEmpty()) {
            val `class` = frontier.pop()
            if (`class` == baseClass) {
                return true
            }
            visited.add(`class`)
            inMemoryGraph.findClassRelationByChildId(`class`)
                .map { it.parent.id }
                .filter { it !in visited }
                .ifPresent(frontier::add)
        }
        return false
    }

    override fun findAllPapersByLabel(label: String) =
        entities.values.filter {
            it.label.equals(label, ignoreCase = true) &&
                Classes.paper in it.classes && Classes.paperDeleted !in it.classes
        }

    override fun findPaperByLabel(label: String) =
        Optional.ofNullable(entities.values.firstOrNull {
            it.label.equals(label, ignoreCase = true) && Classes.paper in it.classes
        })

    // TODO: Create a method with class parameter (and possibly unlisted, featured and verified flags)
    override fun findPaperById(id: ThingId) =
        Optional.ofNullable(entities.values.firstOrNull {
            it.id == id && Classes.paper in it.classes
        })

    override fun findAllContributorIds(pageable: Pageable) =
        entities.values
            .map { it.createdBy }
            .distinct()
            .filter { it != ContributorId.UNKNOWN }
            .sortedBy { it.value.toString() }
            .paged(pageable)

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
        (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) && it.observatoryId == id &&
            it.classes.any { `class` -> `class` in classes }
    }
}
