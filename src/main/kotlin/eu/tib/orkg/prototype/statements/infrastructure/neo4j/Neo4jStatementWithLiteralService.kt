package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteral
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteralService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithLiteralRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
@Transactional
class Neo4jStatementWithLiteralService :
    StatementWithLiteralService {

    @Autowired
    private lateinit var neo4jResourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var neo4jLiteralRepository: Neo4jLiteralRepository

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var neo4jStatementRepository: Neo4jStatementWithLiteralRepository

    @Autowired
    private lateinit var neo4jStatementIdGenerator: Neo4jStatementIdGenerator

    override fun create(
        subject: ResourceId,
        predicate: PredicateId,
        `object`: LiteralId
    ): StatementWithLiteral {
        val foundSubject = neo4jResourceRepository
            .findByResourceId(subject)
            .orElseThrow { IllegalStateException("Could not find subject") }

        val foundPredicate = predicateService.findById(predicate)
        if (!foundPredicate.isPresent)
            throw IllegalArgumentException("Resource could not be found: $subject")

        val foundObject = neo4jLiteralRepository
            .findByLiteralId(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val id = neo4jStatementIdGenerator.nextIdentity()

        val persistedStatement = neo4jStatementRepository.save(
            Neo4jStatementWithLiteral(
                statementId = id,
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject
            )
        )

        return StatementWithLiteral(
            persistedStatement.statementId!!,
            foundSubject.toResource(),
            foundPredicate.get(),
            foundObject.toObject(),
            persistedStatement.createdAt!!
        )
    }

    override fun findAll(): Iterable<StatementWithLiteral> {
        return neo4jStatementRepository
            .findAll()
            .map {
                StatementWithLiteral(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }
    }

    override fun findById(statementId: StatementId): Optional<StatementWithLiteral> =
        neo4jStatementRepository
            .findByStatementId(statementId)
            .map {
                StatementWithLiteral(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }

    override fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithLiteral> {
        val resource = neo4jResourceRepository.findByResourceId(resourceId).get()
        return neo4jStatementRepository
            .findAllBySubject(resource.resourceId!!)
            .map {
                StatementWithLiteral(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }
    }

    override fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ) =
        neo4jStatementRepository
            .findAllBySubjectAndPredicate(resourceId, predicateId)
            .map {
                StatementWithLiteral(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }

    override fun findAllByPredicate(predicateId: PredicateId) =
        neo4jStatementRepository
            .findAllByPredicateId(predicateId)
            .map {
                StatementWithLiteral(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }

    override fun findAllByObject(objectId: LiteralId): Iterable<StatementWithLiteral> {
        val literal = neo4jLiteralRepository.findByLiteralId(objectId)
        if (literal.isPresent) {
            return neo4jStatementRepository
                .findAllByObject(literal.get().literalId!!)
                .map {
                    StatementWithLiteral(
                        it.statementId!!,
                        it.subject!!.toResource(),
                        predicateService.findById(it.predicateId!!).get(),
                        it.`object`!!.toObject(),
                        it.createdAt!!
                    )
                }
        }
        return emptyList()
    }

    override fun totalNumberOfStatements() = neo4jStatementRepository.count()

    override fun remove(statementId: StatementId) {
        val toDelete = neo4jStatementRepository.findByStatementId(statementId)

        neo4jStatementRepository.delete(toDelete.get())
    }
}
