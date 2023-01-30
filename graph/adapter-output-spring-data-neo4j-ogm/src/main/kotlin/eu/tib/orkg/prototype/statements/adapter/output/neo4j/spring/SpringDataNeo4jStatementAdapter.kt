package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
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
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

typealias PredicateLookupTable = Map<PredicateId, Predicate>

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jRepository: Neo4jStatementRepository,
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jLiteralRepository: Neo4jLiteralRepository,
    private val neo4jClassRepository: Neo4jClassRepository,
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val literalRepository: LiteralRepository,
    private val classRepository: ClassRepository,
) : StatementRepository {
    override fun nextIdentity(): StatementId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: StatementId
        do {
            id = neo4jStatementIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByStatementId(id))
        return id
    }

    override fun save(statement: GeneralStatement) {
        neo4jRepository.save(statement.toNeo4jStatement())
    }

    override fun count(): Long = neo4jRepository.count()

    override fun delete(statement: GeneralStatement) {
        neo4jRepository.deleteByStatementId(statement.id!!)
    }

    override fun deleteByStatementId(id: StatementId) {
        neo4jRepository.deleteByStatementId(id)
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(depth: Int): Iterable<GeneralStatement> =
        neo4jRepository.findAll(depth).map { it.toStatement() }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        val neo4jStatements = neo4jRepository.findAll(pageable)
        val predicateIds = neo4jStatements.content.mapNotNull(Neo4jStatement::predicateId).toSet()
        val table = neo4jPredicateRepository.findAllByPredicateIdIn(predicateIds)
            .map(Neo4jPredicate::toPredicate)
            .associateBy { it.id!! }
        return neo4jStatements.map { it.toStatement(table) }
    }

    override fun countStatementsAboutResource(id: ResourceId): Long =
        neo4jRepository.countStatementsByObjectId(id)

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long> =
        neo4jRepository.countStatementsAboutResource(resourceIds).associate { ResourceId(it.resourceId) to it.count }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        neo4jRepository.findByStatementId(id).map { it.toStatement() }

    override fun findAllBySubject(subjectId: String, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubject(subjectId, pageable).map { it.toStatement() }

    override fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateId(predicateId, pageable).map { it.toStatement() }

    override fun findAllByObject(objectId: String, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObject(objectId, pageable).map { it.toStatement() }

    override fun countByIdRecursive(paperId: String): Int = neo4jRepository.countByIdRecursive(paperId)

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByObjectAndPredicate(objectId, predicateId, pageable).map { it.toStatement() }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabel(predicateId, literal, pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pageable)
            .map { it.toStatement() }

    override fun findAllBySubjects(subjectIds: List<String>, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjects(subjectIds, pageable).map { it.toStatement() }

    override fun findAllByObjects(objectIds: List<String>, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObjects(objectIds, pageable).map { it.toStatement() }

    override fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement> =
        neo4jRepository.fetchAsBundle(id, configuration).map { it.toStatement() }

    override fun exists(id: StatementId): Boolean = neo4jRepository.existsByStatementId(id)

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> =
        neo4jRepository.findDOIByContributionId(id).map(Neo4jLiteral::toLiteral)

    private fun Neo4jStatement.toStatement(): GeneralStatement = GeneralStatement(
        id = statementId!!,
        subject = subject!!.toThing(),
        predicate = predicateRepository.findByPredicateId(predicateId!!).get(),
        `object` = `object`!!.toThing(),
        createdAt = createdAt!!,
        createdBy = createdBy
    )

    private fun Neo4jStatement.toStatement(lookupTable: PredicateLookupTable): GeneralStatement = GeneralStatement(
        id = statementId!!,
        subject = subject!!.toThing(),
        predicate = lookupTable[predicateId]
            ?: throw IllegalStateException("Predicate $predicateId not found in lookup table. This is a bug."),
        `object` = `object`!!.toThing(),
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
        // The purpose of this method is of technical nature, as the OGM requires a reference to the start and end node.
        // With direct access to the database, this becomes obsolete, because queries can use the ID directly.
        when (this) {
            is Class -> neo4jClassRepository.findByClassId(this.id.toClassId()).get()
            is Literal -> neo4jLiteralRepository.findByLiteralId(this.id).get()
            is Predicate -> neo4jPredicateRepository.findByPredicateId(this.id).get()
            is Resource -> neo4jResourceRepository.findByResourceId(this.id).get()
        }
}
