package org.orkg.dataimport.domain.configuration.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSVHeaderParser
import org.orkg.dataimport.domain.csv.CSVHeaderProcessor
import org.orkg.dataimport.domain.csv.CSVRecordReader
import org.orkg.dataimport.domain.csv.PositionAwareCSVRecord
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.domain.csv.TypedCSVRecordDeleter
import org.orkg.dataimport.domain.csv.TypedCSVRecordParser
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordParser
import org.orkg.dataimport.domain.jobs.CSVStateUpdater
import org.orkg.dataimport.domain.jobs.ItemErrorListener
import org.orkg.dataimport.domain.jobs.JobExecutionErrorListener
import org.orkg.dataimport.domain.jobs.JobNames.VALIDATE_PAPER_CSV
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ValidatePaperCSVJobConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val transactionManager: PlatformTransactionManager,
    private val problemResponseFactory: ProblemResponseFactory,
    private val objectMapper: ObjectMapper,
    private val jobRepository: JobRepository,
    private val csvRepository: CSVRepository,
    @param:Qualifier("csvHeaderParser")
    private val csvHeaderParser: CSVHeaderParser,
    @param:Qualifier("csvRecordReader")
    private val csvRecordReader: CSVRecordReader,
    @param:Qualifier("typedCSVRecordParser")
    private val typedCSVRecordParser: TypedCSVRecordParser,
    @param:Qualifier("typedCSVRecordWriter")
    private val typedCSVRecordWriter: RepositoryItemWriter<TypedCSVRecord>,
    @param:Qualifier("csvHeaderProcessor")
    private val csvHeaderProcessor: CSVHeaderProcessor,
    @param:Qualifier("typedCSVRecordReader")
    private val typedCSVRecordReader: RepositoryItemReader<TypedCSVRecord>,
    @param:Qualifier("paperCSVRecordParser")
    private val paperCSVRecordParser: PaperCSVRecordParser,
    @param:Qualifier("paperCSVRecordWriter")
    private val paperCSVRecordWriter: RepositoryItemWriter<PaperCSVRecord>,
    @param:Qualifier("typedCSVRecordDeleter")
    private val typedCSVRecordDeleter: TypedCSVRecordDeleter,
) {
    @Bean
    fun validatePaperCSVJob(): Job =
        JobBuilder(VALIDATE_PAPER_CSV, jobRepository)
            .start(parseHeader())
            .next(parseTypedRecords())
            .next(validateHeader())
            .next(parsePapers())
            .next(deleteIntermediateResults())
            .listener(JobExecutionErrorListener(jobRepository, problemResponseFactory, objectMapper))
            .listener(updateCSVState())
            .build()

    private fun parseHeader(): Step =
        StepBuilder("parse-header", jobRepository)
            .tasklet(csvHeaderParser, transactionManager)
            .build()

    private fun parseTypedRecords(): Step =
        StepBuilder("parse-typed-records", jobRepository)
            .chunk<PositionAwareCSVRecord, TypedCSVRecord>(1, transactionManager)
            .reader(csvRecordReader)
            .processor(typedCSVRecordParser)
            .writer(typedCSVRecordWriter)
            .faultTolerant()
            .skipPolicy(AlwaysSkipItemSkipPolicy())
            .listener(ItemErrorListener<PositionAwareCSVRecord, TypedCSVRecord>() as StepExecutionListener)
            .build()

    private fun validateHeader(): Step =
        StepBuilder("validate-header", jobRepository)
            .tasklet(csvHeaderProcessor, transactionManager)
            .build()

    private fun parsePapers(): Step =
        StepBuilder("parse-papers", jobRepository)
            .chunk<TypedCSVRecord, PaperCSVRecord>(1, transactionManager)
            .reader(typedCSVRecordReader)
            .processor(paperCSVRecordParser)
            .writer(paperCSVRecordWriter)
            .faultTolerant()
            .skipPolicy(AlwaysSkipItemSkipPolicy())
            .listener(ItemErrorListener<TypedCSVRecord, PaperCSVRecord>() as StepExecutionListener)
            .build()

    private fun deleteIntermediateResults(): Step =
        StepBuilder("delete-intermediate-results", jobRepository)
            .tasklet(typedCSVRecordDeleter, transactionManager)
            .build()

    private fun updateCSVState(): CSVStateUpdater =
        CSVStateUpdater(
            csvRepository = csvRepository,
            startState = State.VALIDATION_RUNNING,
            successState = State.VALIDATION_DONE,
            stoppedState = State.VALIDATION_STOPPED,
            failureState = State.VALIDATION_FAILED,
            jobIdSetter = { csv, jobId -> csv.copy(validationJobId = jobId) }
        )
}
