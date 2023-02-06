package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

private val paperClass = ThingId("Paper")
private val hasContribution = PredicateId("P31")
private val hasDOI = PredicateId("P26")

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

    override fun nextIdentity(): StatementId {
        var id = StatementId(entities.size.toLong())
        while(id in entities) {
            id = StatementId(id.value.toLong() + 1)
        }
        return id
    }
}
