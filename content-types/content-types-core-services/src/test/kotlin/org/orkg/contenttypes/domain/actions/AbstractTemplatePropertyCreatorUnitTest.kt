package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.input.testing.fixtures.createNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

internal class AbstractTemplatePropertyCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val abstractTemplatePropertyCreator =
        AbstractTemplatePropertyCreator(unsafeResourceUseCases, literalService, statementService)

    @Test
    fun `Given an untyped template property definition, when creating, it returns success`() {
        val property = createUntypedTemplatePropertyCommand()
        val order = 3
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val orderLiteralId = ThingId("L126")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
    }

    @Test
    fun `Given a string literal template property definition, when creating, it returns success`() {
        val property = createStringLiteralTemplatePropertyCommand()
        val order = 4
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val patternLiteralId = ThingId("L125")
        val orderLiteralId = ThingId("L126")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } returns StatementId("S1")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        } returns patternLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shPattern,
                    objectId = patternLiteralId
                )
            )
        } returns StatementId("S2")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shPattern,
                    objectId = patternLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a string literal template property definition, when pattern is not set, it does not create a statement for it`() {
        val property = createStringLiteralTemplatePropertyCommand().copy(pattern = null)
        val order = 4
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val orderLiteralId = ThingId("L126")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        }
    }

    @Test
    fun `Given a literal template property definition, when creating, it returns success`() {
        val property = createOtherLiteralTemplatePropertyCommand()
        val order = 4
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val orderLiteralId = ThingId("L126")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        }
    }

    @Test
    fun `Given a number literal template property definition, when creating, it returns success`() {
        val property = createNumberLiteralTemplatePropertyCommand()
        val order = 4
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val minInclusiveLiteralId = ThingId("L125")
        val maxInclusiveLiteralId = ThingId("L126")
        val orderLiteralId = ThingId("L127")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } returns StatementId("S1")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minInclusive.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minInclusiveLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinInclusive,
                    objectId = minInclusiveLiteralId
                )
            )
        } returns StatementId("S2")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxInclusive.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxInclusiveLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxInclusive,
                    objectId = maxInclusiveLiteralId
                )
            )
        } returns StatementId("S3")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minInclusive.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinInclusive,
                    objectId = minInclusiveLiteralId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxInclusive.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxInclusive,
                    objectId = maxInclusiveLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a number literal template property definition, when minInclusive and maxInclusive are not set, it does not create statements for them`() {
        val property = createNumberLiteralTemplatePropertyCommand().copy(
            minInclusive = null,
            maxInclusive = null
        )
        val order = 4
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val orderLiteralId = ThingId("L127")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        }
    }

    @Test
    fun `Given a resource template property definition, when creating, it returns success`() {
        val property = createResourceTemplatePropertyCommand()
        val order = 1
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val descriptionLiteralId = ThingId("L128")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val orderLiteralId = ThingId("L126")

        mockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shClass,
                    objectId = property.`class`
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verifyMockCommonProperties(
            property,
            order,
            propertyId,
            placeholderLiteralId,
            descriptionLiteralId,
            minLiteralId,
            maxLiteralId,
            orderLiteralId
        )
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shClass,
                    objectId = property.`class`
                )
            )
        }
    }

    private fun mockCommonProperties(
        property: CreateTemplatePropertyCommand,
        order: Int,
        propertyId: ThingId,
        placeholderLiteralId: ThingId,
        descriptionLiteralId: ThingId,
        minLiteralId: ThingId,
        maxLiteralId: ThingId,
        orderLiteralId: ThingId,
    ) {
        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        } returns propertyId
        property.placeholder?.let { placeholder ->
            every {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        label = placeholder
                    )
                )
            } returns placeholderLiteralId
            every {
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.placeholder,
                        objectId = placeholderLiteralId
                    )
                )
            } returns StatementId("Splaceholder")
        }
        property.description?.let { description ->
            every {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        label = description
                    )
                )
            } returns descriptionLiteralId
            every {
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.description,
                        objectId = descriptionLiteralId
                    )
                )
            } returns StatementId("Sdescription")
        }
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinCount,
                    objectId = minLiteralId
                )
            )
        } returns StatementId("SminCount")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxCount,
                    objectId = maxLiteralId
                )
            )
        } returns StatementId("SmaxCount")
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shPath,
                    objectId = property.path
                )
            )
        } returns StatementId("Spath")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shOrder,
                    objectId = orderLiteralId
                )
            )
        } returns StatementId("Sorder")
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = property.templateId,
                    predicateId = Predicates.shProperty,
                    objectId = propertyId
                )
            )
        } returns StatementId("Sproperty")
    }

    private fun verifyMockCommonProperties(
        property: CreateTemplatePropertyCommand,
        order: Int,
        propertyId: ThingId,
        placeholderLiteralId: ThingId,
        descriptionLiteralId: ThingId,
        minLiteralId: ThingId,
        maxLiteralId: ThingId,
        orderLiteralId: ThingId,
    ) {
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        }
        property.placeholder?.let { placeholder ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        label = placeholder
                    )
                )
            }
            verify(exactly = 1) {
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.placeholder,
                        objectId = placeholderLiteralId
                    )
                )
            }
        }
        property.description?.let { description ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        label = description
                    )
                )
            }
            verify(exactly = 1) {
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = property.contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.description,
                        objectId = descriptionLiteralId
                    )
                )
            }
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinCount,
                    objectId = minLiteralId
                )
            )
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxCount,
                    objectId = maxLiteralId
                )
            )
        }

        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shPath,
                    objectId = property.path
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shOrder,
                    objectId = orderLiteralId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = property.contributorId,
                    subjectId = property.templateId,
                    predicateId = Predicates.shProperty,
                    objectId = propertyId
                )
            )
        }
    }
}
