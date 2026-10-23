package org.orkg.dataimport.domain.configuration.jobs

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Statement
import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.dataimport.domain.jobs.JobExecutionErrorListener
import org.orkg.dataimport.domain.jobs.JobNames.VALIDATE_RDF_FILE
import org.orkg.dataimport.domain.jobs.RdfFileStateUpdater
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.domain.rdf.RdfClassParser
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.rdf.RdfPredicateParser
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.rdf.RdfResourceParser
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.RdfTripleParser
import org.orkg.dataimport.domain.rdf.StatementReader
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper

@Configuration
class ValidateRdfJobConfiguration(
    private val problemResponseFactory: ProblemResponseFactory,
    private val objectMapper: ObjectMapper,
    private val jobRepository: JobRepository,
    private val rdfFileRepository: RdfFileRepository,
    // Triples
    @param:Qualifier("rdfStatementReader")
    private val rdfStatementReader: StatementReader,
    @param:Qualifier("rdfTripleParser")
    private val rdfTripleParser: RdfTripleParser,
    @param:Qualifier("rdfTripleWriter")
    private val rdfTripleWriter: RepositoryItemWriter<RdfTriple>,
    // Classes
    @param:Qualifier("rdfClassIriReader")
    private val rdfClassIriReader: RepositoryItemReader<IRI>,
    @param:Qualifier("rdfClassParser")
    private val rdfClassParser: RdfClassParser,
    @param:Qualifier("rdfClassWriter")
    private val rdfClassWriter: RepositoryItemWriter<RdfClass>,
    // Predicates
    @param:Qualifier("rdfPredicateIriReader")
    private val rdfPredicateIriReader: RepositoryItemReader<IRI>,
    @param:Qualifier("rdfPredicateParser")
    private val rdfPredicateParser: RdfPredicateParser,
    @param:Qualifier("rdfPredicateWriter")
    private val rdfPredicateWriter: RepositoryItemWriter<RdfPredicate>,
    // Resources
    @param:Qualifier("rdfResourceIriReader")
    private val rdfResourceIriReader: RepositoryItemReader<Resource>,
    @param:Qualifier("rdfResourceParser")
    private val rdfResourceParser: RdfResourceParser,
    @param:Qualifier("rdfResourceWriter")
    private val rdfResourceWriter: RepositoryItemWriter<RdfResource>,
) {
    @Bean
    fun validateRdfFileJob(): Job =
        JobBuilder(VALIDATE_RDF_FILE, jobRepository)
            .start(parseTriples())
            .next(parseClasses())
            .next(parsePredicates())
            .next(parseResources())
            .listener(JobExecutionErrorListener(jobRepository, problemResponseFactory, objectMapper))
            .listener(updateRdfFileState())
            .build()

    private fun parseTriples(): Step =
        StepBuilder("parse-triples", jobRepository)
            .chunk<Statement, RdfTriple>(100)
            .reader(rdfStatementReader)
            .processor(rdfTripleParser)
            .writer(rdfTripleWriter)
            .build()

    private fun parseClasses(): Step =
        StepBuilder("parse-classes", jobRepository)
            .chunk<IRI, RdfClass>(100)
            .reader(rdfClassIriReader)
            .processor(rdfClassParser)
            .writer(rdfClassWriter)
            .build()

    private fun parsePredicates(): Step =
        StepBuilder("parse-predicates", jobRepository)
            .chunk<IRI, RdfPredicate>(100)
            .reader(rdfPredicateIriReader)
            .processor(rdfPredicateParser)
            .writer(rdfPredicateWriter)
            .build()

    private fun parseResources(): Step =
        StepBuilder("parse-resources", jobRepository)
            .chunk<Resource, RdfResource>(100)
            .reader(rdfResourceIriReader)
            .processor(rdfResourceParser)
            .writer(rdfResourceWriter)
            .build()

    private fun updateRdfFileState(): RdfFileStateUpdater =
        RdfFileStateUpdater(
            rdfFileRepository = rdfFileRepository,
            startState = State.VALIDATION_RUNNING,
            successState = State.VALIDATION_DONE,
            stoppedState = State.VALIDATION_STOPPED,
            failureState = State.VALIDATION_FAILED,
            jobIdSetter = { rdfFile, jobId -> rdfFile.copy(validationJobId = jobId) },
        )
}
