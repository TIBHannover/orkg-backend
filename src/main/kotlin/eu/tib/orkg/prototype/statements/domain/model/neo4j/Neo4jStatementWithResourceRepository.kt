package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.*

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long>
