package eu.tib.orkg.prototype.graphdb.indexing.domain.model

interface IndexService {
    fun verifyIndices()

    fun getIndexes(): Iterable<Neo4jIndex>
}
