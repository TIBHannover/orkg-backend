package org.orkg.dataimport.domain.configuration.steps

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.ValueFactory
import org.orkg.dataimport.domain.extractRdfFileId
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.domain.rdf.RdfClassParser
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.rdf.RdfPredicateParser
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.rdf.RdfResourceParser
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.RdfTripleParser
import org.orkg.dataimport.domain.rdf.StatementReader
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort

@Configuration
class ValidateRdfFileStepConfiguration {
    @Bean
    @JobScope
    fun rdfStatementReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfFileRepository: RdfFileRepository,
        valueFactory: ValueFactory,
    ): StatementReader =
        StatementReader(
            rdfFileId = extractRdfFileId(jobParameters),
            rdfFileRepository = rdfFileRepository,
            valueFactory = valueFactory,
        )

    @Bean
    @JobScope
    fun rdfTripleParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
    ): RdfTripleParser =
        RdfTripleParser(
            rdfFileId = extractRdfFileId(jobParameters),
        )

    @Bean
    fun rdfTripleWriter(
        rdfTripleRepository: RdfTripleRepository,
    ): RepositoryItemWriter<RdfTriple> =
        RepositoryItemWriter(rdfTripleRepository)

    @Bean
    @JobScope
    fun rdfClassIriReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RepositoryItemReader<IRI> =
        RepositoryItemReader<IRI>(rdfTripleRepository, mapOf("object" to Sort.Direction.ASC)).apply {
            setMethodName("findAllDistinctClassIrisByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfClassParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RdfClassParser =
        RdfClassParser(
            rdfFileId = extractRdfFileId(jobParameters),
            rdfTripleRepository = rdfTripleRepository,
        )

    @Bean
    fun rdfClassWriter(
        rdfClassRepository: RdfClassRepository,
    ): RepositoryItemWriter<RdfClass> =
        RepositoryItemWriter(rdfClassRepository)

    @Bean
    @JobScope
    fun rdfPredicateIriReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RepositoryItemReader<IRI> =
        RepositoryItemReader<IRI>(rdfTripleRepository, mapOf("predicate" to Sort.Direction.ASC)).apply {
            setMethodName("findAllDistinctPredicatesByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfPredicateParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RdfPredicateParser =
        RdfPredicateParser(
            rdfFileId = extractRdfFileId(jobParameters),
            rdfTripleRepository = rdfTripleRepository,
        )

    @Bean
    fun rdfPredicateWriter(
        rdfPredicateRepository: RdfPredicateRepository,
    ): RepositoryItemWriter<RdfPredicate> =
        RepositoryItemWriter(rdfPredicateRepository)

    @Bean
    @JobScope
    fun rdfResourceIriReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RepositoryItemReader<Resource> =
        RepositoryItemReader<Resource>(rdfTripleRepository, mapOf("subject" to Sort.Direction.ASC)).apply {
            setMethodName("findAllDistinctIndividualsByRdfFileId")
            setArguments(listOf(extractRdfFileId(jobParameters)))
        }

    @Bean
    @JobScope
    fun rdfResourceParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        rdfTripleRepository: RdfTripleRepository,
    ): RdfResourceParser =
        RdfResourceParser(
            rdfFileId = extractRdfFileId(jobParameters),
            rdfTripleRepository = rdfTripleRepository,
        )

    @Bean
    fun rdfResourceWriter(
        rdfResourceRepository: RdfResourceRepository,
    ): RepositoryItemWriter<RdfResource> =
        RepositoryItemWriter(rdfResourceRepository)
}
