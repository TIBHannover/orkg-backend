package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
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
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplateInstance
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class TemplateInstanceServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val templateService: TemplateUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classService: ClassUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val service = TemplateInstanceService(
        resourceRepository = resourceRepository,
        templateService = templateService,
        statementService = statementService,
        thingRepository = thingRepository,
        classService = classService,
        resourceService = resourceService,
        literalService = literalService,
        predicateService = predicateService,
        listService = listService,
        classRepository = classRepository,
        statementRepository = statementRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            resourceRepository,
            templateService,
            statementService,
            thingRepository,
            classService,
            resourceService,
            literalService,
            predicateService,
            listService,
            statementRepository
        )
    }

    @Test
    fun `Given a template instance, when fetching it by id, then it is returned`() {
        val template = createDummyTemplate()
        val expected = createDummyTemplateInstance()

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
    fun `Given a template instance, when template does not exist, then it throws an exception`() {
        every { templateService.findById(any()) } returns Optional.empty()

        shouldThrow<TemplateNotFound> {
            service.findById(ThingId("R123"), ThingId("R456"))
        }

        verify(exactly = 1) { templateService.findById(any()) }
    }

    @Test
    fun `Given a template instance, when resource is not an instance of template target class, then it throws an exception`() {
        every { templateService.findById(any()) } returns Optional.of(createDummyTemplate())
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
