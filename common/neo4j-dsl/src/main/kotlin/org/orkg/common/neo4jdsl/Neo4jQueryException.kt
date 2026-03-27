package org.orkg.common.neo4jdsl

import org.springframework.dao.DataAccessException
import java.io.Serial

// It is necessary to extend DataAccessException to properly close ongoing transactions
@Suppress("serial")
class Neo4jQueryException : DataAccessException {
    val parameters: Map<String, Any>
    val cypher: String

    constructor(
        cause: Throwable,
        cypher: String,
        parameters: Map<String, Any>,
    ) : super("Failed to execute Neo4j query.", cause) {
        this.cypher = cypher
        this.parameters = parameters
    }

    override val message: String?
        get() = "${super.message!!} Cypher: $cypher, Parameters: $parameters"
}
