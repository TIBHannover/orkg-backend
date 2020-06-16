package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jIndexRepository : Neo4jRepository<Any, Long> {

    // TODO: in Neo4j 3.5 you can't set the name, you can in Neo4j 4.0
    @Query("""CREATE CONSTRAINT ON (n:{0}) ASSERT n.{1} IS UNIQUE;""")
    fun createUniqueConstraint(type: String, property: String)

    // TODO: in Neo4j 3.5 you can't set the name and the create index statement changes in Neo4j 4.0
    @Query("""CREATE INDEX ON :{0}({1});""")
    fun createPropertyIndex(type: String, property: String)

    @Query("""CALL db.indexes() YIELD tokenNames, properties, type RETURN tokenNames[0] as label, properties[0] as property, type""")
    fun getExistingIndicesAndConstraints(): Triple<List<String>, List<String>, String>
}
