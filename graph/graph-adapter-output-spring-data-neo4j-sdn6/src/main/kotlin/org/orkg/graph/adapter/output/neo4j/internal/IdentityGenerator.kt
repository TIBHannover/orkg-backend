package org.orkg.graph.adapter.output.neo4j.internal

interface IdentityGenerator<out T> {
    fun nextIdentity(): T
}
