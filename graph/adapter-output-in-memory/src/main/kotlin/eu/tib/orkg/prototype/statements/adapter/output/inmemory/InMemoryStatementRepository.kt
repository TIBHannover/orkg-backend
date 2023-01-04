package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Pageable

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
        findAllFilteredAndPaged(pageable) { it.subject.thingId.toString() == subjectId }

    override fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.predicate.id == predicateId }

    override fun findAllByObject(objectId: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.`object`.thingId.toString() == objectId }

    override fun countByIdRecursive(paperId: String): Int {
        val visited = mutableSetOf<StatementId>()
        val frontier = ArrayDeque(entities.values.filter {
            it.subject.thingId.toString() == paperId
        })
        var count = 0
        while (frontier.isNotEmpty()) {
            val statement = frontier.pop()
            if (statement.id !in visited) {
                entities.values
                    .filter {
                        statement.`object`.thingId.toString() == it.subject.thingId.toString() && it.id !in visited
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
        it.predicate.id == predicateId && it.`object`.thingId.toString() == objectId
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.subject.thingId.toString() == subjectId
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
        subjectClass: ClassId,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        it.predicate.id == predicateId && it.`object` is Literal && it.`object`.label == literal && it.subject.thingId == subjectClass
    }

    override fun findAllBySubjects(
        subjectIds: List<String>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        subjectIds.contains(it.subject.thingId.toString())
    }

    override fun findAllByObjects(
        objectIds: List<String>,
        pageable: Pageable
    ) = findAllFilteredAndPaged(pageable) {
        objectIds.contains(it.`object`.thingId.toString())
    }

    override fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findAll(depth: Int): Iterable<GeneralStatement> {
        TODO("Can be removed")
    }

    override fun countStatementsAboutResource(id: ResourceId) =
        entities.filter { id == it.value.`object`.thingId }.count().toLong()

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>) =
        resourceIds.associateWith(::countStatementsAboutResource).filter { it.value > 0 }

    override fun nextIdentity(): StatementId {
        var id = StatementId(entities.size.toLong())
        while(entities.contains(id)) {
            id = StatementId(id.value.toLong() + 1)
        }
        return id
    }
}
