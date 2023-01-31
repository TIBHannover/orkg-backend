package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

data class Block(private val range: LongRange) : Iterator<Long> {
    private var counter = range.first

    override fun hasNext() = counter in range

    override fun next() = counter++
}
