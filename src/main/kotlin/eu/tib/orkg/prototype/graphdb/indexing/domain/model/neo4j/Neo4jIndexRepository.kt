package eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.neo4j.ogm.session.Session
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jIndexRepository : Neo4jRepository<Neo4jResource, Long>,
    IndexRepository {

    @Query("""CALL db.indexes() YIELD tokenNames, properties, type RETURN tokenNames[0] as label, properties[0] as property, type as type""")
    fun getExistingIndicesAndConstraints(): Iterable<Neo4jIndexInfo>
}

interface IndexRepository {
    fun createIndex(index: Neo4jIndex)
}

class IndexRepositoryImpl(private val ogmSession: Session) :
    IndexRepository {

    override fun createIndex(index: Neo4jIndex) {
        ogmSession.query(index.toCypherQuery(), emptyMap<String, Any>())
    }
}

interface Neo4jIndex {
    fun toCypherQuery(): String
}

data class UniqueIndex(private val label: String, private val property: String) :
    Neo4jIndex {
    override fun toCypherQuery() = """CREATE CONSTRAINT ON (n:$label) ASSERT n.$property IS UNIQUE;"""
}

data class PropertyIndex(val label: String, val property: String) :
    Neo4jIndex {
    override fun toCypherQuery() = """CREATE INDEX ON :$label($property);"""
}

data class FulltextIndex(private val label: String, private val property: String) : Neo4jIndex {
    override fun toCypherQuery(): String {
        val indexName = "${label}_${property}s".toLowerCase()
        return """CALL db.index.fulltext.createNodeIndex("$indexName", ["$label"], ["$property"])"""
    }
}

@QueryResult
data class Neo4jIndexInfo(
    val label: String,
    val property: String,
    val type: String
)
