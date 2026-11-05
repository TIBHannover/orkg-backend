package org.orkg.dataimport.domain.configuration

import org.orkg.dataimport.domain.rdf.RdfLabelResolver
import org.orkg.dataimport.domain.rdf.RdfTripleRepositoryLabelResolver
import org.orkg.dataimport.domain.rdf.TypeRegistryRdfLabelResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RdfLabelResolverConfiguration {
    /**
     * @return An ordered list of RdfLabelResolver instances
     */
    @Bean
    fun rdfLabelResolvers(
        typeRegistryRdfLabelResolver: TypeRegistryRdfLabelResolver,
        rdfTripleRepositoryLabelResolver: RdfTripleRepositoryLabelResolver,
    ): List<RdfLabelResolver> = listOf(
        typeRegistryRdfLabelResolver,
        rdfTripleRepositoryLabelResolver,
    )
}
