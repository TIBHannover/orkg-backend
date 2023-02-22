package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

const val PREDICATE_ID_TO_PREDICATE_CACHE = "predicate-id-to-predicate"

@Component
@CacheConfig(cacheNames = [PREDICATE_ID_TO_PREDICATE_CACHE])
class SpringDataNeo4jPredicateAdapter(
    private val neo4jRepository: Neo4jPredicateRepository,
    private val idGenerator: Neo4jPredicateIdGenerator
) : PredicateRepository {
    override fun findAll(pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAll(pageable).map(Neo4jPredicate::toPredicate)

    override fun exists(id: ThingId): Boolean = neo4jRepository.existsByPredicateId(id.toPredicateId())

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabel(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jPredicate::toPredicate)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jPredicate::toPredicate)

    @Cacheable(key = "#id")
    override fun findByPredicateId(id: ThingId): Optional<Predicate> =
        neo4jRepository.findByPredicateId(id.toPredicateId()).map(Neo4jPredicate::toPredicate)

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id.value", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteByPredicateId(id: ThingId) {
        neo4jRepository.deleteByPredicateId(id.toPredicateId())
    }

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    @Caching(
        evict = [
            CacheEvict(key = "#predicate.id"),
            CacheEvict(key = "#predicate.id.value", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(predicate: Predicate) {
        neo4jRepository.save(predicate.toNeo4jPredicate())
    }

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = idGenerator.nextIdentity()
        } while (neo4jRepository.existsByPredicateId(id.toPredicateId()))
        return id
    }

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findByPredicateId(id.toPredicateId()).orElse(Neo4jPredicate()).apply {
            predicateId = this@toNeo4jPredicate.id.toPredicateId()
            label = this@toNeo4jPredicate.label
            createdBy = this@toNeo4jPredicate.createdBy
            createdAt = this@toNeo4jPredicate.createdAt
        }
}
