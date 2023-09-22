package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import org.springframework.data.neo4j.core.schema.Node

@Node("_ResourceIdCounter")
class Neo4jResourceIdCounter : Neo4jCounter()
