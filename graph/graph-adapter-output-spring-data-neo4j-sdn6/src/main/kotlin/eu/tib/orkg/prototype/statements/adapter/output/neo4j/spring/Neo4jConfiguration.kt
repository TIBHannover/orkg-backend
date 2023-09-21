package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.AttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.neo4j.driver.Driver
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories(
    "eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j",
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
)
@EntityScan(
    "eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j",
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
)
class Neo4jConfiguration {
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
        driver: Driver?,
        databaseSelectionProvider: DatabaseSelectionProvider?
    ): Neo4jClient {
        return Neo4jClient.create(driver, databaseSelectionProvider)
    }
}
