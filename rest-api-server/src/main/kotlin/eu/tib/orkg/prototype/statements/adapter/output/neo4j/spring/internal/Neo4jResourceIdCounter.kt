package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jCounter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity("_ResourceIdCounter")
data class Neo4jResourceIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()
