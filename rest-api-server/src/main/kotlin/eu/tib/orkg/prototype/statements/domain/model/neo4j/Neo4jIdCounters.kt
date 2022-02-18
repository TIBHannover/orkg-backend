package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.springframework.data.neo4j.repository.Neo4jRepository

abstract class Neo4jCounter {
    @Property
    @Required
    var counter: Long = 0L
}

interface Neo4jIdCounterRepository<T : Neo4jCounter> : Neo4jRepository<T, Long> {
    override fun findAll(): List<T>
    override fun <S : T> save(s: S, depth: Int): S
}
