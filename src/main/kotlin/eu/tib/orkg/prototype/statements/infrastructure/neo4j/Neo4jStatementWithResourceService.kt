package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResource
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithResourceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

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
            foundObject.toObject(),
            persistedStatement.createdAt!!
        )
    }

    override fun findAll(pagination: Pageable): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAll(pagination)
            .content
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
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
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }
    }

    override fun findAllBySubject(resourceId: ResourceId, pagination: Pageable): Iterable<StatementWithResource> {
        val resource = neo4jResourceRepository.findByResourceId(resourceId).get()
        return neo4jStatementRepository
            .findAllBySubject(resource.resourceId!!, pagination)
            .content
            .map {
                StatementWithResource(
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
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAllBySubjectAndPredicate(resourceId, predicateId, pagination)
            .content
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }
    }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<StatementWithResource> {
        return neo4jStatementRepository
            .findAllByPredicateId(predicateId, pagination)
            .content
            .map {
                StatementWithResource(
                    it.statementId!!,
                    it.subject!!.toResource(),
                    predicateService.findById(it.predicateId!!).get(),
                    it.`object`!!.toObject(),
                    it.createdAt!!
                )
            }
    }

    override fun findAllByObject(objectId: ResourceId, pagination: Pageable): Iterable<StatementWithResource> {
        val resource = neo4jResourceRepository.findByResourceId(objectId)
        if (resource.isPresent) {
            return neo4jStatementRepository
                .findAllByObject(resource.get().resourceId!!, pagination)
                .content
                .map {
                    StatementWithResource(
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

    override fun update(statementEditRequest: StatementEditRequest): StatementWithResource {
        // already checked by service
        val found = neo4jStatementRepository.findByStatementId(statementEditRequest.statementId!!).get()

        // update all the properties
        found.predicateId = statementEditRequest.predicateId
        found.subject = neo4jResourceRepository.findByResourceId(statementEditRequest.subjectId).get()
        found.`object` = neo4jResourceRepository.findByResourceId(statementEditRequest.objectId).get()

        neo4jStatementRepository.save(found)

        return StatementWithResource(
            found.statementId!!,
            found.subject!!.toResource(),
            predicateService.findById(found.predicateId!!).get(),
            found.`object`!!.toObject()
        )
    }
}
