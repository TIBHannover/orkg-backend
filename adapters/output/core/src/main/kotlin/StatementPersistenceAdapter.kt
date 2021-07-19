package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.ports.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class StatementPersistenceAdapter(
    private val resourcePersistenceAdapter: ResourcePersistenceAdapter,
    private val predicatePersistenceAdapter: PredicatePersistenceAdapter,
    private val literalPersistenceAdapter: LiteralPersistenceAdapter,
    private val statementRepository: Neo4jStatementRepository
): StatementRepository {

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAll(pagination)
            .content
            .map { toStatement(it) }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)
            .map { toStatement(it) }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination).map { toStatement(it) }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)
            .map { toStatement(it) }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)
            .map { toStatement(it) }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllBySubjectAndPredicate(subjectId, predicateId, pagination)
            .map { toStatement(it) }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllByObjectAndPredicate(objectId, predicateId, pagination)
            .map { toStatement(it) }

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination)
            .map { toStatement(it) }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pagination)
            .map { toStatement(it) }

    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> resourcePersistenceAdapter.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalPersistenceAdapter.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    private fun toStatement(statement: Neo4jStatement) =
        GeneralStatement(
            id = statement.statementId!!,
            subject = refreshObject(statement.subject!!),
            predicate = predicatePersistenceAdapter.findById(statement.predicateId!!).get(),
            `object` = refreshObject(statement.`object`!!),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy
        )
}
