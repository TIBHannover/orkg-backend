package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.repository.Neo4jRepository

abstract class Neo4jCounter {
    @Property
    var counter: Long = 0L
}

interface Neo4jIdCounterRepository<T : Neo4jCounter> : Neo4jRepository<T, Long> {
    override fun findAll(): List<T>
    override fun <S : T> save(entity: S): S
}

@Node("_ResourceIdCounter")
data class Neo4jResourceIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jResourceIdCounterRepository : Neo4jIdCounterRepository<Neo4jResourceIdCounter>

@Node("_PredicateIdCounter")
data class Neo4jPredicateIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jPredicateIdCounterRepository : Neo4jIdCounterRepository<Neo4jPredicateIdCounter>

@Node("_LiteralIdCounter")
data class Neo4jLiteralIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jLiteralIdCounterRepository : Neo4jIdCounterRepository<Neo4jLiteralIdCounter>

@Node("_StatementIdCounter")
data class Neo4jStatementIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jStatementIdCounterRepository : Neo4jIdCounterRepository<Neo4jStatementIdCounter>

@Node("_ClassIdCounter")
data class Neo4jClassIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()

interface Neo4jClassIdCounterRepository : Neo4jIdCounterRepository<Neo4jClassIdCounter>
