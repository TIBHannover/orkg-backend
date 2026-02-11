package org.orkg.dataimport.domain.configuration.steps

import org.orkg.dataimport.domain.csv.TypedCSVRecordDeleter
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaperCSVStepConfiguration {
    @Bean
    fun paperCSVRecordWriter(
        paperCSVRecordRepository: PaperCSVRecordRepository,
    ): RepositoryItemWriter<PaperCSVRecord> =
        RepositoryItemWriter<PaperCSVRecord>(paperCSVRecordRepository)

    @Bean
    @JobScope
    fun typedCSVRecordDeleter(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        typedCSVRecordRepository: TypedCSVRecordRepository,
    ): TypedCSVRecordDeleter =
        TypedCSVRecordDeleter(
            csvId = extractCSVID(jobParameters),
            typedCSVRecordRepository = typedCSVRecordRepository,
        )
}
