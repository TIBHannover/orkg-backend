package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
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
private val hasContribution = PredicateId("P31")
private val hasResearchProblem = PredicateId("P32")
private val hasDOI = PredicateId("P26")
private val compareContribution = PredicateId("compareContribution")

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

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        Optional.ofNullable(entities[id])

    override fun findAllBySubject(subjectId: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.subject.thingId.value == subjectId }

    override fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.predicate.id == predicateId }

    override fun findAllByObject(objectId: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.`object`.thingId.value == objectId }

    override fun countByIdRecursive(paperId: String): Int {
        val visited = mutableSetOf<StatementId>()
        val frontier = ArrayDeque(entities.values.filter {
            it.subject.thingId.value == paperId
        })
        var count = 0
        while (frontier.isNotEmpty()) {
            val statement = frontier.pop()
            if (statement.id !in visited) {
                entities.values
                    .filter {
                        statement.`object`.thingId.value == it.subject.thingId.value && it.id !in visited
                    }
                    .forEach(frontier::add)
                count++
            }
        }
        return count
    }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object`.thingId.value == objectId
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.subject.thingId.value == subjectId
    }

    // FIXME: rename to findAllByPredicateIdAndLiteralObjectLabel
    override fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object` is Literal && it.`object`.label == literal
    }

    // FIXME: rename to findAllByPredicateIdAndLiteralObjectLabelAndSubjectClass
    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object` is Literal && it.`object`.label == literal && it.subject.thingId == subjectClass
    }

    override fun findAllBySubjects(
        subjectIds: List<String>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        subjectIds.contains(it.subject.thingId.value)
    }

    override fun findAllByObjects(
        objectIds: List<String>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        objectIds.contains(it.`object`.thingId.value)
    }

    override fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement> {
        TODO("This depends on apoc-specific configurations")
    }

    override fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount> {
        val predicateIdToUsageCount = mutableMapOf<PredicateId, Long>()
        entities.values.forEach {
            predicateIdToUsageCount.compute(it.predicate.id!!) { _, value ->
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

    override fun countStatementsAboutResource(id: ResourceId) =
        entities.filter { id.value == it.value.`object`.thingId.value }.count().toLong()

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>) =
        resourceIds.associateWith(::countStatementsAboutResource).filter { it.value > 0 }

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> =
        Optional.ofNullable(entities.values.find {
            it.subject is Resource && paperClass in (it.subject as Resource).classes
                && it.predicate.id == hasContribution
                && it.`object`.thingId.value == id.value
        }?.let {
            it.subject as Resource
        }?.let { paper ->
            entities.values.find {
                it.subject.thingId == paper.thingId
                    && it.predicate.id == hasDOI
            }?.let {
                it.`object` as Literal
            }
        })

    override fun countPredicateUsage(id: PredicateId): Long =
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
                && it.`object` is Literal && it.`object`.label == doi
        }).map { it.subject as Resource }

    // TODO: rename to findAllProblemsByObservatoryId
    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        // FIXME: Create a union with all Problems that are not used in statements
        entities.values.filter {
            it.subject is Resource && with(it.subject as Resource) {
                paperClass in classes && observatoryId == id
            }
        }.map { statementAboutPaper ->
            findSubgraph(statementAboutPaper.subject.thingId).filter {
                it.`object` is Resource && with(it.`object` as Resource) {
                    problemClass in classes && observatoryId == id
                }
            }.map { it.`object` as Resource }
        }.flatten().distinct()

    // TODO: rename to findAllContributorsByResourceId
    override fun findContributorsByResourceId(id: ResourceId, pageable: Pageable): Page<ResourceContributor> =
        findSubgraph(ThingId(id.value)) {
            it.`object` !is Resource || (it.`object` as Resource).classes.none { `class` ->
                `class` == paperClass || `class` == researchProblemClass || `class` == researchFieldClass
            }
        }.map {
            setOf(
                it.subject.toResourceContributor(),
                it.`object`.toResourceContributor(),
                ResourceContributor(it.createdBy.toString(), it.createdAt!!.format(ISO_OFFSET_DATE_TIME))
            )
        }.flatten().distinct()
            .sortedByDescending { it.createdAt }
            .paged(pageable)

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean =
        entities.values.any { it.subject.thingId.value == id.value || it.`object`.thingId.value == id.value }

    override fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        entities.values.filter {
            it.subject is Resource && comparisonClass in (it.subject as Resource).classes
                && it.predicate.id == compareContribution
                && it.`object` is Resource && contributionClass in (it.`object` as Resource).classes
        }.map { compareContributionStatement ->
            entities.values.filter {
                it.subject.thingId == compareContributionStatement.`object`.thingId
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

    private fun findSubgraph(
        root: ThingId,
        expansionFilter: (GeneralStatement) -> Boolean = { true }
    ): Set<GeneralStatement> {
        val visited = mutableSetOf<GeneralStatement>()
        val frontier = entities.values.filterTo(Stack()) {
            it.subject.thingId == root && expansionFilter(it)
        }
        while (frontier.isNotEmpty()) {
            val statement = frontier.pop()
            visited.add(statement)
            frontier.addAll(entities.values.filter {
                it.subject == statement.`object` && it !in visited && expansionFilter(it)
            })
        }
        return visited
    }

    private fun Thing.toResourceContributor() =
        when (this) {
            is Class -> ResourceContributor(createdBy.value.toString(), createdAt.format(ISO_OFFSET_DATE_TIME))
            is Resource -> ResourceContributor(createdBy.value.toString(), createdAt.format(ISO_OFFSET_DATE_TIME))
            is Predicate -> ResourceContributor(createdBy.value.toString(), createdAt.format(ISO_OFFSET_DATE_TIME))
            is Literal -> ResourceContributor(createdBy.value.toString(), createdAt.format(ISO_OFFSET_DATE_TIME))
        }
}
