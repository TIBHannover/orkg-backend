package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

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
            .findById(`object`.value)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val persistedStatement = neo4jStatementRepository.save(
            Neo4jStatementWithLiteral(
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject
            )
        )

        return StatementWithLiteral(
            persistedStatement.id!!,
            foundSubject.toResource(),
            foundPredicate.get(),
            foundObject.toObject()
        )
    }

    override fun findAll(): Iterable<StatementWithLiteral> {
        return neo4jStatementRepository
            .findAll()
            .map {
                StatementWithLiteral(
                    it.id!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun findById(statementId: Long): Optional<StatementWithLiteral> =
        neo4jStatementRepository
            .findById(statementId)
            .map {
                StatementWithLiteral(
                    it.id!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }

    override fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithLiteral> {
        val resource = neo4jResourceRepository.findByResourceId(resourceId).get()
        return neo4jStatementRepository
            .findAllBySubject(resource.resourceId!!)
            .map {
                StatementWithLiteral(
                    it.id!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
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
                    it.id!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }

    override fun findAllByPredicate(predicateId: PredicateId) =
        neo4jStatementRepository
            .findAllByPredicateId(predicateId)
            .map {
                StatementWithLiteral(
                    it.id!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
}
