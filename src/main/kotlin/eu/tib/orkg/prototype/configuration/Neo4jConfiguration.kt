package eu.tib.orkg.prototype.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories("eu.tib.orkg.prototype.statements.domain.model.neo4j")
@EntityScan("eu.tib.orkg.prototype.statements.domain.model.neo4j")
class Neo4jConfiguration