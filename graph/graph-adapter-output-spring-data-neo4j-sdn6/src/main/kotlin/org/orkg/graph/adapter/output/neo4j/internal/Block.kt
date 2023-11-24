package org.orkg.graph.adapter.output.neo4j.internal

data class Block(private val range: LongRange) : Iterator<Long> {
    private var counter = range.first

    override fun hasNext() = counter in range

    override fun next() = counter++
}
