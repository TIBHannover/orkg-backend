package org.orkg.dataimport.domain.configuration.steps

import org.orkg.dataimport.domain.extractContributorId
import org.orkg.dataimport.domain.extractExtractionMethod
import org.orkg.dataimport.domain.extractModifiable
import org.orkg.dataimport.domain.extractRdfFileId
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.domain.rdf.RdfClassProcessor
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.rdf.RdfPredicateProcessor
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.rdf.RdfResourceProcessor
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.RdfTripleProcessor
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort

@Configuration
class ImportRdfFileStepConfiguration {
    @Bean
    @JobScope
    fun rdfClassReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfClassRepository: RdfClassRepository,
    ): RepositoryItemReader<RdfClass> =
        RepositoryItemReader<RdfClass>(rdfClassRepository, mapOf("iri" to Sort.Direction.ASC)).apply {
            setMethodName("findAllByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfClassProcessor(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        thingRepository: ThingRepository,
        unsafeClassUseCases: UnsafeClassUseCases,
    ): RdfClassProcessor =
        RdfClassProcessor(
            contributorId = extractContributorId(jobParameters),
            extractionMethod = extractExtractionMethod(jobParameters),
            modifiable = extractModifiable(jobParameters),
            thingRepository = thingRepository,
            unsafeClassUseCases = unsafeClassUseCases,
        )

    @Bean
    @JobScope
    fun rdfPredicateReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfPredicateRepository: RdfPredicateRepository,
    ): RepositoryItemReader<RdfPredicate> =
        RepositoryItemReader<RdfPredicate>(rdfPredicateRepository, mapOf("iri" to Sort.Direction.ASC)).apply {
            setMethodName("findAllByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfPredicateProcessor(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        thingRepository: ThingRepository,
        unsafePredicateUseCases: UnsafePredicateUseCases,
    ): RdfPredicateProcessor =
        RdfPredicateProcessor(
            contributorId = extractContributorId(jobParameters),
            extractionMethod = extractExtractionMethod(jobParameters),
            modifiable = extractModifiable(jobParameters),
            thingRepository = thingRepository,
            unsafePredicateUseCases = unsafePredicateUseCases,
        )

    @Bean
    @JobScope
    fun rdfResourceReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfResourceRepository: RdfResourceRepository,
    ): RepositoryItemReader<RdfResource> =
        RepositoryItemReader<RdfResource>(rdfResourceRepository, mapOf("iri" to Sort.Direction.ASC)).apply {
            setMethodName("findAllByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfResourceProcessor(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        thingRepository: ThingRepository,
        unsafeResourceUseCases: UnsafeResourceUseCases,
    ): RdfResourceProcessor =
        RdfResourceProcessor(
            contributorId = extractContributorId(jobParameters),
            extractionMethod = extractExtractionMethod(jobParameters),
            modifiable = extractModifiable(jobParameters),
            thingRepository = thingRepository,
            unsafeResourceUseCases = unsafeResourceUseCases,
        )

    @Bean
    @JobScope
    fun rdfTripleReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RepositoryItemReader<RdfTriple> =
        RepositoryItemReader<RdfTriple>(rdfTripleRepository, mapOf("id" to Sort.Direction.ASC)).apply {
            setMethodName("findAllByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfTripleProcessor(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        thingRepository: ThingRepository,
        statementUseCases: StatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ): RdfTripleProcessor =
        RdfTripleProcessor(
            contributorId = extractContributorId(jobParameters),
            extractionMethod = extractExtractionMethod(jobParameters),
            modifiable = extractModifiable(jobParameters),
            thingRepository = thingRepository,
            statementUseCases = statementUseCases,
            unsafeLiteralUseCases = unsafeLiteralUseCases,
        )
}
