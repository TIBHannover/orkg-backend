package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.*

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): Iterable<Neo4jResource>
}
