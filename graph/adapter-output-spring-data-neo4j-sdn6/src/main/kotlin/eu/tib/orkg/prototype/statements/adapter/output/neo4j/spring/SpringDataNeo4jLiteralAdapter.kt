package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

const val LITERAL_ID_TO_LITERAL_CACHE = "literal-id-to-literal"
const val LITERAL_ID_TO_LITERAL_EXISTS_CACHE = "literal-id-to-literal-exists"

@Component
@CacheConfig(cacheNames = [LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE])
class SpringDataNeo4jLiteralAdapter(
    private val neo4jRepository: Neo4jLiteralRepository,
    private val neo4jLiteralIdGenerator: Neo4jLiteralIdGenerator
) : LiteralRepository {
    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jLiteralIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByLiteralId(id.toLiteralId()))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#literal.id", cacheNames = [LITERAL_ID_TO_LITERAL_CACHE]),
            CacheEvict(key = "#literal.id.value", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(literal: Literal) {
        neo4jRepository.save(literal.toNeo4jLiteral())
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

    override fun findAll(pageable: Pageable): Page<Literal> =
        neo4jRepository.findAll(pageable).map(Neo4jLiteral::toLiteral)

    @Cacheable(key = "#id", cacheNames = [LITERAL_ID_TO_LITERAL_CACHE])
    override fun findByLiteralId(id: ThingId): Optional<Literal> =
        neo4jRepository.findByLiteralId(id.toLiteralId()).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(value: String, pageable: Pageable): Page<Literal> =
        neo4jRepository.findAllByLabel(value, pageable).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Literal> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Literal> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jLiteral::toLiteral)

    @Cacheable(key = "#id", cacheNames = [LITERAL_ID_TO_LITERAL_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsByLiteralId(id.toLiteralId())

    private fun Literal.toNeo4jLiteral() =
        neo4jRepository.findByLiteralId(id.toLiteralId()).orElse(Neo4jLiteral()).apply {
            literalId = this@toNeo4jLiteral.id.toLiteralId()
            label = this@toNeo4jLiteral.label
            datatype = this@toNeo4jLiteral.datatype
            createdAt = this@toNeo4jLiteral.createdAt
            createdBy = this@toNeo4jLiteral.createdBy
        }
}
