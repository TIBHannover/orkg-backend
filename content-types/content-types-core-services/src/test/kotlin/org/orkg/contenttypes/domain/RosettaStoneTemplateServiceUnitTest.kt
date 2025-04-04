package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort
import java.util.Optional
import java.util.UUID

internal class RosettaStoneTemplateServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val thingRepository: ThingRepository = mockk()

    private val service = RosettaStoneTemplateService(
        resourceRepository,
        statementRepository,
        predicateRepository,
        classRepository,
        observatoryRepository,
        organizationRepository,
        resourceService,
        unsafeResourceUseCases,
        unsafeClassUseCases,
        statementService,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
        rosettaStoneStatementRepository,
        contributorRepository,
        thingRepository
    )

    @Test
    fun `Given a rosetta stone template, when fetching it by id, it is returned`() {
        val expected = createResource(
            label = "rosetta stone template label",
            classes = setOf(Classes.rosettaNodeShape),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val description = "rosetta stone template description"
        val formattedLabel = FormattedLabel.of("{P32}")
        val targetClassId = ThingId("targetClass")
        val exampleUsage = "example usage of template"
        val predicateId = ThingId("P22")
        val predicateLabel = "Predicate label"

        val untypedProperty = createResource(
            id = ThingId("R23"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val untypedPropertyPlaceholder = "untyped property placeholder"
        val untypedPropertyDescription = "untyped property description"
        val untypedPropertyOrder = 1
        val untypedPropertyMinCount = 1
        val untypedPropertyMaxCount = 2
        val untypedPropertyPath = createPredicate(ThingId("R24"), label = "untyped property path label")

        val stringLiteralProperty = createResource(
            id = ThingId("R26"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val stringLiteralPropertyPlaceholder = "string literal property placeholder"
        val stringLiteralPropertyDescription = "string literal property description"
        val stringLiteralPropertyOrder = 2
        val stringLiteralPropertyMinCount = 1
        val stringLiteralPropertyMaxCount = 2
        val stringLiteralPropertyPattern = """\d+"""
        val stringLiteralPropertyPath = createPredicate(ThingId("R27"), label = "string literal property path label")
        val stringLiteralPropertyDatatype = createClass(Classes.string, label = "string literal property class label")

        val numberLiteralProperty = createResource(
            id = ThingId("R29"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val numberLiteralPropertyPlaceholder = "number literal property placeholder"
        val numberLiteralPropertyDescription = "number literal property description"
        val numberLiteralPropertyOrder = 3
        val numberLiteralPropertyMinCount = 1
        val numberLiteralPropertyMaxCount = 2
        val numberLiteralPropertyMinInclusive = RealNumber(5.0)
        val numberLiteralPropertyMaxInclusive = RealNumber(10.0)
        val numberLiteralPropertyPath = createPredicate(ThingId("R30"), label = "number literal property path label")
        val numberLiteralPropertyDatatype = createClass(Classes.decimal, label = "number literal property class label")

        val otherLiteralProperty = createResource(
            id = ThingId("R32"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val otherLiteralPropertyPlaceholder = "literal property placeholder"
        val otherLiteralPropertyDescription = "literal property description"
        val otherLiteralPropertyOrder = 4
        val otherLiteralPropertyMinCount = 1
        val otherLiteralPropertyMaxCount = 2
        val otherLiteralPropertyPath = createPredicate(ThingId("R33"), label = "literal property path label")
        val otherLiteralPropertyDatatype = createClass(ThingId("R34"), label = "literal property class label")

        val resourceProperty = createResource(
            id = ThingId("R38"),
            label = "property label",
            classes = setOf(Classes.propertyShape)
        )
        val resourcePropertyPlaceholder = "resource property placeholder"
        val resourcePropertyDescription = "resource property description"
        val resourcePropertyOrder = 6
        val resourcePropertyMinCount = 3
        val resourcePropertyMaxCount = 4
        val resourcePropertyPattern = """\w+"""
        val resourcePropertyPath = createPredicate(ThingId("R39"), label = "resource property path label")
        val resourcePropertyClass = createClass(ThingId("R40"), label = "resource property class label")

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
                subject = createClass(targetClassId),
                predicate = createPredicate(Predicates.exampleOfUsage),
                `object` = createLiteral(label = exampleUsage)
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
                `object` = untypedProperty
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = stringLiteralProperty
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = numberLiteralProperty
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = otherLiteralProperty
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.shProperty),
                `object` = resourceProperty
            ),
            // Statements for untyped property
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = untypedPropertyPlaceholder)
            ),
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = untypedPropertyDescription)
            ),
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(
                    label = untypedPropertyOrder.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(
                    label = untypedPropertyMinCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(
                    label = untypedPropertyMaxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = untypedProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = untypedPropertyPath
            ),
            // Statements for string literal property
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = stringLiteralPropertyPlaceholder)
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = stringLiteralPropertyDescription)
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(
                    label = stringLiteralPropertyOrder.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(
                    label = stringLiteralPropertyMinCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(
                    label = stringLiteralPropertyMaxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = stringLiteralPropertyPattern)
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = stringLiteralPropertyPath
            ),
            createStatement(
                subject = stringLiteralProperty,
                predicate = createPredicate(Predicates.shDatatype),
                `object` = stringLiteralPropertyDatatype
            ),
            // Statements for number literal property
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = numberLiteralPropertyPlaceholder)
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = numberLiteralPropertyDescription)
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(
                    label = numberLiteralPropertyOrder.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(
                    label = numberLiteralPropertyMinCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(
                    label = numberLiteralPropertyMaxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shMinInclusive),
                `object` = createLiteral(
                    label = numberLiteralPropertyMinInclusive.toString(),
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shMaxInclusive),
                `object` = createLiteral(
                    label = numberLiteralPropertyMaxInclusive.toString(),
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = numberLiteralPropertyPath
            ),
            createStatement(
                subject = numberLiteralProperty,
                predicate = createPredicate(Predicates.shDatatype),
                `object` = numberLiteralPropertyDatatype
            ),
            // Statements for literal property
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = otherLiteralPropertyPlaceholder)
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = otherLiteralPropertyDescription)
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(
                    label = otherLiteralPropertyOrder.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(
                    label = otherLiteralPropertyMinCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(
                    label = otherLiteralPropertyMaxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.shPath),
                `object` = otherLiteralPropertyPath
            ),
            createStatement(
                subject = otherLiteralProperty,
                predicate = createPredicate(Predicates.shDatatype),
                `object` = otherLiteralPropertyDatatype
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
            template.exampleUsage shouldBe exampleUsage
            template.properties.size shouldBe 5
            template.properties shouldContainInOrder listOf(
                UntypedTemplateProperty(
                    id = untypedProperty.id,
                    label = untypedProperty.label,
                    placeholder = untypedPropertyPlaceholder,
                    description = untypedPropertyDescription,
                    order = untypedPropertyOrder.toLong(),
                    minCount = untypedPropertyMinCount,
                    maxCount = untypedPropertyMaxCount,
                    path = ObjectIdAndLabel(untypedPropertyPath.id, untypedPropertyPath.label),
                    createdBy = untypedProperty.createdBy,
                    createdAt = untypedProperty.createdAt
                ),
                StringLiteralTemplateProperty(
                    id = stringLiteralProperty.id,
                    label = stringLiteralProperty.label,
                    placeholder = stringLiteralPropertyPlaceholder,
                    description = stringLiteralPropertyDescription,
                    order = stringLiteralPropertyOrder.toLong(),
                    minCount = stringLiteralPropertyMinCount,
                    maxCount = stringLiteralPropertyMaxCount,
                    pattern = stringLiteralPropertyPattern,
                    path = ObjectIdAndLabel(stringLiteralPropertyPath.id, stringLiteralPropertyPath.label),
                    createdBy = stringLiteralProperty.createdBy,
                    createdAt = stringLiteralProperty.createdAt,
                    datatype = ClassReference(stringLiteralPropertyDatatype)
                ),
                NumberLiteralTemplateProperty(
                    id = numberLiteralProperty.id,
                    label = numberLiteralProperty.label,
                    placeholder = numberLiteralPropertyPlaceholder,
                    description = numberLiteralPropertyDescription,
                    order = numberLiteralPropertyOrder.toLong(),
                    minCount = numberLiteralPropertyMinCount,
                    maxCount = numberLiteralPropertyMaxCount,
                    minInclusive = numberLiteralPropertyMinInclusive,
                    maxInclusive = numberLiteralPropertyMaxInclusive,
                    path = ObjectIdAndLabel(numberLiteralPropertyPath.id, numberLiteralPropertyPath.label),
                    createdBy = numberLiteralProperty.createdBy,
                    createdAt = numberLiteralProperty.createdAt,
                    datatype = ClassReference(numberLiteralPropertyDatatype)
                ),
                OtherLiteralTemplateProperty(
                    id = otherLiteralProperty.id,
                    label = otherLiteralProperty.label,
                    placeholder = otherLiteralPropertyPlaceholder,
                    description = otherLiteralPropertyDescription,
                    order = otherLiteralPropertyOrder.toLong(),
                    minCount = otherLiteralPropertyMinCount,
                    maxCount = otherLiteralPropertyMaxCount,
                    path = ObjectIdAndLabel(otherLiteralPropertyPath.id, otherLiteralPropertyPath.label),
                    createdBy = otherLiteralProperty.createdBy,
                    createdAt = otherLiteralProperty.createdAt,
                    datatype = ClassReference(otherLiteralPropertyDatatype)
                ),
                ResourceTemplateProperty(
                    id = resourceProperty.id,
                    label = resourceProperty.label,
                    placeholder = resourcePropertyPlaceholder,
                    description = resourcePropertyDescription,
                    order = resourcePropertyOrder.toLong(),
                    minCount = resourcePropertyMinCount,
                    maxCount = resourcePropertyMaxCount,
                    path = ObjectIdAndLabel(resourcePropertyPath.id, resourcePropertyPath.label),
                    createdBy = resourceProperty.createdBy,
                    createdAt = resourceProperty.createdAt,
                    `class` = ObjectIdAndLabel(resourcePropertyClass.id, resourcePropertyClass.label)
                )
            )
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

    @Test
    fun `Given a rosetta stone template, when deleting it by id, it deletes the template`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy
        )
        val property = createResource(
            id = ThingId("TemplateProperty"),
            classes = setOf(Classes.propertyShape)
        )
        val targetClass = createClass(ThingId("TargetClass"))
        val contributorId = template.createdBy

        every { resourceService.findById(template.id) } returns Optional.of(resource)
        every { thingRepository.isUsedAsObject(template.id) } returns false
        every {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every {
            statementRepository.fetchAsBundle(
                id = resource.id,
                configuration = any(),
                sort = Sort.unsorted()
            )
        } returns listOf(
            createStatement(
                id = StatementId("S1"),
                subject = resource,
                predicate = createPredicate(Predicates.shProperty),
                `object` = property
            ),
            createStatement(
                id = StatementId("S2"),
                subject = resource,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral()
            ),
            createStatement(
                id = StatementId("S3"),
                subject = resource,
                predicate = createPredicate(Predicates.shTargetClass),
                `object` = targetClass
            ),
            createStatement(
                id = StatementId("S4"),
                subject = property,
                predicate = createPredicate(Predicates.shClass),
                `object` = createClass()
            ),
            createStatement(
                id = StatementId("S5"),
                subject = property,
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral()
            ),
            createStatement(
                id = StatementId("S6"),
                subject = property,
                predicate = createPredicate(Predicates.shPath),
                `object` = createPredicate()
            ),
            createStatement(
                id = StatementId("S7"),
                subject = targetClass,
                predicate = createPredicate(Predicates.exampleOfUsage),
                `object` = createLiteral()
            )
        )
        every { statementRepository.deleteByStatementIds(any()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        service.delete(template.id, contributorId)

        verify(exactly = 1) { resourceService.findById(template.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(template.id) }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = resource.id,
                configuration = any(),
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            statementRepository.deleteByStatementIds(
                setOf(
                    StatementId("S1"),
                    StatementId("S2"),
                    StatementId("S3"),
                    StatementId("S4"),
                    StatementId("S5"),
                    StatementId("S6")
                )
            )
        }
        verify(exactly = 1) { resourceService.delete(template.id, contributorId) }
        verify(exactly = 1) { resourceService.delete(property.id, contributorId) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is not found, it does nothing`() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { resourceService.findById(id) } returns Optional.empty()

        service.delete(id, contributorId)

        verify(exactly = 1) { resourceService.findById(id) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and id does not belong to a template, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            createdBy = template.createdBy
        )
        val contributorId = template.createdBy

        every { resourceService.findById(template.id) } returns Optional.of(resource)

        assertThrows<RosettaStoneTemplateNotFound> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is not modifiable, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy,
            modifiable = false
        )
        val contributorId = template.createdBy

        every { resourceService.findById(template.id) } returns Optional.of(resource)

        assertThrows<RosettaStoneTemplateNotModifiable> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is used in a statement, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy
        )
        val contributorId = template.createdBy

        every { resourceService.findById(template.id) } returns Optional.of(resource)
        every { thingRepository.isUsedAsObject(template.id) } returns true

        assertThrows<RosettaStoneTemplateInUse> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(template.id) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is used in a rosetta stone statement, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy
        )
        val contributorId = template.createdBy

        every { resourceService.findById(template.id) } returns Optional.of(resource)
        every { thingRepository.isUsedAsObject(template.id) } returns false
        every {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createRosettaStoneStatement()
        )

        assertThrows<RosettaStoneTemplateInUse> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(template.id) }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is not owned by contributor and contributor cannot be found, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy
        )
        val contributorId = ContributorId(MockUserId.USER)

        every { resourceService.findById(template.id) } returns Optional.of(resource)
        every { thingRepository.isUsedAsObject(template.id) } returns false
        every {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every { contributorRepository.findById(contributorId) } returns Optional.empty()

        assertThrows<ContributorNotFound> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(template.id) }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a rosetta stone template, when deleting it by id and template is not owned by contributor and contributor is not a curator, it throws an exception`() {
        val template = createRosettaStoneTemplate()
        val resource = createResource(
            id = template.id,
            classes = setOf(Classes.rosettaNodeShape),
            createdBy = template.createdBy
        )
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId)

        every { resourceService.findById(template.id) } returns Optional.of(resource)
        every { thingRepository.isUsedAsObject(template.id) } returns false
        every {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)

        assertThrows<NeitherOwnerNorCurator> {
            service.delete(template.id, contributorId)
        }

        verify(exactly = 1) { resourceService.findById(template.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(template.id) }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }
}
