package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.ports.PredicateRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class PredicatePersistenceAdapter(
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jPredicateIdGenerator: Neo4jPredicateIdGenerator,
) : PredicateRepository {
    
    override fun nextIdentity(): PredicateId = neo4jPredicateIdGenerator.nextIdentity()

    override fun save(predicate: Predicate) {
        neo4jPredicateRepository.save(
            Neo4jPredicate().apply {
                predicateId = predicate.id
                label = predicate.label
                createdAt = predicate.createdAt
                createdBy = predicate.createdBy
            })
    }

    override fun findAll(pageable: Pageable): Page<Predicate> =
        neo4jPredicateRepository
            .findAll(pageable)
            .map(Neo4jPredicate::toPredicate)

    override fun findById(id: PredicateId?): Optional<Predicate> =
        neo4jPredicateRepository
            .findByPredicateId(id)
            .map(Neo4jPredicate::toPredicate)

    fun findAllById(ids: List<PredicateId>): Iterable<Predicate> =
        neo4jPredicateRepository
            .findAllByPredicateIdIn(ids)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Predicate> =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable):
        Page<Predicate> =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex(part.toSearchString(), pageable)
            .map(Neo4jPredicate::toPredicate)

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
