package org.orkg.contenttypes.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.testing.fixtures.dummyPublishCommand
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

class PublishingServiceUnitTests {
    private val doiService: DoiService = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val snapshotCreator = mockk<PublishingService.SnapshotCreator>()

    private val service = PublishingService(doiService, resourceRepository, statementService, literalService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(doiService, resourceRepository, statementService, literalService, snapshotCreator)
    }

    @Test
    fun `Given a publish command, then a doi is registered and it returns success`() {
        val command = dummyPublishCommand(snapshotCreator = snapshotCreator)
        val id = command.id
        val doi = DOI.of("10.123/$id")
        val resource = createResource(id = id).copy(classes = setOf(Classes.paper))
        val snapshotId = ThingId("R321")
        val doiLiteralId = ThingId("L1")

        every { resourceRepository.findById(id) } returns Optional.of(resource)
        every { snapshotCreator.createSnapshot() } returns snapshotId
        every { doiService.register(any()) } returns doi
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi.value
                )
            )
        } returns doiLiteralId
        every { statementService.create(command.contributorId, snapshotId, Predicates.hasDOI, doiLiteralId) } returns StatementId("S123")

        val result = service.publish(command)
        result shouldBe snapshotId

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { snapshotCreator.createSnapshot() }
        verify(exactly = 1) {
            doiService.register(
                withArg {
                    it.suffix shouldBe snapshotId.value
                    it.title shouldBe command.title
                    it.subject shouldBe command.subject
                    it.description shouldBe command.description
                    it.url shouldBe command.url
                    it.creators shouldBe command.creators
                    it.resourceType shouldBe command.resourceType.value
                    it.resourceTypeGeneral shouldBe "Dataset"
                    it.relatedIdentifiers shouldBe command.relatedIdentifiers
                }
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi.value
                )
            )
        }
        verify(exactly = 1) { statementService.create(command.contributorId, snapshotId, Predicates.hasDOI, doiLiteralId) }
    }

    @Test
    fun `Given a publish command, when resource does not exist, an exception is thrown`() {
        val command = dummyPublishCommand(snapshotCreator = snapshotCreator)
        val id = command.id

        every { resourceRepository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> { service.publish(command) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }

    @Test
    fun `Given a publish command, when resource does not contain a publishable class, an exception is thrown`() {
        val command = dummyPublishCommand(snapshotCreator = snapshotCreator)
        val id = command.id
        val resource = createResource(id = id)

        every { resourceRepository.findById(id) } returns Optional.of(resource)

        shouldThrow<UnpublishableThing> { service.publish(command) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }
}
