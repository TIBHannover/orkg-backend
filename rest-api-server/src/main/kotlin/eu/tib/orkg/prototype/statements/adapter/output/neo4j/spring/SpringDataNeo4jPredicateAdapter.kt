package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jPredicateAdapter(
    private val neo4jRepository: Neo4jPredicateRepository,
    private val idGenerator: Neo4jPredicateIdGenerator
) : PredicateRepository {
    override fun findAll(pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAll(pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabel(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jPredicate::toPredicate)

    override fun findByPredicateId(id: PredicateId?): Optional<Predicate> =
        neo4jRepository.findByPredicateId(id).map(Neo4jPredicate::toPredicate)

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun save(predicate: Predicate) {
        neo4jRepository.save(predicate.toNeo4jPredicate())
    }

    override fun nextIdentity(): PredicateId = idGenerator.nextIdentity()

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findByPredicateId(id).orElse(Neo4jPredicate()).apply {
            predicateId = this@toNeo4jPredicate.id
            label = this@toNeo4jPredicate.label
            createdBy = this@toNeo4jPredicate.createdBy
            createdAt = this@toNeo4jPredicate.createdAt
        }
}
