package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*

// T should extend identity class?
abstract class RepositoryBasedIdGenerator<T, C : Neo4jCounter> : IdentityGenerator<T> {
    abstract fun createCounterNode(): C

    abstract fun idFromLong(value: Long): T

    abstract fun repository(): Neo4jIdCounterRepository<C>

    private var currentBlock: Block? = null

    private val node: C by lazy {
        val nodes = repository().findAll()
        require(nodes.size <= 1) {
            "Multiple nodes of type _ResourceIdCounter found, but only one should be present."
        }
        if (nodes.isEmpty())
            createInitialCounterNode()
        else
            nodes.first()
    }

    @Synchronized
    override fun nextIdentity(): T {
        if (currentBlock == null || !currentBlock!!.hasNext())
            currentBlock = nextBlock()
        return idFromLong(currentBlock!!.next())
    }

    private fun createInitialCounterNode() = repository().save(createCounterNode())

    private fun nextBlock(): Block {
        val lower = node.counter
        node.counter = lower + 1_000L
        val upper = repository().save(node).counter
        return Block(lower until upper)
    }

    data class Block(private val range: LongRange) : Iterator<Long> {
        private var counter = range.first

        override fun hasNext() = counter in range

        override fun next() = counter++
    }
}

@Component
class Neo4jResourceIdGenerator : RepositoryBasedIdGenerator<ResourceId, Neo4jResourceIdCounter>() {
    @Autowired
    private lateinit var repository: Neo4jResourceIdCounterRepository

    override fun createCounterNode() = Neo4jResourceIdCounter()

    override fun idFromLong(value: Long) = ResourceId(value)

    override fun repository() = repository
}

@Component
class Neo4jPredicateIdGenerator : RepositoryBasedIdGenerator<PredicateId, Neo4jPredicateIdCounter>() {
    @Autowired
    private lateinit var repository: Neo4jPredicateIdCounterRepository

    override fun createCounterNode() = Neo4jPredicateIdCounter()

    override fun idFromLong(value: Long) = PredicateId(value)

    override fun repository() = repository
}

@Component
class Neo4jLiteralIdGenerator : RepositoryBasedIdGenerator<LiteralId, Neo4jLiteralIdCounter>() {
    @Autowired
    private lateinit var repository: Neo4jLiteralIdCounterRepository

    override fun createCounterNode() = Neo4jLiteralIdCounter()

    override fun idFromLong(value: Long) = LiteralId(value)

    override fun repository() = repository
}

@Component
class Neo4jStatementIdGenerator : RepositoryBasedIdGenerator<StatementId, Neo4jStatementIdCounter>() {
    @Autowired
    private lateinit var repository: Neo4jStatementIdCounterRepository

    override fun createCounterNode() = Neo4jStatementIdCounter()

    override fun idFromLong(value: Long) = StatementId(value)

    override fun repository() = repository
}
