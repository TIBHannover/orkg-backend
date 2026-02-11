package org.orkg.statistics.adapter.output.neo4j.configuration

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories("org.orkg.statistics.adapter.output.neo4j", transactionManagerRef = "neo4jTransactionManager")
@EntityScan("org.orkg.statistics.adapter.output.neo4j")
class StatisticsNeo4jConfiguration
