package org.orkg.graph.adapter.output.inmemory

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.OwnershipInfo
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class InMemoryStatementRepository(inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<StatementId, GeneralStatement>(compareBy(GeneralStatement::createdAt)), StatementRepository {

    override val entities: InMemoryEntityAdapter<StatementId, GeneralStatement> =
        object : InMemoryEntityAdapter<StatementId, GeneralStatement> {
            override val keys: Collection<StatementId> get() = inMemoryGraph.findAllStatements().map { it.id }
            override val values: MutableCollection<GeneralStatement>
                get() = inMemoryGraph.findAllStatements().toMutableSet()

            override fun remove(key: StatementId): GeneralStatement? = get(key)?.also { inMemoryGraph.remove(it.id) }
            override fun clear() = inMemoryGraph.findAllStatements().forEach(inMemoryGraph::remove)

            override fun get(key: StatementId): GeneralStatement? = inMemoryGraph.findStatementById(key).getOrNull()
            override fun set(key: StatementId, value: GeneralStatement): GeneralStatement? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun save(statement: GeneralStatement) {
        entities[statement.id] = statement
    }

    override fun saveAll(statements: Set<GeneralStatement>) {
        statements.forEach(::save)
    }

    override fun count(): Long = entities.size.toLong()

    override fun delete(statement: GeneralStatement) {
        entities.remove(statement.id)
    }

    override fun deleteByStatementId(id: StatementId) {
        entities.remove(id)
    }

    override fun deleteByStatementIds(ids: Set<StatementId>) =
       ids.forEach { deleteByStatementId(it) }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        Optional.ofNullable(entities[id])

    override fun findAll(pageable: Pageable): Page<GeneralStatement> =
        findAll(
            pageable = pageable,
            subjectClasses = emptySet(),
            subjectId = null,
            subjectLabel = null,
            predicateId = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null,
            objectClasses = emptySet(),
            objectId = null,
            objectLabel = null
        )

    override fun findAll(
        pageable: Pageable,
        subjectClasses: Set<ThingId>,
        subjectId: ThingId?,
        subjectLabel: String?,
        predicateId: ThingId?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        objectClasses: Set<ThingId>,
        objectId: ThingId?,
        objectLabel: String?
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            pageable = pageable,
            comparator = pageable.withDefaultSort { Sort.by("created_at") }.sort.comparator
        ) {
            it.subject isInstanceOfAll subjectClasses &&
                (subjectId == null || subjectId == it.subject.id) &&
                (subjectLabel == null || subjectLabel == it.subject.label) &&
                (predicateId == null || predicateId == it.predicate.id) &&
                (createdBy == null || it.createdBy == createdBy) &&
                (createdAtStart == null || it.createdAt!! >= createdAtStart) &&
                (createdAtEnd == null || it.createdAt!! <= createdAtEnd) &&
                it.`object` isInstanceOfAll objectClasses &&
                (objectId == null || objectId == it.`object`.id) &&
                (objectLabel == null || objectLabel == it.`object`.label)
        }

    override fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { it.id in ids }

    override fun countByIdRecursive(id: ThingId): Long =
        findSubgraph(ThingId(id.value)) { statement, _ ->
            statement.`object` !is Resource || (statement.`object` as Resource).classes.none { `class` ->
                `class` == Classes.paper || `class` == Classes.problem || `class` == Classes.researchField
            }
        }.count().toLong()

    override fun findAllBySubjects(
        subjectIds: List<ThingId>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.subject.id in subjectIds
    }

    override fun findAllByObjects(
        objectIds: List<ThingId>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.`object`.id in objectIds
    }

    override fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration, sort: Sort): Iterable<GeneralStatement> =
        entities.values.find { it.subject.id == id }?.let {
            val exclude = mutableSetOf<GeneralStatement>()
            findSubgraph(it.subject.id) { statement, level ->
                if (configuration.minLevel != null && level <= configuration.minLevel!!) {
                    // Filter statements out later because we do not want to stop the graph traversal
                    exclude.add(statement)
                }
                if (configuration.maxLevel != null && level > configuration.maxLevel!!) {
                    return@findSubgraph false
                } else if (statement.`object` is Resource) {
                    with(statement.`object` as Resource) {
                        if (configuration.blacklist.isNotEmpty() &&
                            (classes.containsAny(configuration.blacklist) ||
                                configuration.blacklist.contains(Classes.resource))
                        ) {
                            return@findSubgraph false
                        } else if (configuration.whitelist.isNotEmpty() &&
                            !classes.containsAny(configuration.whitelist) &&
                            configuration.whitelist.contains(Classes.resource)
                        ) {
                            return@findSubgraph false
                        }
                    }
                } else if (configuration.whitelist.isNotEmpty()) {
                    return@findSubgraph when (statement.`object`) {
                        is Literal -> configuration.whitelist.contains(Classes.literal)
                        is Predicate -> configuration.whitelist.contains(Classes.predicate)
                        is Class -> configuration.whitelist.contains(Classes.`class`)
                        else -> false
                    }
                }
                return@findSubgraph true
            }.filter { statement -> statement !in exclude }
        }?.sortedWith(sort.comparator) ?: emptySet()

    private val Sort.comparator: Comparator<GeneralStatement>
        get() = if (isUnsorted) {
            Comparator.comparing<GeneralStatement?, OffsetDateTime> { it.createdAt }.reversed()
        } else {
            Comparator { a, b ->
                var result = 0
                for (order in this) {
                    result = when (order.property) {
                        "id" -> order.compare(a.id, b.id)
                        "created_at" -> order.compare(a.createdAt, b.createdAt)
                        "created_by" -> order.compare(a.createdBy.value, b.createdBy.value)
                        "index" -> order.compare(a.index, b.index)
                        "sub.id" -> order.compare(a.subject.id, b.subject.id)
                        "sub.label" -> order.compare(a.subject.label, b.subject.label)
                        "sub.created_at" -> order.compare(a.subject.createdAt, b.subject.createdAt)
                        "sub.created_by" -> order.compare(a.subject.createdBy.value, b.subject.createdBy.value)
                        "obj.id" -> order.compare(a.`object`.id, b.`object`.id)
                        "obj.label" -> order.compare(a.`object`.label, b.`object`.label)
                        "obj.created_at" -> order.compare(a.`object`.createdAt, b.`object`.createdAt)
                        "obj.created_by" -> order.compare(a.`object`.createdBy.value, b.`object`.createdBy.value)
                        else -> 0 // TODO: Throw exception?
                    }
                    if (result != 0) {
                        break
                    }
                }
                result
            }
        }

    private fun <T : Comparable<T>> Sort.Order.compare(a: T?, b: T?): Int {
        val result = when {
            a == null && b == null -> 0
            a == null -> 1
            b == null -> -1
            else -> a.compareTo(b)
        }
        return if (isAscending) result else -result
    }

    override fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount> {
        val predicateIdToUsageCount = mutableMapOf<ThingId, Long>()
        entities.values.forEach {
            predicateIdToUsageCount.compute(it.predicate.id) { _, value ->
                if (value == null) 1
                else value + 1
            }
        }
        return predicateIdToUsageCount.entries.map { PredicateUsageCount(it.key, it.value) }
            .sortedWith(compareByDescending<PredicateUsageCount> { it.count }.thenBy { it.id })
            .paged(pageable)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun countIncomingStatements(id: ThingId) =
        entities.count { id == it.`object`.id }.toLong()

    override fun countIncomingStatements(ids: Set<ThingId>) =
        entities.groupBy { it.`object`.id }
            .filter { it.value.isNotEmpty() && it.key in ids }
            .mapValues { it.value.size.toLong() }

    override fun findAllDescriptions(ids: Set<ThingId>): Map<ThingId, String> =
        entities.groupBy { it.subject.id }
            .filter { it.value.any { statement -> statement.`object` is Literal } && it.key in ids }
            .mapValues { it.value.first().`object`.label }

    override fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo> =
        entities.filter { it.id in statementIds }.map { OwnershipInfo(it.id, it.createdBy) }.toSet()

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && Classes.paper in (it.subject as Resource).classes &&
                it.predicate.id == Predicates.hasContribution &&
                it.`object`.id.value == id.value
        }?.let {
            it.subject as Resource
        }?.let { paper ->
            entities.values.find {
                it.subject.id == paper.id && it.predicate.id == Predicates.hasDOI
            }?.let {
                it.`object` as Literal
            }
        })

    override fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource> =
        entities.values.filter {
            it.subject is Resource && (it.subject as Resource).classes.containsAny(classes) &&
                it.predicate.id == Predicates.hasDOI &&
                it.`object` is Literal && it.`object`.label.uppercase() == doi.uppercase()
        }
            .map { it.subject as Resource }
            .maxByOrNull { it.createdAt }
            .let { Optional.ofNullable(it) }

    override fun findAllBySubjectClassAndDOI(subjectClass: ThingId, doi: String, pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && with(it.subject as Resource) {
                    subjectClass in classes
                } && it.predicate.id == Predicates.hasDOI && it.`object` is Literal && it.`object`.label.uppercase() == doi.uppercase()
            }
            .map { it.subject as Resource }
            .distinct()
            .paged(pageable)

    // TODO: rename to findAllProblemsByObservatoryId
    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        // FIXME: Create a union with all Problems that are not used in statements
        entities.values.filter {
            it.subject is Resource && Classes.paper in (it.subject as Resource).classes && (it.subject as Resource).observatoryId == id &&
                it.predicate.id == Predicates.hasContribution &&
                it.`object` is Resource && Classes.contribution in (it.`object` as Resource).classes
        }.map { hasContributionStatement ->
            entities.values.filter {
                it.subject.id == hasContributionStatement.`object`.id &&
                    it.predicate.id == Predicates.hasResearchProblem &&
                    it.`object` is Resource && Classes.problem in (it.`object` as Resource).classes
            }.map { it.`object` as Resource }
        }.flatten().distinct().paged(pageable)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        findSubgraph(id) { statement, _ ->
            statement.`object` !is Resource || (statement.`object` as Resource).classes.none { `class` ->
                `class` == Classes.paper || `class` == Classes.problem || `class` == Classes.researchField
            }
        }.map {
            setOf(
                it.subject.createdBy,
                it.`object`.createdBy,
                it.createdBy
            )
        }.flatten().distinct()
            .sortedBy { it.toString() }
            .paged(pageable)

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> {
        val resource = entities.values
            .first { it.subject.id == id && (it.subject is Resource) }
            .subject as Resource
        return findSubgraph(id) { statement, _ ->
            statement.`object` !is Resource || (statement.`object` as Resource).classes.none { `class` ->
                `class` == Classes.paper || `class` == Classes.problem || `class` == Classes.researchField
            }
        }.asSequence()
            .map {
                setOf(
                    ResourceEdit(it.subject.createdBy, it.subject.createdAt.toInstant().toEpochMilli()),
                    ResourceEdit(it.`object`.createdBy, it.`object`.createdAt.toInstant().toEpochMilli()),
                    ResourceEdit(it.createdBy, it.createdAt!!.toInstant().toEpochMilli())
                )
            }.flatten()
            .distinct()
            .filter { it.millis >= resource.createdAt.toInstant().toEpochMilli() }
            .map {
                ResourceContributor(
                    it.contributor.value.toString(),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:'00'XXX")
                        .apply { timeZone = TimeZone.getTimeZone("UTC") }
                        .format(it.millis)
                )
            }.distinct()
            .sortedByDescending { it.createdAt }
            .toList()
            .paged(pageable)
    }

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        entities.values.filter {
            it.subject is Resource && Classes.comparison in (it.subject as Resource).classes && (it.subject as Resource).organizationId == id &&
                it.predicate.id == Predicates.comparesContribution &&
                it.`object` is Resource && Classes.contribution in (it.`object` as Resource).classes
        }.map { compareContributionStatement ->
            entities.values.filter {
                it.subject.id == compareContributionStatement.`object`.id &&
                    it.predicate.id == Predicates.hasResearchProblem &&
                    it.`object` is Resource && Classes.problem in (it.`object` as Resource).classes
            }.map { it.`object` as Resource }
        }.flatten().distinct().paged(pageable)

    override fun nextIdentity(): StatementId {
        var count = entities.size.toLong()
        var id = StatementId(count)
        while (id in entities) {
            id = StatementId(++count)
        }
        return id
    }

    override fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && it.predicate.id == Predicates.hasContribution && with(it.subject as Resource) {
                    (observatoryId == null || this.observatoryId == observatoryId) && Classes.paper in classes && when (visibility) {
                        VisibilityFilter.ALL_LISTED -> this.visibility == Visibility.DEFAULT || this.visibility == Visibility.FEATURED
                        VisibilityFilter.UNLISTED -> this.visibility == Visibility.UNLISTED
                        VisibilityFilter.FEATURED -> this.visibility == Visibility.FEATURED
                        VisibilityFilter.NON_FEATURED -> this.visibility == Visibility.FEATURED
                        VisibilityFilter.DELETED -> this.visibility == Visibility.DELETED
                    }
                } && allFiltersMatch(filters, it.`object` as Resource)
            }
            .map { it.subject as Resource }
            .paged(pageable)

    private fun allFiltersMatch(filters: List<SearchFilter>, contribution: Resource): Boolean =
        filters.all { filter ->
            filter.path.fold(listOf<Thing>(contribution)) { acc, predicate ->
                entities.values
                    .filter { it.subject in acc && it.predicate.id == predicate }
                    .map { it.`object` }
            }.any {
                when (it) {
                    is Resource -> filter.range == Classes.resources || filter.range in it.classes
                    is Literal -> when (it.datatype.toUri()) {
                        Literals.XSD.INT.uri -> Classes.integer == filter.range
                        Literals.XSD.STRING.uri -> Classes.string == filter.range
                        Literals.XSD.DECIMAL.uri, Literals.XSD.FLOAT.uri -> Classes.decimal == filter.range || Classes.float == filter.range
                        Literals.XSD.DATE.uri -> Classes.date == filter.range
                        Literals.XSD.BOOLEAN.uri -> Classes.boolean == filter.range
                        Literals.XSD.URI.uri -> Classes.uri == filter.range
                        else -> false
                    }
                    is Predicate -> Classes.predicates == filter.range
                    is Class -> Classes.classes == filter.range
                } && filter.values.any { (op, value) ->
                    when (it) {
                        is Literal -> when (it.datatype.toUri()) {
                            Literals.XSD.INT.uri -> op.matches(it.label.toInt(), value.toInt())
                            Literals.XSD.DECIMAL.uri, Literals.XSD.FLOAT.uri -> op.matches(it.label.toDouble(), value.toDouble())
                            Literals.XSD.DATE.uri -> op.matches(OffsetDateTime.parse(it.label), OffsetDateTime.parse(value))
                            Literals.XSD.BOOLEAN.uri -> op.matches(it.label.toBoolean(), value.toBoolean())
                            else -> op.matches(it.label, value)
                        }
                        else -> op.matches(it.id.value, value)
                    }
                }
            }
        }

    private fun String.toUri(): String = replace(Regex("^xsd:"), "http://www.w3.org/2001/XMLSchema#")

    private fun <T : Comparable<T>> Operator.matches(a: T, b: T): Boolean =
        when (this) {
            Operator.EQ -> a.compareTo(b) == 0
            Operator.NE -> a.compareTo(b) != 0
            Operator.LT -> a < b
            Operator.GT -> b > a
            Operator.LE -> a <= b
            Operator.GE -> a >= b
        }

    private fun findSubgraph(
        root: ThingId,
        expansionFilter: (GeneralStatement, Int) -> Boolean = { _, _ -> true }
    ): Set<GeneralStatement> {
        val visited = mutableSetOf<GeneralStatement>()
        val frontier = entities.values.filter {
            it.subject.id == root && expansionFilter(it, 1)
        }.mapTo(Stack()) {
            it to 1
        }
        while (frontier.isNotEmpty()) {
            val (statement, level) = frontier.pop()
            visited.add(statement)
            frontier.addAll(
                entities.values.filter {
                    it.subject == statement.`object` && it !in visited && expansionFilter(it, level + 1)
                }.map {
                    it to level + 1
                }
            )
        }
        return visited
    }

    private fun <T : Any> Iterable<T>.containsAny(other: Iterable<T>): Boolean = any(other::contains)

    private infix fun Thing.isInstanceOfAll(classes: Set<ThingId>) =
        classes.isEmpty() || classes.size == 1 && when (this) {
            is Resource -> classes.single() == Classes.resource
            is Predicate -> classes.single() == Classes.predicate
            is Literal -> classes.single() == Classes.literal
            is Class -> classes.single() == Classes.`class`
        } || this is Resource && (classes - Classes.resource).all { it in classes }

    private data class ResourceEdit(
        val contributor: ContributorId,
        val millis: Long
    )
}
