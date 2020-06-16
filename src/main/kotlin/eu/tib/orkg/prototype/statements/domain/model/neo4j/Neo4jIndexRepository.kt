package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.neo4j.ogm.session.Session
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jIndexRepository : Neo4jRepository<Neo4jResource, Long>, IndexRepository {

    @Query("""CALL db.indexes() YIELD tokenNames, properties, type RETURN tokenNames[0] as label, properties[0] as property, type as type""")
    fun getExistingIndicesAndConstraints(): Iterable<Neo4jIndexInfo>
}

interface IndexRepository {

    fun createUniqueConstraint(type: String, property: String)

    fun createPropertyIndex(type: String, property: String)
}

class IndexRepositoryImpl(private val ogmSession: Session) : IndexRepository {

    // TODO: in Neo4j 3.5 you can't set the name and the create index statement changes in Neo4j 4.0
    override fun createPropertyIndex(type: String, property: String) {
        val query = """CREATE INDEX ON :$type($property);"""
        ogmSession.query(query, emptyMap<String, Any>())
    }

    // TODO: in Neo4j 3.5 you can't set the name, you can in Neo4j 4.0
    override fun createUniqueConstraint(type: String, property: String) {
        val query = """CREATE CONSTRAINT ON (n:$type) ASSERT n.$property IS UNIQUE;"""
        ogmSession.query(query, emptyMap<String, Any>())
    }
}

@QueryResult
data class Neo4jIndexInfo(
    val label: String,
    val property: String,
    val type: String
)
