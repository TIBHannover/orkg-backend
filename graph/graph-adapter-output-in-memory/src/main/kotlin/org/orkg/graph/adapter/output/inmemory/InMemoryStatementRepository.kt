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
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.OwnershipInfo
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

private val paperClass = ThingId("Paper")
private val problemClass = ThingId("Problem")
private val comparisonClass = ThingId("Comparison")
private val contributionClass = ThingId("Contribution")
private val researchProblemClass = ThingId("ResearchProblem")
private val researchFieldClass = ThingId("ResearchField")
private val hasContribution = ThingId("P31")
private val hasResearchProblem = ThingId("P32")
private val hasDOI = ThingId("P26")
private val compareContribution = ThingId("compareContribution")

class InMemoryStatementRepository(inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<StatementId, GeneralStatement>(compareBy(GeneralStatement::createdAt)), StatementRepository {

    override val entities: InMemoryEntityAdapter<StatementId, GeneralStatement> =
        object : InMemoryEntityAdapter<StatementId, GeneralStatement> {
            override val keys: Collection<StatementId> get() = inMemoryGraph.findAllStatements().map { it.id!! }
            override val values: MutableCollection<GeneralStatement>
                get() = inMemoryGraph.findAllStatements().toMutableSet()

            override fun remove(key: StatementId): GeneralStatement? = get(key)?.also { inMemoryGraph.remove(it.id!!) }
            override fun clear() = inMemoryGraph.findAllStatements().forEach(inMemoryGraph::remove)

            override fun get(key: StatementId): GeneralStatement? = inMemoryGraph.findStatementById(key).getOrNull()
            override fun set(key: StatementId, value: GeneralStatement): GeneralStatement? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun save(statement: GeneralStatement) {
        entities[statement.id!!] = statement
    }

    override fun saveAll(statements: Set<GeneralStatement>) {
        statements.forEach(::save)
    }

    override fun count(): Long = entities.size.toLong()

    override fun delete(statement: GeneralStatement) {
        entities.remove(statement.id!!)
    }

    override fun deleteByStatementId(id: StatementId) {
        entities.remove(id)
    }

    override fun deleteByStatementIds(ids: Set<StatementId>) =
       ids.forEach { deleteByStatementId(it) }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        Optional.ofNullable(entities[id])

    override fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { it.id in ids }

    override fun findAllBySubject(subjectId: ThingId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.subject.id == subjectId }

    override fun findAllByPredicateId(predicateId: ThingId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.predicate.id == predicateId }

    override fun findAllByObject(objectId: ThingId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.`object`.id == objectId }

    override fun countByIdRecursive(id: ThingId): Long =
        findSubgraph(ThingId(id.value)) { statement, _ ->
            statement.`object` !is Resource || (statement.`object` as Resource).classes.none { `class` ->
                `class` == paperClass || `class` == researchProblemClass || `class` == researchFieldClass
            }
        }.count().toLong()

    override fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object`.id == objectId
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.subject.id == subjectId
    }

    // FIXME: rename to findAllByPredicateIdAndLiteralObjectLabel
    override fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object` is Literal && it.`object`.label == literal
    }

    // FIXME: rename to findAllByPredicateIdAndLiteralObjectLabelAndSubjectClass
    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object` is Literal && it.`object`.label == literal &&
            it.subject is Resource && subjectClass in (it.subject as Resource).classes
    }

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
                                configuration.blacklist.contains(ThingId("Resource")))
                        ) {
                            return@findSubgraph false
                        } else if (configuration.whitelist.isNotEmpty() &&
                            !classes.containsAny(configuration.whitelist) &&
                            configuration.whitelist.contains(ThingId("Resource"))
                        ) {
                            return@findSubgraph false
                        }
                    }
                } else if (configuration.whitelist.isNotEmpty()) {
                    return@findSubgraph when (statement.`object`) {
                        is Literal -> configuration.whitelist.contains(ThingId("Literal"))
                        is Predicate -> configuration.whitelist.contains(ThingId("Predicate"))
                        is Class -> configuration.whitelist.contains(ThingId("Class"))
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
                        "created_at" -> order.compare(a.createdAt, b.createdAt)
                        "created_by" -> order.compare(a.createdBy.value, b.createdBy.value)
                        "index" -> order.compare(a.index, b.index)
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

    override fun countStatementsAboutResource(id: ThingId) =
        entities.count { id == it.`object`.id }.toLong()

    override fun countStatementsAboutResources(resourceIds: Set<ThingId>) =
        resourceIds.associateWith(::countStatementsAboutResource).filter { it.value > 0 }

    override fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo> =
        entities.filter { it.id in statementIds }.map { OwnershipInfo(it.id!!, it.createdBy) }.toSet()

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && paperClass in (it.subject as Resource).classes &&
                it.predicate.id == hasContribution &&
                it.`object`.id.value == id.value
        }?.let {
            it.subject as Resource
        }?.let { paper ->
            entities.values.find {
                it.subject.id == paper.id && it.predicate.id == hasDOI
            }?.let {
                it.`object` as Literal
            }
        })

    override fun findAllDOIsRelatedToComparison(id: ThingId): Iterable<String> =
        entities.values.filter {
            it.subject.id == id && it.subject is Resource && Classes.comparison in (it.subject as Resource).classes &&
                it.predicate.id == Predicates.comparesContribution &&
                it.`object` is Resource && Classes.contribution in (it.`object` as Resource).classes
        }.map { comparisonHasContribution ->
            val paperIds = entities.values.filter {
                it.subject is Resource && Classes.paper in (it.subject as Resource).classes &&
                    it.predicate.id == Predicates.hasContribution &&
                    it.`object`.id == comparisonHasContribution.`object`.id
            }.map { it.subject.id }
            entities.values
                .filter { it.subject.id in paperIds && it.predicate.id == Predicates.hasDOI && it.`object` is Literal }
                .map { (it.`object` as Literal).label.trim() }
                .filter { it.isNotBlank() }
        }.flatten().distinct()

    override fun countPredicateUsage(id: ThingId): Long =
        entities.values.count {
            (it.subject is Predicate && (it.subject as Predicate).id == id ||
                it.predicate.id == id ||
                it.`object` is Predicate && (it.`object` as Predicate).id == id) &&
                it.predicate.id.value != "description"
        }.toLong()

    override fun findByDOI(doi: String): Optional<Resource> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && it.predicate.id == hasDOI &&
                it.`object` is Literal && it.`object`.label.uppercase() == doi.uppercase()
        }).map { it.subject as Resource }

    override fun findAllBySubjectClassAndDOI(subjectClass: ThingId, doi: String, pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && with(it.subject as Resource) {
                    subjectClass in classes
                } && it.predicate.id == hasDOI && it.`object` is Literal && it.`object`.label.uppercase() == doi.uppercase()
            }
            .map { it.subject as Resource }
            .distinct()
            .paged(pageable)

    // TODO: rename to findAllProblemsByObservatoryId
    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        // FIXME: Create a union with all Problems that are not used in statements
        entities.values.filter {
            it.subject is Resource && paperClass in (it.subject as Resource).classes && (it.subject as Resource).observatoryId == id &&
                it.predicate.id == hasContribution &&
                it.`object` is Resource && contributionClass in (it.`object` as Resource).classes
        }.map { hasContributionStatement ->
            entities.values.filter {
                it.subject.id == hasContributionStatement.`object`.id &&
                    it.predicate.id == hasResearchProblem &&
                    it.`object` is Resource && problemClass in (it.`object` as Resource).classes
            }.map { it.`object` as Resource }
        }.flatten().distinct().paged(pageable)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        findSubgraph(id) { statement, _ ->
            statement.`object` !is Resource || (statement.`object` as Resource).classes.none { `class` ->
                `class` == paperClass || `class` == researchProblemClass || `class` == researchFieldClass
            }
        }.map {
            setOf(
                it.subject.toContributor(),
                it.`object`.toContributor(),
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
                `class` == paperClass || `class` == researchProblemClass || `class` == researchFieldClass
            }
        }.asSequence()
            .map {
                setOf(
                    it.subject.toResourceEdit(),
                    it.`object`.toResourceEdit(),
                    ResourceEdit(it.createdBy, it.createdAt!!.toInstant().toEpochMilli())
                )
            }.flatten()
            .distinct()
            .filter { it.millis >= resource.createdAt.toInstant().toEpochMilli() }
            .map {
                ResourceContributor(
                    it.contributor.value.toString(),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(it.millis - (it.millis % 60000))
                )
            }.distinct()
            .sortedByDescending { it.createdAt }
            .toList()
            .paged(pageable)
    }

    override fun checkIfResourceHasStatements(id: ThingId): Boolean =
        entities.values.any { it.subject.id.value == id.value || it.`object`.id.value == id.value }

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        entities.values.filter {
            it.subject is Resource && comparisonClass in (it.subject as Resource).classes && (it.subject as Resource).organizationId == id &&
                it.predicate.id == compareContribution &&
                it.`object` is Resource && contributionClass in (it.`object` as Resource).classes
        }.map { compareContributionStatement ->
            entities.values.filter {
                it.subject.id == compareContributionStatement.`object`.id &&
                    it.predicate.id == hasResearchProblem &&
                    it.`object` is Resource && problemClass in (it.`object` as Resource).classes
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

    override fun findBySubjectIdAndPredicateIdAndObjectId(
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId
    ): Optional<GeneralStatement> = Optional.ofNullable(entities.values.firstOrNull {
        it.subject.id == subjectId && it.predicate.id == predicateId && it.`object`.id == objectId
    })

    override fun findAllCurrentComparisons(pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && Classes.comparison in (it.subject as Resource).classes &&
                    findAllByObjectAndPredicate(it.subject.id, Predicates.hasPreviousVersion, PageRequest.of(0, 1)).isEmpty
            }
            .map { it.subject as Resource }
            .distinct()
            .sortedBy { it.createdAt }
            .paged(pageable)

    override fun findAllCurrentListedComparisons(pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && with(it.subject as Resource) {
                    Classes.comparison in classes && (visibility == Visibility.DEFAULT || visibility == Visibility.FEATURED)
                } && findAllByObjectAndPredicate(it.subject.id, Predicates.hasPreviousVersion, PageRequest.of(0, 1)).isEmpty
            }
            .map { it.subject as Resource }
            .distinct()
            .sortedBy { it.createdAt }
            .paged(pageable)

    override fun findAllCurrentComparisonsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && with(it.subject as Resource) {
                    Classes.comparison in classes && this.visibility == visibility
                } && findAllByObjectAndPredicate(it.subject.id, Predicates.hasPreviousVersion, PageRequest.of(0, 1)).isEmpty
            }
            .map { it.subject as Resource }
            .distinct()
            .sortedBy { it.createdAt }
            .paged(pageable)

    override fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Resource> =
        entities.values
            .filter {
                it.subject is Resource && with(it.subject as Resource) {
                    Classes.comparison in classes && (visibility == Visibility.DEFAULT || visibility == Visibility.FEATURED)
                } && findAllByObjectAndPredicate(it.subject.id, Predicates.hasPreviousVersion, PageRequest.of(0, 1)).isEmpty &&
                    findAllBySubjectAndPredicate(it.subject.id, Predicates.hasDOI, PageRequest.of(0, 1)).isEmpty
            }
            .map { it.subject as Resource }
            .distinct()
            .sortedBy { it.createdAt }
            .paged(pageable)

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

    private fun Thing.toContributor() =
        when (this) {
            is Class -> createdBy
            is Resource -> createdBy
            is Predicate -> createdBy
            is Literal -> createdBy
        }

    private fun Thing.toResourceEdit() =
        when (this) {
            is Class -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Resource -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Predicate -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Literal -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
        }

    private fun <T : Any> Iterable<T>.containsAny(other: Iterable<T>): Boolean = any(other::contains)

    private data class ResourceEdit(
        val contributor: ContributorId,
        val millis: Long
    )
}
