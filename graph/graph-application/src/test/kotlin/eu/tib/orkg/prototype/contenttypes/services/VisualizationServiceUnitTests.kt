package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.pageOf
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test

class VisualizationServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = VisualizationService(resourceRepository, statementRepository)

    @Test
    fun `Given a visualization exists, when fetching it by id, then it is returned`() {
        val expected = createResource().copy(
            classes = setOf(Classes.visualization),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val description = "Description of a visualization"
        val authorList = createResource().copy(classes = setOf(Classes.list), id = ThingId("R536456"))

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(id = Predicates.hasAuthors.value),
                `object` = authorList
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(id = Predicates.description.value),
                `object` = createLiteral(value = description)
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(id = Predicates.hasListElement.value),
                `object` = createLiteral(value = "Author 1")
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
