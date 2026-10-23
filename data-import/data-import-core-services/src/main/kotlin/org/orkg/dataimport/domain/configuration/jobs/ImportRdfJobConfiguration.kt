package org.orkg.dataimport.domain.configuration.jobs

import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.dataimport.domain.jobs.JobExecutionErrorListener
import org.orkg.dataimport.domain.jobs.JobNames.IMPORT_RDF_FILE
import org.orkg.dataimport.domain.jobs.RdfFileStateUpdater
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.domain.rdf.RdfClassProcessor
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.rdf.RdfPredicateProcessor
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.rdf.RdfResourceProcessor
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.RdfTripleProcessor
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper

@Configuration
class ImportRdfJobConfiguration(
    private val problemResponseFactory: ProblemResponseFactory,
    private val objectMapper: ObjectMapper,
    private val jobRepository: JobRepository,
    private val rdfFileRepository: RdfFileRepository,
    // Classes
    @param:Qualifier("rdfClassReader")
    private val rdfClassReader: RepositoryItemReader<RdfClass>,
    @param:Qualifier("rdfClassProcessor")
    private val rdfClassProcessor: RdfClassProcessor,
    // Predicates
    @param:Qualifier("rdfPredicateReader")
    private val rdfPredicateReader: RepositoryItemReader<RdfPredicate>,
    @param:Qualifier("rdfPredicateProcessor")
    private val rdfPredicateProcessor: RdfPredicateProcessor,
    // Resources
    @param:Qualifier("rdfResourceReader")
    private val rdfResourceReader: RepositoryItemReader<RdfResource>,
    @param:Qualifier("rdfResourceProcessor")
    private val rdfResourceProcessor: RdfResourceProcessor,
    // Triples
    @param:Qualifier("rdfTripleReader")
    private val rdfTripleReader: RepositoryItemReader<RdfTriple>,
    @param:Qualifier("rdfTripleProcessor")
    private val rdfTripleProcessor: RdfTripleProcessor,
) {
    @Bean
    fun importRdfFileJob(): Job =
        JobBuilder(IMPORT_RDF_FILE, jobRepository)
            .start(createClasses())
            .next(createPredicates())
            .next(createResources())
            .next(createTriples())
            .listener(JobExecutionErrorListener(jobRepository, problemResponseFactory, objectMapper))
            .listener(updateRdfFileState())
            .build()

    private fun createClasses(): Step =
        StepBuilder("create-classes", jobRepository)
            .chunk<RdfClass, RdfClass>(100)
            .reader(rdfClassReader)
            .processor(rdfClassProcessor)
            .writer {} // Noop
            .build()

    private fun createPredicates(): Step =
        StepBuilder("create-predicates", jobRepository)
            .chunk<RdfPredicate, RdfPredicate>(100)
            .reader(rdfPredicateReader)
            .processor(rdfPredicateProcessor)
            .writer {} // Noop
            .build()

    private fun createResources(): Step =
        StepBuilder("create-resources", jobRepository)
            .chunk<RdfResource, RdfResource>(100)
            .reader(rdfResourceReader)
            .processor(rdfResourceProcessor)
            .writer {} // Noop
            .build()

    private fun createTriples(): Step =
        StepBuilder("create-triples", jobRepository)
            .chunk<RdfTriple, RdfTriple>(100)
            .reader(rdfTripleReader)
            .processor(rdfTripleProcessor)
            .writer {} // Noop
            .build()

    private fun updateRdfFileState(): RdfFileStateUpdater =
        RdfFileStateUpdater(
            rdfFileRepository = rdfFileRepository,
            startState = State.IMPORT_RUNNING,
            successState = State.IMPORT_DONE,
            stoppedState = State.IMPORT_STOPPED,
            failureState = State.IMPORT_FAILED,
            jobIdSetter = { rdfFile, jobId -> rdfFile.copy(importJobId = jobId) },
        )
}
