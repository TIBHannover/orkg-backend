package org.orkg.statistics.adapter.output.neo4j.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories("org.orkg.statistics.adapter.output.neo4j.internal")
@EntityScan("org.orkg.statistics.adapter.output.neo4j.internal")
@ComponentScan(basePackages = ["org.orkg.statistics.adapter.output.neo4j.internal"])
class StatisticsNeo4jConfiguration
