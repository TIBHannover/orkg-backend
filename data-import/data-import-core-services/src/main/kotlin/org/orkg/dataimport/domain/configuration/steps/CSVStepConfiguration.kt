package org.orkg.dataimport.domain.configuration.steps

import org.orkg.dataimport.domain.csv.CSVHeaderParser
import org.orkg.dataimport.domain.csv.CSVHeaderProcessor
import org.orkg.dataimport.domain.csv.CSVRecordReader
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.domain.csv.TypedCSVRecordParser
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.output.CSVRepository
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.orkg.graph.output.PredicateRepository
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort

@Configuration
class CSVStepConfiguration {
    @Bean
    @JobScope
    fun csvHeaderParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        csvRepository: CSVRepository,
    ): CSVHeaderParser =
        CSVHeaderParser(
            csvRepository = csvRepository,
            csvId = extractCSVID(jobParameters),
        )

    @Bean
    @JobScope
    fun csvRecordReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        csvRepository: CSVRepository,
    ): CSVRecordReader =
        CSVRecordReader(
            csvId = extractCSVID(jobParameters),
            csvRepository = csvRepository
        )

    @Bean
    @JobScope
    fun typedCSVRecordParser(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
    ): TypedCSVRecordParser =
        TypedCSVRecordParser(extractCSVID(jobParameters))

    @Bean
    fun typedCSVRecordWriter(
        typedCSVRecordRepository: TypedCSVRecordRepository,
    ): RepositoryItemWriter<TypedCSVRecord> =
        RepositoryItemWriter<TypedCSVRecord>().apply {
            setRepository(typedCSVRecordRepository)
        }

    @Bean
    fun csvHeaderProcessor(
        predicateRepository: PredicateRepository,
    ): CSVHeaderProcessor =
        CSVHeaderProcessor(predicateRepository)

    @Bean
    @JobScope
    fun typedCSVRecordReader(
        @Value("#{jobParameters}")
        jobParameters: Map<String, Any>,
        typedCSVRecordRepository: TypedCSVRecordRepository,
    ): RepositoryItemReader<TypedCSVRecord> =
        RepositoryItemReader<TypedCSVRecord>().apply {
            setRepository(typedCSVRecordRepository)
            setMethodName("findAllByCSVID")
            setArguments(listOf(extractCSVID(jobParameters)))
            setSort(mapOf("itemNumber" to Sort.Direction.ASC))
        }
}
