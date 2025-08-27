package org.orkg.dataimport.domain.configuration.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.papers.PaperCSVPredicateProcessor
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordProcessor
import org.orkg.dataimport.domain.csv.papers.PaperCSVStatementObjectProcessor
import org.orkg.dataimport.domain.jobs.CSVStateUpdater
import org.orkg.dataimport.domain.jobs.JobExecutionErrorListener
import org.orkg.dataimport.domain.jobs.JobNames.IMPORT_PAPER_CSV
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ImportPaperCSVJobConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val transactionManager: PlatformTransactionManager,
    private val problemResponseFactory: ProblemResponseFactory,
    private val objectMapper: ObjectMapper,
    private val jobRepository: JobRepository,
    private val csvRepository: CSVRepository,
    @param:Qualifier("paperCSVRecordWriter")
    private val paperCSVRecordWriter: RepositoryItemWriter<PaperCSVRecord>,
    @param:Qualifier("paperCSVRecordReader")
    private val paperCSVRecordReader: RepositoryItemReader<PaperCSVRecord>,
    @param:Qualifier("paperCSVPredicateProcessor")
    private val paperCSVPredicateProcessor: PaperCSVPredicateProcessor,
    @param:Qualifier("paperCSVStatementObjectProcessor")
    private val paperCSVStatementObjectProcessor: PaperCSVStatementObjectProcessor,
    @param:Qualifier("paperCSVRecordProcessor")
    private val paperCSVRecordProcessor: PaperCSVRecordProcessor,
    @param:Qualifier("paperCSVRecordImportResultWriter")
    private val paperCSVRecordImportResultWriter: RepositoryItemWriter<PaperCSVRecordImportResult>,
) {
    @Bean
    fun importPaperCSVJob(): Job =
        JobBuilder(IMPORT_PAPER_CSV, jobRepository)
            .start(createPredicates())
            .next(createStatementObjects())
            .next(createPapers())
            .listener(JobExecutionErrorListener(jobRepository, problemResponseFactory, objectMapper))
            .listener(updateCSVState())
            .build()

    private fun createPredicates(): Step =
        StepBuilder("create-predicates", jobRepository)
            .chunk<PaperCSVRecord, PaperCSVRecord>(1, transactionManager)
            .reader(paperCSVRecordReader)
            .processor(paperCSVPredicateProcessor)
            .writer(paperCSVRecordWriter)
            .build()

    private fun createStatementObjects(): Step =
        StepBuilder("create-statement-objects", jobRepository)
            .chunk<PaperCSVRecord, PaperCSVRecord>(1, transactionManager)
            .reader(paperCSVRecordReader)
            .processor(paperCSVStatementObjectProcessor)
            .writer(paperCSVRecordWriter)
            .build()

    private fun createPapers(): Step =
        StepBuilder("create-papers", jobRepository)
            .chunk<PaperCSVRecord, PaperCSVRecordImportResult>(1, transactionManager)
            .reader(paperCSVRecordReader)
            .processor(paperCSVRecordProcessor)
            .writer(paperCSVRecordImportResultWriter)
            .build()

    private fun updateCSVState(): CSVStateUpdater =
        CSVStateUpdater(
            csvRepository = csvRepository,
            startState = State.IMPORT_RUNNING,
            successState = State.IMPORT_DONE,
            stoppedState = State.IMPORT_STOPPED,
            failureState = State.IMPORT_FAILED,
            jobIdSetter = { csv, jobId -> csv.copy(importJobId = jobId) }
        )
}
