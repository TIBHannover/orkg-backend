package org.orkg.dataimport.domain.configuration.steps

import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.dataimport.domain.csv.papers.PaperCSVPredicateProcessor
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordProcessor
import org.orkg.dataimport.domain.csv.papers.PaperCSVStatementObjectProcessor
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort

@Configuration
class ImportPaperCSVStepConfiguration {
    @Bean
    @JobScope
    fun paperCSVRecordReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        paperCSVRecordRepository: PaperCSVRecordRepository,
    ): RepositoryItemReader<PaperCSVRecord> =
        RepositoryItemReader<PaperCSVRecord>(paperCSVRecordRepository, mapOf("itemNumber" to Sort.Direction.ASC)).apply {
            setMethodName("findAllByCSVID")
            setArguments(listOf(extractCSVID(jobParameters)))
        }

    @Bean
    fun paperCSVPredicateProcessor(
        unsafePredicateUseCases: UnsafePredicateUseCases,
    ): PaperCSVPredicateProcessor =
        PaperCSVPredicateProcessor(unsafePredicateUseCases)

    @Bean
    fun paperCSVStatementObjectProcessor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ): PaperCSVStatementObjectProcessor =
        PaperCSVStatementObjectProcessor(unsafeResourceUseCases, unsafeLiteralUseCases)

    @Bean
    fun paperCSVRecordProcessor(
        paperUseCases: PaperUseCases,
        contributionUseCases: ContributionUseCases,
    ): PaperCSVRecordProcessor =
        PaperCSVRecordProcessor(paperUseCases, contributionUseCases)

    @Bean
    fun paperCSVRecordImportResultWriter(
        paperCSVRecordImportResultRepository: PaperCSVRecordImportResultRepository,
    ): RepositoryItemWriter<PaperCSVRecordImportResult> =
        RepositoryItemWriter<PaperCSVRecordImportResult>(paperCSVRecordImportResultRepository)
}
