package eu.tib.orkg.prototype.statements.domain.model

data class Block(private val range: LongRange) : Iterator<Long> {
    private var counter = range.first

    override fun hasNext() = counter in range

    override fun next() = counter++
}
