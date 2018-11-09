package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): Iterable<Neo4jResource>

    override fun findById(id: Long?): Optional<Neo4jResource>

    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jResource>
}
