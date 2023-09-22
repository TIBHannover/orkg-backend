package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

interface IdentityGenerator<out T> {
    fun nextIdentity(): T
}
