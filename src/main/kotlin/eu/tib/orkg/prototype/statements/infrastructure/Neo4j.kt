package eu.tib.orkg.prototype.statements.infrastructure

import org.neo4j.ogm.annotation.NodeEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

@NodeEntity
class Neo4jEntity {
    var id: Long? = null

    var value: String? = null
}

interface Neo4jEntityRepository : Neo4jRepository<Neo4jEntity, Long>
