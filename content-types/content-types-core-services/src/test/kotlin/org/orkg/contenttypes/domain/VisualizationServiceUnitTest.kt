package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort
import java.util.Optional
import java.util.UUID

internal class VisualizationServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val visualizationRepository: VisualizationRepository = mockk()

    private val service = VisualizationService(
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        unsafeResourceUseCases = unsafeResourceUseCases,
        unsafeStatementUseCases = unsafeStatementUseCases,
        literalService = literalService,
        listService = listService,
        visualizationRepository = visualizationRepository
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
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(Classes.researchField),
            whitelist = emptyList()
        )

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = description)
            ),
            createStatement(
                subject = authorList,
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
            visualization.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        }
    }
}
