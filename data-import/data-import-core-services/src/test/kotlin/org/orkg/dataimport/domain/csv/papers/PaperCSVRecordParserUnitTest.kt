package org.orkg.dataimport.domain.csv.papers

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.common.DOI
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.output.DoiService
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_HEADER_TO_PREDICATE_FIELD
import org.orkg.dataimport.domain.CSV_TYPE_FIELD
import org.orkg.dataimport.domain.PaperCSVMissingTitle
import org.orkg.dataimport.domain.PaperCSVResourceNotFound
import org.orkg.dataimport.domain.PaperCSVThingNotFound
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVHeaders
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.dataimport.domain.testing.fixtures.createTypedCSVRecord
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import tools.jackson.databind.ObjectMapper
import java.util.Optional
import java.util.UUID

internal class PaperCSVRecordParserUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val doiService: DoiService = mockk()

    private val paperCSVRecordParser = PaperCSVRecordParser(thingRepository, resourceRepository, doiService)

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, it parses all properties correctly`() {
        val record = createTypedCSVRecord()
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders()
        val headerToPredicate: Map<CSVHeader, Either<ThingId, String>> = mapOf(
            headers[10] to Either.right("category"),
            headers[11] to Either.left(ThingId("P2")),
            headers[12] to Either.right("result"),
            headers[13] to Either.left(ThingId("numericValue")),
            headers[14] to Either.left(Predicates.description),
        )
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, headerToPredicate)
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val id = UUID.fromString("a0e2f360-64a9-4e60-919f-76f906b987f3")
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        mockkStatic(UUID::class) {
            every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
            every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
            every { UUID.randomUUID() } returns id

            paperCSVRecordParser.process(record).asClue {
                it.shouldNotBeNull()
                it shouldBe PaperCSVRecord(
                    id = id,
                    csvId = record.csvId,
                    itemNumber = record.itemNumber,
                    lineNumber = record.lineNumber,
                    title = "Dummy Paper Title",
                    authors = listOf(
                        Author("Josiah Stinkney Carberry"),
                        Author("Author 2"),
                    ),
                    publicationMonth = 4,
                    publicationYear = 2023,
                    publishedIn = "Fancy Conference",
                    url = ParsedIRI("https://example.org"),
                    doi = doi.value,
                    researchFieldId = researchFieldId,
                    extractionMethod = ExtractionMethod.MANUAL,
                    statements = setOf(
                        ContributionStatement(Either.right("category"), record.values[10]),
                        ContributionStatement(Either.left(ThingId("P2")), record.values[11]),
                        ContributionStatement(Either.right("result"), record.values[12]),
                        ContributionStatement(Either.left(ThingId("numericValue")), record.values[13]),
                        ContributionStatement(Either.left(Predicates.description), record.values[14]),
                        ContributionStatement(Either.left(Predicates.hasResearchProblem), record.values[8])
                    )
                )
            }

            verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
            verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
            verify(exactly = 1) { UUID.randomUUID() }
        }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and doi can be resolved, it overrides the provided metadata`() {
        val record = createTypedCSVRecord()
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders()
        val headerToPredicate: Map<CSVHeader, Either<ThingId, String>> = mapOf(
            headers[10] to Either.right("category"),
            headers[11] to Either.left(ThingId("P2")),
            headers[12] to Either.right("result"),
            headers[13] to Either.left(ThingId("numericValue")),
            headers[14] to Either.left(Predicates.description),
        )
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, headerToPredicate)
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val id = UUID.fromString("a0e2f360-64a9-4e60-919f-76f906b987f3")
        val doi = DOI.of("10.1000/182")
        val doiResponse = ObjectMapper().readTree(responseJson("datacite/doiLookupSuccess"))

        paperCSVRecordParser.beforeStep(stepExecution)

        mockkStatic(UUID::class) {
            every { doiService.findMetadataByDoi(doi) } returns Optional.of(doiResponse)
            every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
            every { UUID.randomUUID() } returns id

            paperCSVRecordParser.process(record).asClue {
                it.shouldNotBeNull()
                it shouldBe PaperCSVRecord(
                    id = id,
                    csvId = record.csvId,
                    itemNumber = record.itemNumber,
                    lineNumber = record.lineNumber,
                    title = "ORKG: Facilitating the Transfer of Research Results with the Open Research Knowledge Graph",
                    authors = listOf(
                        Author("Sören Auer", identifiers = mapOf("orcid" to listOf("0000-0002-0698-2864"))),
                        Author("Markus Stocker", identifiers = mapOf("orcid" to listOf("0000-0001-5492-3212"))),
                        Author("Lars Vogt", identifiers = mapOf("orcid" to listOf("0000-0002-8280-0487"))),
                        Author("Grischa Fraumann", identifiers = mapOf("orcid" to listOf("0000-0003-0099-6509"))),
                        Author("Alexandra Garatzogianni"),
                    ),
                    publicationMonth = 5,
                    publicationYear = 2021,
                    publishedIn = "Research Ideas and Outcomes",
                    url = ParsedIRI("http://dx.doi.org/10.3897/rio.7.e68513"),
                    doi = doi.value,
                    researchFieldId = researchFieldId,
                    extractionMethod = ExtractionMethod.MANUAL,
                    statements = setOf(
                        ContributionStatement(Either.right("category"), record.values[10]),
                        ContributionStatement(Either.left(ThingId("P2")), record.values[11]),
                        ContributionStatement(Either.right("result"), record.values[12]),
                        ContributionStatement(Either.left(ThingId("numericValue")), record.values[13]),
                        ContributionStatement(Either.left(Predicates.description), record.values[14]),
                        ContributionStatement(Either.left(Predicates.hasResearchProblem), record.values[8])
                    )
                )
            }

            verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
            verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
            verify(exactly = 1) { UUID.randomUUID() }
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["", "  "])
    fun `Given a typed csv record, when parsing a paper csv record, and paper title was not provided, it throws an exception`(value: String?) {
        val record = createTypedCSVRecord().let {
            val values = it.values.dropLast(5).toMutableList()
            values[0] = TypedValue(namespace = null, value = value, type = Classes.string)
            it.copy(values = values)
        }
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders().dropLast(5)
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()

        shouldThrow<PaperCSVMissingTitle> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and research field does not exist, it throws an exception`() {
        val record = createTypedCSVRecord()
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, createPaperCSVHeaders())
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.empty()

        shouldThrow<PaperCSVResourceNotFound> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and research field resouce is not a research field instance, it throws an exception`() {
        val record = createTypedCSVRecord()
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, createPaperCSVHeaders())
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)

        shouldThrow<PaperCSVResourceNotFound> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and research problem does not exist, it throws an exception`() {
        val researchProblemId = ThingId("R123")
        val record = createTypedCSVRecord().let {
            val values = it.values.dropLast(5).toMutableList()
            values[8] = TypedValue(namespace = "orkg", value = researchProblemId.value, type = Classes.resource)
            it.copy(values = values)
        }
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders().dropLast(5)
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
        every { resourceRepository.findById(researchProblemId) } returns Optional.empty()

        shouldThrow<PaperCSVResourceNotFound> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
        verify(exactly = 1) { resourceRepository.findById(researchProblemId) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and research problem is not a research problem instance, it throws an exception`() {
        val researchProblemId = ThingId("R123")
        val record = createTypedCSVRecord().let {
            val values = it.values.dropLast(5).toMutableList()
            values[8] = TypedValue(namespace = "orkg", value = researchProblemId.value, type = Classes.resource)
            it.copy(values = values)
        }
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders().dropLast(5)
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val researchProblem = createResource(id = researchProblemId)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
        every { resourceRepository.findById(researchProblemId) } returns Optional.of(researchProblem)

        shouldThrow<PaperCSVResourceNotFound> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
        verify(exactly = 1) { resourceRepository.findById(researchProblemId) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and statement object has invalid type, it throws an exception`() {
        val objectId = ThingId("R123")
        val record = createTypedCSVRecord().let {
            val values = it.values.dropLast(4).toMutableList()
            values[10] = TypedValue(namespace = "orkg", value = objectId.value, type = Classes.comparison)
            it.copy(values = values)
        }
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders().dropLast(4)
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)

        shouldThrow<UnknownCSVValueType> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
    }

    @Test
    fun `Given a typed csv record, when parsing a paper csv record, and statement object does not exist, it throws an exception`() {
        val objectId = ThingId("R123")
        val record = createTypedCSVRecord().let {
            val values = it.values.dropLast(4).toMutableList()
            values[10] = TypedValue(namespace = "orkg", value = objectId.value, type = Classes.resource)
            it.copy(values = values)
        }
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, Type.PAPER).toJobParameters()
        val headers = createPaperCSVHeaders().dropLast(4)
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
            executionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, emptyMap<CSVHeader, Either<ThingId, String>>())
        }
        val researchFieldId = ThingId("R456")
        val researchField = createResource(id = researchFieldId, classes = setOf(Classes.researchField))
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val doi = DOI.of("10.1000/182")

        paperCSVRecordParser.beforeStep(stepExecution)

        every { doiService.findMetadataByDoi(doi) } returns Optional.empty()
        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
        every { thingRepository.findById(objectId) } returns Optional.empty()

        shouldThrow<PaperCSVThingNotFound> { paperCSVRecordParser.process(record) }

        verify(exactly = 1) { doiService.findMetadataByDoi(doi) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
        verify(exactly = 1) { thingRepository.findById(objectId) }
    }
}
