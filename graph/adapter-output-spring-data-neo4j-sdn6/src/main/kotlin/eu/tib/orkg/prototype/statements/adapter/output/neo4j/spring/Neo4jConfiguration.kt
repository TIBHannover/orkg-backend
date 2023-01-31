package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.ClassIdConverter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.ContributorIdConverter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.LiteralIdConverter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.OffsetDateTimeConverter
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories(
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
)
@EntityScan(
    "eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal",
)
class Neo4jConfiguration {
    @Bean
    fun neo4jConversions() = Neo4jConversions(
        setOf(
            ClassIdConverter(),
            LiteralIdConverter(),
            ContributorIdConverter(),
            OffsetDateTimeConverter()
        )
    )
}
