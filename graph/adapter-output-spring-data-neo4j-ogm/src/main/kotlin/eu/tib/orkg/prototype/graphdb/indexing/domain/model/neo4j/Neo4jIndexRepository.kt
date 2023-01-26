package eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j

import eu.tib.orkg.prototype.graphdb.indexing.domain.model.Neo4jIndex
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
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

@QueryResult
data class Neo4jIndexInfo(
    val label: String,
    val property: String,
    val type: String
)
