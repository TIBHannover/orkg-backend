package eu.tib.orkg.prototype.graphdb.indexing.domain.model

import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.Neo4jIndex

interface IndexService {

    fun verifyIndices()

    fun getIndexes(): Iterable<Neo4jIndex>
}
