package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.SearchString
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

    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByLabel(
                query = labelSearchString.query,
                label = labelSearchString.input,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByLabelContaining(
                label = labelSearchString.query,
                minLabelLength = labelSearchString.input.length,
                pageable = pageable
            )
        }.map(Neo4jPredicate::toPredicate)

    @Cacheable(key = "#id")
    override fun findById(id: ThingId): Optional<Predicate> =
        neo4jRepository.findById(id).map(Neo4jPredicate::toPredicate)

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteById(id: ThingId) {
        neo4jRepository.deleteById(id)
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
            CacheEvict(key = "#predicate.id", cacheNames = [THING_ID_TO_THING_CACHE]),
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
        } while (neo4jRepository.existsById(id))
        return id
    }

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findById(this.id).orElse(Neo4jPredicate()).apply {
            id = this@toNeo4jPredicate.id
            label = this@toNeo4jPredicate.label
            createdBy = this@toNeo4jPredicate.createdBy
            createdAt = this@toNeo4jPredicate.createdAt
        }
}
