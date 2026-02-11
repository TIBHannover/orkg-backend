package org.orkg.curation.adapter.output.neo4j.configuration

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories("org.orkg.curation.adapter.output.neo4j.internal", transactionManagerRef = "neo4jTransactionManager")
@EntityScan(basePackages = ["org.orkg.curation.adapter.output.neo4j.internal"])
class CurationNeo4jConfiguration
