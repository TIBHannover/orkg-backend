package org.orkg.graph.adapter.output.neo4j.internal

import org.springframework.data.neo4j.core.schema.Node

@Node("_LiteralIdCounter")
class Neo4jLiteralIdCounter : Neo4jCounter()
