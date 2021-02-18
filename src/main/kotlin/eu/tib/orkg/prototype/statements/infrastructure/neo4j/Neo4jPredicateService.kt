package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jPredicateService(
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jPredicateIdGenerator: Neo4jPredicateIdGenerator
) : PredicateService {

    override fun create(label: String) = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): Predicate {
        val id = neo4jPredicateIdGenerator.nextIdentity()
        return neo4jPredicateRepository
            .save(Neo4jPredicate(label = label, predicateId = id, createdBy = userId))
            .toPredicate()
    }

    override fun create(request: CreatePredicateRequest) = create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreatePredicateRequest): Predicate {
        val id = request.id ?: neo4jPredicateIdGenerator.nextIdentity()
        return neo4jPredicateRepository
            .save(Neo4jPredicate(label = request.label, predicateId = id, createdBy = userId))
            .toPredicate()
    }

    override fun findAll(pageable: Pageable): Page<Predicate> {
        return neo4jPredicateRepository
            .findAll(pageable)
            .map(Neo4jPredicate::toPredicate)
    }

    override fun findById(id: PredicateId?): Optional<Predicate> =
        neo4jPredicateRepository
            .findByPredicateId(id)
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration
            .map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> =
        neo4jPredicateRepository
            .findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration
            .map(Neo4jPredicate::toPredicate)

    override fun update(predicate: Predicate): Predicate {
        // already checked by service
        val found = neo4jPredicateRepository.findByPredicateId(predicate.id).get()

        // update all the properties
        found.label = predicate.label

        return neo4jPredicateRepository.save(found).toPredicate()
    }

    override fun removeAll() = neo4jPredicateRepository.deleteAll()

    private fun String.toSearchString() = "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() = "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
