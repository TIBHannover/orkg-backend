package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.AttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.*
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories("eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal")
@EntityScan("eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal")
class Neo4jConfiguration {
    @Bean
    fun neo4jConversions() = Neo4jConversions(
        setOf(
            AttributeConverter(ClassId::class, ::ClassId),
            AttributeConverter(LiteralId::class, ::LiteralId),
            AttributeConverter(ContributorId::class, ::ContributorId),
            AttributeConverter(PredicateId::class, ::PredicateId),
            AttributeConverter(ResourceId::class, ::ResourceId),
            AttributeConverter(OrganizationId::class, ::OrganizationId),
            AttributeConverter(ObservatoryId::class, ::ObservatoryId),
            AttributeConverter(StatementId::class, ::StatementId),
            AttributeConverter(
                kClass = OffsetDateTime::class,
                deserializer = { OffsetDateTime.parse(it, ISO_OFFSET_DATE_TIME) },
                serializer = { (it as OffsetDateTime).format(ISO_OFFSET_DATE_TIME) }
            )
        )
    )
}
