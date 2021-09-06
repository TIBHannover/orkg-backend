package eu.tib.orkg.prototype.core.statements

import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ClassIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.LiteralIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ObservatoryIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OrganizationIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ResourceIdConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.convert.Neo4jConversions

@Configuration
class StatementsConfiguration {
    @Bean
    fun neo4jConversions(): Neo4jConversions = Neo4jConversions(
        setOf(
            ClassIdConverter(),
            ContributorIdConverter(),
            LiteralIdConverter(),
            ObservatoryIdConverter(),
            OrganizationIdConverter(),
            PredicateIdConverter(),
            ResourceIdConverter()
        )
    )
}
