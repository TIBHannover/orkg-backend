package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatement
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jRepository: Neo4jStatementRepository,
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jLiteralRepository: Neo4jLiteralRepository,
    private val neo4jClassRepository: Neo4jClassRepository,
) : StatementRepository {
    override fun nextIdentity(): StatementId = neo4jStatementIdGenerator.nextIdentity()

    override fun save(statement: GeneralStatement) {
        neo4jRepository.save(statement.toNeo4jStatement())
    }

    override fun count(): Long = neo4jRepository.count()

    override fun delete(statement: GeneralStatement) {
        neo4jRepository.delete(statement.toNeo4jStatement())
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(depth: Int): Iterable<GeneralStatement> =
        neo4jRepository.findAll(depth).map { it.toStatement() }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAll(pageable).map { it.toStatement() }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        neo4jRepository.findByStatementId(id).map { it.toStatement() }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubject(subjectId, pagination).map { it.toStatement() }

    override fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateId(predicateId, pagination).map { it.toStatement() }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObject(objectId, pagination).map { it.toStatement() }

    override fun countByIdRecursive(paperId: String): Int = neo4jRepository.countByIdRecursive(paperId)

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByObjectAndPredicate(objectId, predicateId, pagination).map { it.toStatement() }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pagination).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pagination)
            .map { it.toStatement() }

    override fun findAllBySubjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjects(subjectIds, pagination).map { it.toStatement() }

    override fun findAllByObjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObjects(subjectIds, pagination).map { it.toStatement() }

    override fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement> =
        neo4jRepository.fetchAsBundle(id, configuration).map { it.toStatement() }

    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> neo4jResourceRepository.findByResourceId(thing.resourceId).get().toResource()
            is Neo4jLiteral -> neo4jLiteralRepository.findByLiteralId(thing.literalId).get().toLiteral()
            else -> thing.toThing()
        }
    }

    private fun Neo4jStatement.toStatement(): GeneralStatement = GeneralStatement(
        id = statementId!!,
        subject = refreshObject(subject!!),
        predicate = neo4jPredicateRepository.findByPredicateId(predicateId!!).get().toPredicate(),
        `object` = refreshObject(`object`!!),
        createdAt = createdAt!!,
        createdBy = createdBy
    )

    private fun GeneralStatement.toNeo4jStatement(): Neo4jStatement =
        // Need to fetch the internal ID of a (possibly) existing entity to prevent creating a new one.
        neo4jRepository.findByStatementId(id!!).orElse(Neo4jStatement()).apply {
            statementId = this@toNeo4jStatement.id
            subject = this@toNeo4jStatement.subject.toNeo4jThing()
            `object` = this@toNeo4jStatement.`object`.toNeo4jThing()
            predicateId = this@toNeo4jStatement.predicate.id
            createdBy = this@toNeo4jStatement.createdBy
            createdAt = this@toNeo4jStatement.createdAt
        }

    private fun Thing.toNeo4jThing(): Neo4jThing =
        when (this) {
            is Class -> neo4jClassRepository.findByClassId(this.id).get()
            is Literal -> neo4jLiteralRepository.findByLiteralId(this.id).get()
            is Predicate -> neo4jPredicateRepository.findByPredicateId(this.id).get()
            is Resource -> neo4jResourceRepository.findByResourceId(this.id).get()
            else -> throw IllegalStateException("Unable to map unknown type ${this.javaClass.simpleName}. This is a bug!")
        }
}
