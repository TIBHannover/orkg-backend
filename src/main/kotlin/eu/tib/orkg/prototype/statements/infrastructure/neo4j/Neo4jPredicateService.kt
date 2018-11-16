package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

@Service
@Transactional
class Neo4jPredicateService(
    private val neo4jPredicateRepository: Neo4jPredicateRepository
) : PredicateService {

    override fun create(label: String) =
        neo4jPredicateRepository
            .save(Neo4jPredicate(label = label))
            .toPredicate()

    override fun findAll() = neo4jPredicateRepository
        .findAll()
        .map(Neo4jPredicate::toPredicate)

    override fun findById(id: PredicateId?): Optional<Predicate> =
        neo4jPredicateRepository
            .findById(id?.value)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabel(label: String) =
        neo4jPredicateRepository
            .findAllByLabel(label)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String) =
        neo4jPredicateRepository
            .findAllByLabelContaining(part)
            .map(Neo4jPredicate::toPredicate)
}
