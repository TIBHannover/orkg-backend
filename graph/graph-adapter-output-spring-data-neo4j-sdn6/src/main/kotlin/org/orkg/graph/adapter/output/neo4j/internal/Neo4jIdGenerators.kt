package org.orkg.graph.adapter.output.neo4j.internal

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
