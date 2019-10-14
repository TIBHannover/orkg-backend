package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

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

    override fun findAll(pageable: Pageable): Iterable<Predicate> {
        return neo4jPredicateRepository
            .findAll(pageable)
            .content
            .map(Neo4jPredicate::toPredicate)
    }

    override fun findById(id: PredicateId?): Optional<Predicate> =
        neo4jPredicateRepository
            .findByPredicateId(id)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabel(label: String, pageable: Pageable) =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex("(?i)^$label$", pageable) // TODO: See declaration
            .content
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex("(?i).*$part.*", pageable) // TODO: See declaration
            .content
            .map(Neo4jPredicate::toPredicate)

    override fun update(predicate: Predicate): Predicate {
        // already checked by service
        val found = neo4jPredicateRepository.findByPredicateId(predicate.id).get()

        // update all the properties
        found.label = predicate.label

        return neo4jPredicateRepository.save(found).toPredicate()
    }
}
