package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder
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
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort

class RosettaTemplateServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = RosettaTemplateService(resourceRepository, statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, statementRepository)
    }

    @Test
    fun `Given a rosetta template, when fetching it by id, it is returned`() {
        val expected = createResource(
            label = "rosetta template label",
            classes = setOf(Classes.rosettaNodeShape),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val description = "rosetta template description"
        val formattedLabel = FormattedLabel.of("{P32}")
        val targetClassId = ThingId("targetClass")
        val predicateId = ThingId("P22")
        val predicateLabel = "Predicate label"

        val literalProperty = createResource(
            id = ThingId("R23"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val literalPropertyPlaceholder = "literal property placeholder"
        val literalPropertyDescription = "literal property description"
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
        val resourcePropertyPlaceholder = "resource property placeholder"
        val resourcePropertyDescription = "resource property description"
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

            // Statements for literal property
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = literalPropertyPlaceholder)
            ),
            createStatement(
                subject = literalProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = literalPropertyDescription)
            ),
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

            // Statements for resource property
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = resourcePropertyPlaceholder)
            ),
            createStatement(
                subject = resourceProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = resourcePropertyDescription)
            ),
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
            template.properties.size shouldBe 2
            template.properties shouldContainInOrder listOf(
                LiteralTemplateProperty(
                    id = literalProperty.id,
                    label = literalProperty.label,
                    placeholder = literalPropertyPlaceholder,
                    description = literalPropertyDescription,
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
                    placeholder = resourcePropertyPlaceholder,
                    description = resourcePropertyDescription,
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
