package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndex

interface IndexService {

    fun verifyIndices()

    fun getIndexes(): Iterable<Neo4jIndex>
}
