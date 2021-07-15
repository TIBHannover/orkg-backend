package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
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

@NodeEntity("_ResourceIdCounter")
data class Neo4jResourceIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jResourceIdCounterRepository : Neo4jIdCounterRepository<Neo4jResourceIdCounter>

@NodeEntity("_PredicateIdCounter")
data class Neo4jPredicateIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jPredicateIdCounterRepository : Neo4jIdCounterRepository<Neo4jPredicateIdCounter>

@NodeEntity("_LiteralIdCounter")
data class Neo4jLiteralIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jLiteralIdCounterRepository : Neo4jIdCounterRepository<Neo4jLiteralIdCounter>

@NodeEntity("_StatementIdCounter")
data class Neo4jStatementIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jStatementIdCounterRepository : Neo4jIdCounterRepository<Neo4jStatementIdCounter>

@NodeEntity("_ClassIdCounter")
data class Neo4jClassIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jClassIdCounterRepository : Neo4jIdCounterRepository<Neo4jClassIdCounter>
