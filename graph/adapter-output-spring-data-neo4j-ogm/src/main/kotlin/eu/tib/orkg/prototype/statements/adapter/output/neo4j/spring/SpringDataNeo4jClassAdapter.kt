package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClass
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

const val CLASS_ID_TO_CLASS_CACHE = "class-id-to-class"
const val CLASS_ID_TO_CLASS_EXISTS_CACHE = "class-id-to-class-exists"

@Component
@CacheConfig(cacheNames = [CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE])
class SpringDataNeo4jClassAdapter(
    private val neo4jRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator,
) : ClassRepository {

    @Caching(
        evict = [
            CacheEvict(key = "#c.id", cacheNames = [CLASS_ID_TO_CLASS_CACHE]),
            CacheEvict(key = "#c.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(c: Class) {
        neo4jRepository.save(c.toNeo4jClass(neo4jRepository))
    }

    override fun findAll(pageable: Pageable): Page<Class> = neo4jRepository.findAll(pageable).map(Neo4jClass::toClass)

    @Cacheable(key = "#id", cacheNames = [CLASS_ID_TO_CLASS_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsByClassId(id.toClassId())

    override fun existsAll(ids: Set<ThingId>): Boolean =
        neo4jRepository.existsAllByClassId(ids.toClassIds())

    @Cacheable(key = "#id", cacheNames = [CLASS_ID_TO_CLASS_CACHE])
    override fun findByClassId(id: ThingId): Optional<Class> =
        neo4jRepository.findByClassId(id.toClassId()).map(Neo4jClass::toClass)

    override fun findAllByClassId(id: Iterable<ThingId>, pageable: Pageable): Page<Class> =
        neo4jRepository.findAllByClassIdIn(id.toClassIds(), pageable).map(Neo4jClass::toClass)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Class> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByLabel(labelSearchString.value, pageable)
            is FuzzySearchString -> neo4jRepository.findAllByLabelContaining(labelSearchString.value, pageable)
        }.map(Neo4jClass::toClass)

    override fun findByUri(uri: String): Optional<Class> = neo4jRepository.findByUri(uri).map(Neo4jClass::toClass)

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jClassIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByClassId(id.toClassId()))
        return id
    }
}
