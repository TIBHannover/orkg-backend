package eu.tib.orkg.prototype.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories(
    "eu.tib.orkg.prototype.statements.domain.model.neo4j",
    "eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j",
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
    "eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j"
)
@EntityScan(
    "eu.tib.orkg.prototype.statements.domain.model.neo4j",
    "eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j",
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
    "eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j",
    "eu.tib.orkg.prototype.statements.spi" // because of mapping types, can be removed after upgrade to SDN 6
)
class Neo4jConfiguration
