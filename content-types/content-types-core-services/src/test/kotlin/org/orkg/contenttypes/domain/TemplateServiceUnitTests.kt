package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort

class TemplateServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val templateRepository: TemplateRepository = mockk()

    private val service = TemplateService(
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        classRepository = classRepository,
        predicateRepository = predicateRepository,
        resourceService = resourceService,
        literalService = literalService,
        statementService = statementService,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        templateRepository = templateRepository
    )

    @Test
    fun `Given a template exists, when fetching it by id, then it is returned`() {
        val expected = createResource(
            label = "template label",
            classes = setOf(Classes.nodeShape),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val description = "template description"
        val formattedLabel = FormattedLabel.of("{P32}")
        val targetClassId = ThingId("targetClass")
        val researchFieldId = ThingId("R20")
        val researchFieldLabel = "Research Field 1"
        val researchProblemId = ThingId("R21")
        val researchProblemLabel = "Research Problem 1"
        val predicateId = ThingId("P22")
        val predicateLabel = "Predicate label"

        val literalProperty = createResource(
            id = ThingId("R23"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val literalPropertyOrder = 1
        val literalPropertyMinCount = 1
        val literalPropertyMaxCount = 2
        val literalPropertyPattern = """\d+"""
        val literalPropertyPath = createPredicate(ThingId("R24"), label = "literal property path label")
        val literalPropertyDatatype = createClass(ThingId("R25"), label = "literal property class label")

        val resourceProperty = createResource(
            id = ThingId("R26"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val resourcePropertyOrder = 2
        val resourcePropertyMinCount = 3
        val resourcePropertyMaxCount = 4
        val resourcePropertyPattern = """\w+"""
        val resourcePropertyPath = createPredicate(ThingId("R27"), label = "resource property path label")
        val resourcePropertyClass = createClass(ThingId("R28"), label = "resource property class label")
        
        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = BundleConfiguration(
                    minLevel = null,
                    maxLevel = 2,
                    blacklist = emptyList(),
                    whitelist = emptyList()
                ),
                sort = Sort.unsorted()
            )
        } returns listOf(
            // Statements for template root resource
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = description)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.templateLabelFormat),
                `object` = createLiteral(label = formattedLabel.value)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shTargetClass),
                `object` = createClass(targetClassId)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.templateOfResearchField),
                `object` = createResource(
                    id = researchFieldId,
                    classes = setOf(Classes.researchField),
                    label = researchFieldLabel
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.templateOfResearchProblem),
                `object` = createResource(
                    id = researchProblemId,
                    classes = setOf(Classes.problem),
                    label = researchProblemLabel
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.templateOfPredicate),
                `object` = createPredicate(predicateId, label = predicateLabel)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shClosed),
                `object` = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = literalProperty
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = resourceProperty
            ),

            //Statements for literal property
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(label = literalPropertyOrder.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(label = literalPropertyMinCount.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(label = literalPropertyMaxCount.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = literalPropertyPattern)
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = literalPropertyPath
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.shDatatype),
                `object` = literalPropertyDatatype
            ),

            //Statements for resource property
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(label = resourcePropertyOrder.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(label = resourcePropertyMinCount.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(label = resourcePropertyMaxCount.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = resourcePropertyPattern)
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = resourcePropertyPath
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.shClass),
                `object` = resourcePropertyClass
            )
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get().asClue { template ->
            template.id shouldBe expected.id
            template.label shouldBe expected.label
            template.description shouldBe description
            template.formattedLabel shouldBe formattedLabel
            template.targetClass shouldBe targetClassId
            template.relations.asClue {
                it.researchFields.size shouldBe 1
                it.researchFields.single() shouldBe ObjectIdAndLabel(researchFieldId, researchFieldLabel)
                it.researchProblems.size shouldBe 1
                it.researchProblems.single() shouldBe ObjectIdAndLabel(researchProblemId, researchProblemLabel)
                it.predicate shouldBe ObjectIdAndLabel(predicateId, predicateLabel)
            }
            template.properties.size shouldBe 2
            template.properties shouldContainInOrder listOf(
                LiteralTemplateProperty(
                    id = literalProperty.id,
                    label = literalProperty.label,
                    order = literalPropertyOrder.toLong(),
                    minCount = literalPropertyMinCount,
                    maxCount = literalPropertyMaxCount,
                    pattern = literalPropertyPattern,
                    path = ObjectIdAndLabel(literalPropertyPath.id, literalPropertyPath.label),
                    createdBy = literalProperty.createdBy,
                    createdAt = literalProperty.createdAt,
                    datatype = ObjectIdAndLabel(literalPropertyDatatype.id, literalPropertyDatatype.label)
                ),
                ResourceTemplateProperty(
                    id = resourceProperty.id,
                    label = resourceProperty.label,
                    order = resourcePropertyOrder.toLong(),
                    minCount = resourcePropertyMinCount,
                    maxCount = resourcePropertyMaxCount,
                    pattern = resourcePropertyPattern,
                    path = ObjectIdAndLabel(resourcePropertyPath.id, resourcePropertyPath.label),
                    createdBy = resourceProperty.createdBy,
                    createdAt = resourceProperty.createdAt,
                    `class` = ObjectIdAndLabel(resourcePropertyClass.id, resourcePropertyClass.label)
                )
            )
            template.isClosed shouldBe true
            template.createdBy shouldBe expected.createdBy
            template.createdAt shouldBe expected.createdAt
            template.observatories shouldBe setOf(expected.observatoryId)
            template.organizations shouldBe setOf(expected.organizationId)
            template.visibility shouldBe Visibility.DEFAULT
            template.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(expected.id, any(), any()) }
    }
}
