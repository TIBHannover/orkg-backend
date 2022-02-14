package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdCounter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdCounterRepository
import eu.tib.orkg.prototype.statements.domain.model.Block
import eu.tib.orkg.prototype.statements.domain.model.IdentityGenerator
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.stereotype.Component

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
}

@Component
class Neo4jResourceIdGenerator(
    private val repository: Neo4jResourceIdCounterRepository
) :
    RepositoryBasedIdGenerator<ResourceId, Neo4jResourceIdCounter>() {

    override fun createCounterNode() = Neo4jResourceIdCounter()

    override fun idFromLong(value: Long) = ResourceId(value)

    override fun repository() = repository
}

@Component
class Neo4jLiteralIdGenerator(
    private val repository: Neo4jLiteralIdCounterRepository
) : RepositoryBasedIdGenerator<LiteralId, Neo4jLiteralIdCounter>() {

    override fun createCounterNode() = Neo4jLiteralIdCounter()

    override fun idFromLong(value: Long) = LiteralId(value)

    override fun repository() = repository
}

@Component
class Neo4jStatementIdGenerator(
    private val repository: Neo4jStatementIdCounterRepository
) : RepositoryBasedIdGenerator<StatementId, Neo4jStatementIdCounter>() {

    override fun createCounterNode() = Neo4jStatementIdCounter()

    override fun idFromLong(value: Long) = StatementId(value)

    override fun repository() = repository
}
