package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

@Service
@Transactional
class Neo4jStatementWithResourceService : StatementWithResourceService {

    @Autowired
    private lateinit var neo4jResourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var neo4jStatementRepository: Neo4jStatementWithResourceRepository

    @Autowired
    private lateinit var neo4jStatementIdGenerator: Neo4jStatementIdGenerator

    override fun create(
        subject: ResourceId,
        predicate: PredicateId,
        `object`: ResourceId
    ): StatementWithResource {
        val foundSubject = neo4jResourceRepository
            .findByResourceId(subject)
            .orElseThrow { IllegalStateException("Could not find subject") }

        val foundPredicate = predicateService.findById(predicate)
        if (!foundPredicate.isPresent)
            throw IllegalArgumentException("Resource could not be found.")

        val foundObject = neo4jResourceRepository
            .findByResourceId(`object`)
            .orElseThrow { IllegalStateException("Could not find object") }

        val id = neo4jStatementIdGenerator.nextIdentity()

        val persistedStatement = neo4jStatementRepository.save(
            Neo4jStatementWithResource(
                statementId = id,
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject
            )
        )

        return StatementWithResource(
            persistedStatement.statementId!!,
            foundSubject.toResource(),
            foundPredicate.get(),
            foundObject.toObject()
        )
    }

    override fun findAll(): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAll()
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun findById(statementId: StatementId): Optional<StatementWithResource> {
        return neo4jStatementRepository
            .findByStatementId(statementId)
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithResource> {
        val resource = neo4jResourceRepository.findByResourceId(resourceId).get()
        return neo4jStatementRepository
            .findAllBySubject(resource.resourceId!!)
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAllBySubjectAndPredicate(resourceId, predicateId)
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun findAllByPredicate(predicateId: PredicateId): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAllByPredicateId(predicateId)
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject()
                )
            }
    }

    override fun totalNumberOfStatements() = neo4jStatementRepository.count()
}
