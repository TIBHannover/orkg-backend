package org.orkg.graph.adapter.output.neo4j.internal

// T should extend identity class?
abstract class RepositoryBasedIdGenerator<T>(
    private val id: String,
    private val repository: Neo4jIdCounterRepository
) : IdentityGenerator<T> {
    abstract fun idFromLong(value: Long): T

    private var currentBlock: Block? = null

    private val node: Neo4jIdCounter by lazy {
        repository.findById(id).orElseGet { Neo4jIdCounter().apply { id = "Class" } }
    }

    @Synchronized
    override fun nextIdentity(): T {
        if (currentBlock == null || !currentBlock!!.hasNext())
            currentBlock = nextBlock()
        return idFromLong(currentBlock!!.next())
    }

    private fun nextBlock(): Block {
        val lower = node.counter
        node.counter = lower + 1_000L
        val upper = repository.save(node).counter
        return Block(lower until upper)
    }
}
