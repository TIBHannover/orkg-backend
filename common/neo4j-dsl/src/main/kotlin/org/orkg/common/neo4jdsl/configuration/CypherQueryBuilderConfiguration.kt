package org.orkg.common.neo4jdsl.configuration

import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.Neo4jClient
import org.neo4j.cypherdsl.core.renderer.Configuration as CypherConfiguration

@Configuration
class CypherQueryBuilderConfiguration {
    @Bean
    fun cypherQueryBuilderFactory(
        configuration: CypherConfiguration,
        neo4jClient: Neo4jClient,
    ): CypherQueryBuilderFactory =
        CypherQueryBuilderFactory(configuration, neo4jClient)
}
