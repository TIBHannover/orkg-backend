package eu.tib.orkg.prototype.statements.infrastructure.neo4j.bulk

import eu.tib.orkg.prototype.statements.domain.model.BulkStatementsService
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jBulkStatementService :
    BulkStatementsService {

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var literalService: LiteralService

    @Autowired
    private lateinit var statementRepository: Neo4jStatementRepository

    override fun getBulkStatementsBySubjects(
        subjects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllBySubjects(subjects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .map { toStatement(it) }
            .groupBy { it.id!!.value }
    }

    override fun getBulkStatementsByObjects(
        objects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllByObjects(objects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .map { toStatement(it) }
            .groupBy { it.id!!.value }
    }

    // FIXME: This is duplicated from the StatementService
    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> resourceService.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalService.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    // FIXME: This is duplicated from the StatementService
    private fun toStatement(statement: Neo4jStatement) =
        GeneralStatement(
            id = statement.statementId!!,
            subject = refreshObject(statement.subject!!),
            predicate = predicateService.findById(statement.predicateId!!).get(),
            `object` = refreshObject(statement.`object`!!),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy
        )
}
