package org.orkg.contenttypes.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.output.DoiService
import org.orkg.contenttypes.domain.testing.fixtures.dummyPublishCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class PublishingServiceUnitTests {
    private val doiService: DoiService = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val fixedTime = OffsetDateTime.of(2023, 9, 11, 13, 30, 57, 12345, ZoneOffset.ofHours(1))
    private val staticClock = Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())

    private val service = PublishingService(doiService, resourceRepository, statementService, literalService, staticClock)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(doiService, resourceRepository, statementService, literalService)
    }

    @Test
    fun `Given a publish command, then a doi is registered and it returns success`() {
        val command = dummyPublishCommand()
        val id = command.id
        val doi = DOI.of("10.123/$id")
        val resource = createResource(id = id).copy(classes = setOf(Classes.paper))
        val literal = createLiteral(
            id = ThingId("L1"),
            label = doi.value
        )
        val year = createLiteral(
            id = ThingId("L2"),
            label = "2023",
            datatype = Literals.XSD.INT.prefixedUri
        )
        val month = createLiteral(
            id = ThingId("L3"),
            label = "9",
            datatype = Literals.XSD.INT.prefixedUri
        )

        every { resourceRepository.findById(id) } returns Optional.of(resource)
        every { statementService.findAllBySubjectAndPredicate(id, Predicates.hasDOI, PageRequests.SINGLE) } returns pageOf()
        every { doiService.register(any()) } returns doi
        every { literalService.create(command.contributorId, doi.value) } returns literal
        every { statementService.create(command.contributorId, id, Predicates.hasDOI, literal.id) } returns StatementId("S123")
        every { literalService.create(command.contributorId, label = "2023", datatype = Literals.XSD.INT.prefixedUri) } returns year
        every { statementService.create(command.contributorId, id, Predicates.yearPublished, year.id) } returns StatementId("S465")
        every { literalService.create(command.contributorId, label = "9", datatype = Literals.XSD.INT.prefixedUri) } returns month
        every { statementService.create(command.contributorId, id, Predicates.monthPublished, month.id) } returns StatementId("S789")

        val result = service.publish(command)
        result shouldBe doi

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { statementService.findAllBySubjectAndPredicate(command.id, Predicates.hasDOI, PageRequests.SINGLE) }
        verify(exactly = 1) {
            doiService.register(
                withArg {
                    it.suffix shouldBe command.id.value
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
        verify(exactly = 1) { literalService.create(command.contributorId, doi.value) }
        verify(exactly = 1) { statementService.create(command.contributorId, id, Predicates.hasDOI, literal.id) }
        verify(exactly = 1) { literalService.create(command.contributorId, label = "2023", datatype = Literals.XSD.INT.prefixedUri) }
        verify(exactly = 1) { statementService.create(command.contributorId, id, Predicates.yearPublished, year.id) }
        verify(exactly = 1) { literalService.create(command.contributorId, label = "9", datatype = Literals.XSD.INT.prefixedUri) }
        verify(exactly = 1) { statementService.create(command.contributorId, id, Predicates.monthPublished, month.id) }
    }

    @Test
    fun `Given a publish command, when resource does not exist, an exception is thrown`() {
        val command = dummyPublishCommand()
        val id = command.id

        every { resourceRepository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> { service.publish(command) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }

    @Test
    fun `Given a publish command, when resource does not contain a publishable class, an exception is thrown`() {
        val command = dummyPublishCommand()
        val id = command.id
        val resource = createResource(id = id)

        every { resourceRepository.findById(id) } returns Optional.of(resource)

        shouldThrow<UnpublishableThing> { service.publish(command) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }

    @Test
    fun `Given a publish command, when resource already has a doi, an exception is thrown`() {
        val command = dummyPublishCommand()
        val id = command.id
        val resource = createResource(id = id, classes = setOf(Classes.paper))

        every { resourceRepository.findById(id) } returns Optional.of(resource)
        every { statementService.findAllBySubjectAndPredicate(id, Predicates.hasDOI, PageRequests.SINGLE) } returns pageOf(
            createStatement(
                subject = resource,
                predicate = createPredicate(Predicates.hasDOI),
                `object` = createLiteral(label = "10.1000/183")
            )
        )

        shouldThrow<DoiAlreadyRegistered> { service.publish(command) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { statementService.findAllBySubjectAndPredicate(command.id, Predicates.hasDOI, PageRequests.SINGLE) }
    }
}
