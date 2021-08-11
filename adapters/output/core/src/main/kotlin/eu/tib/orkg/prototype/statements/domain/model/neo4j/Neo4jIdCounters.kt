package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.repository.Neo4jRepository

abstract class Neo4jCounter {
    @Id
    @GeneratedValue
    private var id: Long? = null

    @Property
    var counter: Long = 0L
}

interface Neo4jIdCounterRepository<T : Neo4jCounter> : Neo4jRepository<T, Long> {
    override fun findAll(): List<T>
    override fun <S : T> save(entity: S): S
}

@Node(primaryLabel = "_ResourceIdCounter")
class Neo4jResourceIdCounter : Neo4jCounter()

interface Neo4jResourceIdCounterRepository : Neo4jIdCounterRepository<Neo4jResourceIdCounter>

@Node(primaryLabel = "_PredicateIdCounter")
class Neo4jPredicateIdCounter : Neo4jCounter()

interface Neo4jPredicateIdCounterRepository : Neo4jIdCounterRepository<Neo4jPredicateIdCounter>

@Node(primaryLabel = "_LiteralIdCounter")
class Neo4jLiteralIdCounter : Neo4jCounter()

interface Neo4jLiteralIdCounterRepository : Neo4jIdCounterRepository<Neo4jLiteralIdCounter>

@Node(primaryLabel = "_StatementIdCounter")
class Neo4jStatementIdCounter : Neo4jCounter()

interface Neo4jStatementIdCounterRepository : Neo4jIdCounterRepository<Neo4jStatementIdCounter>

@Node(primaryLabel = "_ClassIdCounter")
class Neo4jClassIdCounter : Neo4jCounter()

interface Neo4jClassIdCounterRepository : Neo4jIdCounterRepository<Neo4jClassIdCounter>
