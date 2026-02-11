package org.orkg.dataimport.domain.csv.papers

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.DOI
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.dataimport.domain.CONTRIBUTOR_ID_FIELD
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.testing.MockUserId
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import java.util.Optional
import java.util.UUID

internal class PaperCSVRecordProcessorUnitTest : MockkBaseTest {
    private val paperUseCases: PaperUseCases = mockk()
    private val contributionUseCases: ContributionUseCases = mockk()

    private val paperCSVRecordProcessor = PaperCSVRecordProcessor(paperUseCases, contributionUseCases)

    @Test
    fun `Given a paper csv record, when paper already exists in the graph (matched by doi), it creates a new contribution`() {
        val record = createProcessedPaperCSVRecord()
        val contributorId = ContributorId(MockUserId.USER)
        val jobParameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val id = UUID.fromString("390b0013-96a0-4e46-b67a-7c2bfa0d977c")
        val paperId = ThingId("R123")
        val contributionId = ThingId("R147")
        val contributionCreateCommand = CreateContributionUseCase.CreateCommand(
            contributorId = contributorId,
            paperId = paperId,
            extractionMethod = record.extractionMethod,
            contribution = createContributionCommandPart()
        )

        paperCSVRecordProcessor.beforeStep(stepExecution)

        mockkStatic(UUID::class) {
            every { paperUseCases.existsByDOI(DOI.of(record.doi!!)) } returns Optional.of(paperId)
            every { contributionUseCases.create(contributionCreateCommand) } returns contributionId
            every { UUID.randomUUID() } returns id

            paperCSVRecordProcessor.process(record) shouldBe PaperCSVRecordImportResult(
                id = id,
                importedEntityId = contributionId,
                importedEntityType = PaperCSVRecordImportResult.Type.CONTRIBUTION,
                csvId = record.csvId,
                itemNumber = record.itemNumber,
                lineNumber = record.lineNumber,
            )

            verify(exactly = 1) { paperUseCases.existsByDOI(DOI.of(record.doi!!)) }
            verify(exactly = 1) { contributionUseCases.create(contributionCreateCommand) }
            verify(exactly = 1) { UUID.randomUUID() }
        }
    }

    @Test
    fun `Given a paper csv record, when paper already exists in the graph (matched by title), it creates a new contribution`() {
        val record = createProcessedPaperCSVRecord().copy(doi = null)
        val contributorId = ContributorId(MockUserId.USER)
        val jobParameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val id = UUID.fromString("390b0013-96a0-4e46-b67a-7c2bfa0d977c")
        val paperId = ThingId("R123")
        val contributionId = ThingId("R147")
        val contributionCreateCommand = CreateContributionUseCase.CreateCommand(
            contributorId = contributorId,
            paperId = paperId,
            extractionMethod = record.extractionMethod,
            contribution = createContributionCommandPart()
        )

        paperCSVRecordProcessor.beforeStep(stepExecution)

        mockkStatic(UUID::class) {
            every { paperUseCases.existsByTitle(any()) } returns Optional.of(paperId)
            every { contributionUseCases.create(contributionCreateCommand) } returns contributionId
            every { UUID.randomUUID() } returns id

            paperCSVRecordProcessor.process(record) shouldBe PaperCSVRecordImportResult(
                id = id,
                importedEntityId = contributionId,
                importedEntityType = PaperCSVRecordImportResult.Type.CONTRIBUTION,
                csvId = record.csvId,
                itemNumber = record.itemNumber,
                lineNumber = record.lineNumber,
            )

            verify(exactly = 1) { paperUseCases.existsByTitle(any()) }
            verify(exactly = 1) { contributionUseCases.create(contributionCreateCommand) }
            verify(exactly = 1) { UUID.randomUUID() }
        }
    }

    @Test
    fun `Given a paper csv record, when paper does not exists in the graph (matched by doi and title), it creates a new paper`() {
        val record = createProcessedPaperCSVRecord()
        val contributorId = ContributorId(MockUserId.USER)
        val jobParameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val id = UUID.fromString("390b0013-96a0-4e46-b67a-7c2bfa0d977c")
        val paperId = ThingId("R123")
        val paperCreateCommand = CreatePaperUseCase.CreateCommand(
            contributorId = contributorId,
            title = record.title,
            researchFields = listOf(record.researchFieldId),
            identifiers = mapOf("doi" to listOf(record.doi!!)),
            publicationInfo = PublicationInfoCommand(
                publishedMonth = record.publicationMonth,
                publishedYear = record.publicationYear,
                publishedIn = record.publishedIn,
                url = record.url
            ),
            authors = record.authors,
            sustainableDevelopmentGoals = emptySet(),
            mentionings = emptySet(),
            observatories = emptyList(),
            organizations = emptyList(),
            contents = CreatePaperUseCase.CreateCommand.PaperContents(
                contributions = listOf(createContributionCommandPart())
            ),
            extractionMethod = record.extractionMethod,
        )

        paperCSVRecordProcessor.beforeStep(stepExecution)

        mockkStatic(UUID::class) {
            every { paperUseCases.existsByDOI(DOI.of(record.doi!!)) } returns Optional.empty()
            every { paperUseCases.existsByTitle(any()) } returns Optional.empty()
            every { paperUseCases.create(paperCreateCommand) } returns paperId
            every { UUID.randomUUID() } returns id

            paperCSVRecordProcessor.process(record) shouldBe PaperCSVRecordImportResult(
                id = id,
                importedEntityId = paperId,
                importedEntityType = PaperCSVRecordImportResult.Type.PAPER,
                csvId = record.csvId,
                itemNumber = record.itemNumber,
                lineNumber = record.lineNumber,
            )

            verify(exactly = 1) { paperUseCases.existsByDOI(DOI.of(record.doi!!)) }
            verify(exactly = 1) { paperUseCases.existsByTitle(any()) }
            verify(exactly = 1) { paperUseCases.create(paperCreateCommand) }
            verify(exactly = 1) { UUID.randomUUID() }
        }
    }

    private fun createProcessedPaperCSVRecord(): PaperCSVRecord = createPaperCSVRecord().copy(
        statements = setOf(
            ContributionStatement(
                predicate = Either.left(Predicates.employs),
                `object` = TypedValue(namespace = "orkg", value = "R789", type = Classes.resource)
            ),
            ContributionStatement(
                predicate = Either.left(ThingId("result")),
                `object` = TypedValue(namespace = "orkg", value = "R369", type = Classes.resource)
            )
        )
    )

    private fun createContributionCommandPart(): CreateContributionCommandPart = CreateContributionCommandPart(
        label = "Contribution",
        statements = mapOf(
            "P2" to listOf(CreateContributionCommandPart.StatementObject("R789")),
            "result" to listOf(CreateContributionCommandPart.StatementObject("R369")),
        )
    )
}
