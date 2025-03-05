package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.testing.fixtures.createNestedTemplateInstance
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.Optional

internal class TemplateInstanceServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val templateService: TemplateUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classService: ClassUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val classHierarchyRepository: ClassHierarchyRepository = mockk()

    private val service = TemplateInstanceService(
        resourceRepository,
        templateService,
        statementService,
        unsafeStatementUseCases,
        thingRepository,
        classService,
        unsafeResourceUseCases,
        unsafeLiteralUseCases,
        unsafePredicateUseCases,
        listService,
        statementRepository,
        classRepository,
        classHierarchyRepository
    )

    @Test
    fun `Given a template instance, when fetching it by id, then it is returned`() {
        val template = createTemplate()
        val expected = createTemplateInstance()

        val untypedPropertyId = template.properties[0].path.id
        val stringLiteralPropertyId = template.properties[1].path.id
        val numberLiteralPropertyId = template.properties[2].path.id
        val otherLiteralPropertyId = template.properties[3].path.id
        val resourcePropertyId = template.properties[4].path.id

        every { templateService.findById(template.id) } returns Optional.of(template)
        every { resourceRepository.findById(expected.root.id) } returns Optional.of(expected.root)
        every {
            statementService.findAll(subjectId = expected.root.id, pageable = PageRequests.ALL)
        } returns pageOf(
            expected.statements[untypedPropertyId]!!.single().toStatement(expected.root, untypedPropertyId),
            expected.statements[stringLiteralPropertyId]!!.single().toStatement(expected.root, stringLiteralPropertyId),
            expected.statements[numberLiteralPropertyId]!!.single().toStatement(expected.root, numberLiteralPropertyId),
            expected.statements[otherLiteralPropertyId]!!.single().toStatement(expected.root, otherLiteralPropertyId),
            expected.statements[resourcePropertyId]!!.single().toStatement(expected.root, resourcePropertyId)
        )

        val actual = service.findById(template.id, expected.root.id)

        actual.isPresent shouldBe true
        actual.get().asClue { templateInstance ->
            templateInstance.root shouldBe expected.root
            templateInstance.statements shouldBe expected.statements
        }

        verify(exactly = 1) { templateService.findById(template.id) }
        verify(exactly = 1) { resourceRepository.findById(expected.root.id) }
        verify(exactly = 1) {
            statementService.findAll(subjectId = expected.root.id, pageable = PageRequests.ALL)
        }
    }

    @Test
    fun `Given a template instance, when fetching it nested by id, then it is returned`() {
        val rootTemplate = createTemplate()
        val authorTemplate = createTemplate().copy(
            id = ThingId("R032154"),
            targetClass = ClassReference(createClass(ThingId("C28"))),
            properties = listOf(
                createStringLiteralTemplateProperty().copy(
                    order = 0,
                    pattern = null,
                    path = ObjectIdAndLabel(Predicates.hasDOI, "has doi")
                ),
                createStringLiteralTemplateProperty().copy(
                    order = 1,
                    pattern = null,
                    path = ObjectIdAndLabel(Predicates.hasWikidataId, "has wikidata id")
                )
            )
        )
        val expected = createNestedTemplateInstance()

        val untypedPropertyId = rootTemplate.properties[0].path.id
        val stringLiteralPropertyId = rootTemplate.properties[1].path.id
        val numberLiteralPropertyId = rootTemplate.properties[2].path.id
        val otherLiteralPropertyId = rootTemplate.properties[3].path.id
        val resourcePropertyId = rootTemplate.properties[4].path.id
        val hasAuthor = expected.statements[resourcePropertyId]!!.single()

        every { templateService.findById(rootTemplate.id) } returns Optional.of(rootTemplate)
        every { resourceRepository.findById(expected.root.id) } returns Optional.of(expected.root)
        every {
            statementService.findAll(subjectId = expected.root.id, pageable = PageRequests.ALL)
        } returns pageOf(
            expected.statements[untypedPropertyId]!!.single().toStatement(expected.root, untypedPropertyId),
            expected.statements[stringLiteralPropertyId]!!.single().toStatement(expected.root, stringLiteralPropertyId),
            expected.statements[numberLiteralPropertyId]!!.single().toStatement(expected.root, numberLiteralPropertyId),
            expected.statements[otherLiteralPropertyId]!!.single().toStatement(expected.root, otherLiteralPropertyId),
            hasAuthor.toStatement(expected.root, resourcePropertyId)
        )
        every {
            templateService.findAll(targetClass = ThingId("C28"), pageable = PageRequests.SINGLE)
        } returns pageOf(authorTemplate)
        every {
            statementService.findAll(subjectId = hasAuthor.thing.id, pageable = PageRequests.ALL)
        } returns pageOf(
            hasAuthor.statements[Predicates.hasDOI]!!.single().toStatement(hasAuthor.thing, Predicates.hasDOI),
        )

        val actual = service.findById(rootTemplate.id, expected.root.id, nested = true)

        actual.isPresent shouldBe true
        actual.get().asClue { templateInstance ->
            templateInstance.root shouldBe expected.root
            templateInstance.statements shouldBe expected.statements
        }

        verify(exactly = 1) { templateService.findById(rootTemplate.id) }
        verify(exactly = 1) { resourceRepository.findById(expected.root.id) }
        verify(exactly = 1) {
            statementService.findAll(subjectId = expected.root.id, pageable = PageRequests.ALL)
        }
        verify(exactly = 1) {
            templateService.findAll(targetClass = ThingId("C28"), pageable = PageRequests.SINGLE)
        }
        verify(exactly = 1) {
            statementService.findAll(subjectId = hasAuthor.thing.id, pageable = PageRequests.ALL)
        }
    }

    @Test
    fun `Given a template instance, when template does not exist, then it throws an exception`() {
        every { templateService.findById(any()) } returns Optional.empty()

        shouldThrow<TemplateNotFound> {
            service.findById(ThingId("R123"), ThingId("R456"))
        }

        verify(exactly = 1) { templateService.findById(any()) }
    }

    @Test
    fun `Given a template instance, when resource is not an instance of template target class, then it throws an exception`() {
        every { templateService.findById(any()) } returns Optional.of(createTemplate())
        every { resourceRepository.findById(any()) } returns Optional.of(createResource())

        shouldThrow<TemplateNotApplicable> {
            service.findById(ThingId("R123"), ThingId("R456"))
        }

        verify(exactly = 1) { templateService.findById(any()) }
        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    private fun EmbeddedStatement.toStatement(subject: Thing, predicateId: ThingId): GeneralStatement =
        createStatement(
            subject = subject,
            predicate = createPredicate(predicateId),
            `object` = thing,
            createdAt = createdAt,
            createdBy = createdBy
        )
}
