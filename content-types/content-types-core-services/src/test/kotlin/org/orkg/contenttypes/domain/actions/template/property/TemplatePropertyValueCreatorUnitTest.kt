package org.orkg.contenttypes.domain.actions.template.property

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
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
import org.orkg.contenttypes.domain.actions.TemplatePropertyState
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

class TemplatePropertyValueCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templatePropertyValueCreator = TemplatePropertyValueCreator(resourceService, literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, literalService, statementService)
    }

    @Test
    fun `Given a create literal template property command, when creating, it returns success`() {
        val command = dummyCreateLiteralTemplatePropertyCommand()
        val state = TemplatePropertyState(
            templatePropertyId = command.templateId,
            propertyCount = 4
        )
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val patternLiteralId = ThingId("L125")
        val orderLiteralId = ThingId("L126")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "literal property placeholder"
                )
            )
        } returns placeholderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.placeholder,
                `object` = placeholderLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.pattern.toString()
                )
            )
        } returns patternLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = command.datatype
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        val result = templatePropertyValueCreator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe propertyId
            it.propertyCount shouldBe (state.propertyCount!! + 1)
        }

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "literal property placeholder"
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.placeholder,
                `object` = placeholderLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = command.datatype
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }

    @Test
    fun `Given a create literal template property command, when creating, it does not create null values`() {
        val command = dummyCreateLiteralTemplatePropertyCommand().copy(
            placeholder = null,
            minCount = null,
            maxCount = null,
            pattern = null
        )
        val state = TemplatePropertyState(
            templatePropertyId = command.templateId,
            propertyCount = 4
        )
        val propertyId = ThingId("R1325")
        val orderLiteral = ThingId("L126")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = command.datatype
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        val result = templatePropertyValueCreator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe propertyId
            it.propertyCount shouldBe (state.propertyCount!! + 1)
        }

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = command.datatype
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }

    @Test
    fun `Given a create resource template property command, when creating, it returns success`() {
        val command = dummyCreateResourceTemplatePropertyCommand()
        val state = TemplatePropertyState(
            templatePropertyId = command.templateId,
            propertyCount = 4
        )
        val propertyId = ThingId("R1325")
        val placeholderLiteralId = ThingId("L127")
        val minLiteralId = ThingId("L123")
        val maxLiteralId = ThingId("L124")
        val patternLiteralId = ThingId("L125")
        val orderLiteralId = ThingId("L126")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "resource property placeholder"
                )
            )
        } returns placeholderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.placeholder,
                `object` = placeholderLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns minLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns maxLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.pattern.toString()
                )
            )
        } returns patternLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = command.`class`
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        val result = templatePropertyValueCreator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe propertyId
            it.propertyCount shouldBe (state.propertyCount!! + 1)
        }

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "resource property placeholder"
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.placeholder,
                `object` = placeholderLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = command.`class`
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }

    @Test
    fun `Given a create resource template property command, when creating, it does not create null values`() {
        val command = dummyCreateResourceTemplatePropertyCommand().copy(
            placeholder = null,
            minCount = null,
            maxCount = null,
            pattern = null
        )
        val state = TemplatePropertyState(
            templatePropertyId = command.templateId,
            propertyCount = 4
        )
        val propertyId = ThingId("R1325")
        val orderLiteral = ThingId("L126")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = command.`class`
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns orderLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        } just runs

        val result = templatePropertyValueCreator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe propertyId
            it.propertyCount shouldBe (state.propertyCount!! + 1)
        }

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = command.`class`
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPath,
                `object` = command.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "5",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shOrder,
                `object` = orderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shProperty,
                `object` = propertyId
            )
        }
    }
}
