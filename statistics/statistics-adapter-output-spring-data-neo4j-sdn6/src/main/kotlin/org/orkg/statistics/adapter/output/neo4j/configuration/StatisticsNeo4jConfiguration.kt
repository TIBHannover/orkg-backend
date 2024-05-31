package org.orkg.statistics.adapter.output.neo4j.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration

@Configuration
@EntityScan("org.orkg.statistics.adapter.output.neo4j.internal")
class StatisticsNeo4jConfiguration
