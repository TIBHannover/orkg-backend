package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long>, Neo4jResourceRepositoryCustom {
    override fun findAll(): Iterable<Neo4jResource>

    override fun findById(id: Long?): Optional<Neo4jResource>

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jResource>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jResource>
}

interface Neo4jResourceRepositoryCustom : IdentityGenerator<ResourceId>

class Neo4jResourceRepositoryCustomImpl : Neo4jResourceRepositoryCustom {
    var counter = 0L

    override fun nextIdentity(): ResourceId {
        counter++
        return ResourceId(counter)
    }
}
