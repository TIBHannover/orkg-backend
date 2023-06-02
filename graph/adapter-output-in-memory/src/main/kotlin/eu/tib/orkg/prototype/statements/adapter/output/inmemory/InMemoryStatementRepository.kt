package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.OwnershipInfo
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.text.SimpleDateFormat
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

private val paperClass = ThingId("Paper")
private val paperDeletedClass = ThingId("PaperDeleted")
private val problemClass = ThingId("Problem")
private val comparisonClass = ThingId("Comparison")
private val contributionClass = ThingId("Contribution")
private val researchProblemClass = ThingId("ResearchProblem")
private val researchFieldClass = ThingId("ResearchField")
private val hasContribution = ThingId("P31")
private val hasResearchProblem = ThingId("P32")
private val hasDOI = ThingId("P26")
private val compareContribution = ThingId("compareContribution")

class InMemoryStatementRepository : InMemoryRepository<StatementId, GeneralStatement>(
    compareBy(GeneralStatement::createdAt)
), StatementRepository {
    override fun save(statement: GeneralStatement) {
        entities[statement.id!!] = statement
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

    override fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration): Iterable<GeneralStatement> =
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
                        if (configuration.blacklist.isNotEmpty()
                            && (classes.containsAny(configuration.blacklist)
                                || configuration.blacklist.contains(ThingId("Resource")))
                        ) {
                            return@findSubgraph false
                        } else if (configuration.whitelist.isNotEmpty()
                            && !classes.containsAny(configuration.whitelist)
                            && configuration.whitelist.contains(ThingId("Resource"))
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
        } ?: emptySet()

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
        entities.filter { id.value == it.value.`object`.id.value }.count().toLong()

    override fun countStatementsAboutResources(resourceIds: Set<ThingId>) =
        resourceIds.associateWith(::countStatementsAboutResource).filter { it.value > 0 }

    override fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo> =
        entities.filter { it.value.id in statementIds }.map { OwnershipInfo(it.value.id!!, it.value.createdBy) }.toSet()

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && paperClass in (it.subject as Resource).classes
                && it.predicate.id == hasContribution
                && it.`object`.id.value == id.value
        }?.let {
            it.subject as Resource
        }?.let { paper ->
            entities.values.find {
                it.subject.id == paper.id
                    && it.predicate.id == hasDOI
            }?.let {
                it.`object` as Literal
            }
        })

    override fun countPredicateUsage(id: ThingId): Long =
        entities.values.count {
            it.subject is Predicate && (it.subject as Predicate).id == id
                || it.predicate.id == id
                || it.`object` is Predicate && (it.`object` as Predicate).id == id
        }.toLong()

    override fun findByDOI(doi: String): Optional<Resource> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && with(it.subject as Resource) {
                paperClass in classes && paperDeletedClass !in classes
            }   && it.predicate.id == hasDOI
                && it.`object` is Literal && it.`object`.label.uppercase() == doi.uppercase()
        }).map { it.subject as Resource }

    // TODO: rename to findAllProblemsByObservatoryId
    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        // FIXME: Create a union with all Problems that are not used in statements
        entities.values.filter {
            it.subject is Resource && paperClass in (it.subject as Resource).classes && (it.subject as Resource).observatoryId == id
                && it.predicate.id == hasContribution
                && it.`object` is Resource && contributionClass in (it.`object` as Resource).classes
        }.map { hasContributionStatement ->
            entities.values.filter {
                it.subject.id == hasContributionStatement.`object`.id
                    && it.predicate.id == hasResearchProblem
                    && it.`object` is Resource && problemClass in (it.`object` as Resource).classes
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
            it.subject is Resource && comparisonClass in (it.subject as Resource).classes && (it.subject as Resource).organizationId == id
                && it.predicate.id == compareContribution
                && it.`object` is Resource && contributionClass in (it.`object` as Resource).classes
        }.map { compareContributionStatement ->
            entities.values.filter {
                it.subject.id == compareContributionStatement.`object`.id
                    && it.predicate.id == hasResearchProblem
                    && it.`object` is Resource && problemClass in (it.`object` as Resource).classes
            }.map { it.`object` as Resource }
        }.flatten().distinct().paged(pageable)

    override fun nextIdentity(): StatementId {
        var id = StatementId(entities.size.toLong())
        while(id in entities) {
            id = StatementId(id.value.toLong() + 1)
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
            is Predicate ->createdBy
            is Literal -> createdBy
        }

    private fun Thing.toResourceEdit() =
        when (this) {
            is Class -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Resource -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Predicate -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
            is Literal -> ResourceEdit(createdBy, createdAt.toInstant().toEpochMilli())
        }

    private fun <T: Any> Iterable<T>.containsAny(other: Iterable<T>): Boolean = any(other::contains)

    private data class ResourceEdit(
        val contributor: ContributorId,
        val millis: Long
    )
}
