package org.orkg.graph.adapter.output.neo4j.configuration

import org.neo4j.cypherdsl.core.renderer.Dialect
import org.neo4j.driver.Driver
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.AttributeConverter
import org.orkg.graph.domain.StatementId
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.neo4j.cypherdsl.core.renderer.Configuration as CypherConfiguration

@Configuration
@EnableNeo4jRepositories("org.orkg.graph.adapter.output.neo4j.internal", transactionManagerRef = "neo4jTransactionManager")
@EntityScan("org.orkg.graph.adapter.output.neo4j.internal")
@ComponentScan(basePackages = ["org.orkg.graph.adapter.output.neo4j.internal"])
class GraphNeo4jConfiguration {
    @Bean
    fun neo4jConversions() = Neo4jConversions(
        setOf(
            AttributeConverter(ContributorId::class, ::ContributorId),
            AttributeConverter(OrganizationId::class, ::OrganizationId),
            AttributeConverter(ObservatoryId::class, ::ObservatoryId),
            AttributeConverter(StatementId::class, ::StatementId),
            AttributeConverter(ThingId::class, ::ThingId),
            AttributeConverter(
                kClass = OffsetDateTime::class,
                deserializer = { OffsetDateTime.parse(it, ISO_OFFSET_DATE_TIME) },
                serializer = { (it as OffsetDateTime).format(ISO_OFFSET_DATE_TIME) }
            )
        )
    )

    @Bean
    fun neo4jClient(
        driver: Driver,
        databaseNameProvider: DatabaseSelectionProvider,
        neo4jConversions: Neo4jConversions,
    ): Neo4jClient =
        Neo4jClient
            .with(driver)
            .withDatabaseSelectionProvider(databaseNameProvider)
            .withNeo4jConversions(neo4jConversions)
            .build()

    @Bean
    fun cypherDslConfiguration(): CypherConfiguration =
        CypherConfiguration.newConfig()
            .withDialect(Dialect.NEO4J_5_23)
            .build()
}
