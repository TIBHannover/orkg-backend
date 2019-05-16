package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.*
import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

@Service
@Transactional
class Neo4jPredicateService(
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jPredicateIdGenerator: Neo4jPredicateIdGenerator
) : PredicateService {

    override fun create(label: String): Predicate {
        val id = neo4jPredicateIdGenerator.nextIdentity()
        return neo4jPredicateRepository
            .save(Neo4jPredicate(label = label, predicateId = id))
            .toPredicate()
    }

    override fun create(request: CreatePredicateRequest): Predicate {
        val id = request.id ?: neo4jPredicateIdGenerator.nextIdentity()
        return neo4jPredicateRepository
            .save(Neo4jPredicate(label = request.label, predicateId = id))
            .toPredicate()
    }

    override fun findAll() = neo4jPredicateRepository
        .findAll()
        .map(Neo4jPredicate::toPredicate)

    override fun findById(id: PredicateId?): Optional<Predicate> =
        neo4jPredicateRepository
            .findByPredicateId(id)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabel(label: String) =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex("(?i)^$label$") // TODO: See declaration
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String) =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex("(?i).*$part.*") // TODO: See declaration
            .map(Neo4jPredicate::toPredicate)
}
