package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@CacheConfig(cacheNames = ["predicates"])
class SpringDataNeo4jPredicateAdapter(
    private val neo4jRepository: Neo4jPredicateRepository,
    private val idGenerator: Neo4jPredicateIdGenerator
) : PredicateRepository {
    override fun findAll(pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAll(pageable).map(Neo4jPredicate::toPredicate)

    override fun exists(id: PredicateId): Boolean = neo4jRepository.existsByPredicateId(id)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabel(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jPredicate::toPredicate)

    @Cacheable(key = "#id")
    override fun findByPredicateId(id: PredicateId?): Optional<Predicate> =
        neo4jRepository.findByPredicateId(id).map(Neo4jPredicate::toPredicate)

    @CacheEvict(key = "#id")
    override fun deleteByPredicateId(id: PredicateId) {
        neo4jRepository.deleteByPredicateId(id)
    }

    @CacheEvict(allEntries = true)
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    @CacheEvict(key = "#predicate.id")
    override fun save(predicate: Predicate) {
        neo4jRepository.save(predicate.toNeo4jPredicate())
    }

    override fun nextIdentity(): PredicateId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: PredicateId
        do {
            id = idGenerator.nextIdentity()
        } while (neo4jRepository.existsByPredicateId(id))
        return id
    }

    override fun usageCount(id: PredicateId) = neo4jRepository.countUsage(id)

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findByPredicateId(id).orElse(Neo4jPredicate()).apply {
            predicateId = this@toNeo4jPredicate.id
            label = this@toNeo4jPredicate.label
            createdBy = this@toNeo4jPredicate.createdBy
            createdAt = this@toNeo4jPredicate.createdAt
        }
}
