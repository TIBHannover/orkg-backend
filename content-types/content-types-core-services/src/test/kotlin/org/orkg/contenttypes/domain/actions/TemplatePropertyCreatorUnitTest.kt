package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertyCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templatePropertyCreator =
        object : TemplatePropertyCreator(resourceService, literalService, statementService) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, literalService, statementService)
    }

    @Test
    fun `Given a literal template property definition, when creating, it returns success`() {
        val property = dummyCreateLiteralTemplatePropertyCommand()
        val order = 5
        val propertyId = ThingId("R1325")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val patternLiteralId = ThingId("L125")
        val orderLiteralId = ThingId("L126")
        
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        } returns patternLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = property.datatype
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = property.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = property.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        templatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = property.datatype
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = property.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = property.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }

    @Test
    fun `Given a resource template property definition, when creating, it returns success`() {
        val property = dummyCreateResourceTemplatePropertyCommand()
        val order = 5
        val propertyId = ThingId("R1325")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val patternLiteralId = ThingId("L125")
        val orderLiteralId = ThingId("L126")

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        } returns patternLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = property.`class`
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = property.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteralId
        every {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = property.contributorId,
                subject = property.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        templatePropertyCreator.create(property.contributorId, property.templateId, order, property)

        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = property.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = property.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = property.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = property.`class`
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = property.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = property.contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = property.contributorId,
                subject = property.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }
}
