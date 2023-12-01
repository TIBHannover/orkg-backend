package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class VisualizationServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: PostgresOrganizationRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val researchFieldService: RetrieveResearchFieldUseCase = mockk()

    private val service = VisualizationService(
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        listService = listService,
        researchFieldService = researchFieldService
    )

    @Test
    fun `Given a visualization exists, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.visualization),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val description = "Description of a visualization"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = description)
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            )
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get().asClue { visualization ->
            visualization.id shouldBe expected.id
            visualization.title shouldBe expected.label
            visualization.description shouldBe description
            visualization.authors shouldNotBe null
            visualization.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                )
            )
            visualization.observatories shouldBe setOf(expected.observatoryId)
            visualization.organizations shouldBe setOf(expected.organizationId)
            visualization.extractionMethod shouldBe expected.extractionMethod
            visualization.createdAt shouldBe expected.createdAt
            visualization.createdBy shouldBe expected.createdBy
            visualization.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) }
    }
}
