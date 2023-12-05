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
import org.orkg.contenttypes.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral

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
        val minLiteral = ThingId("L123")
        val maxLiteral = ThingId("L124")
        val patternLiteral = ThingId("L125")
        val orderLiteral = ThingId("L126")

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(minLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(maxLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.pattern.toString()
            )
        } returns createLiteral(patternLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteral
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(orderLiteral)
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
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.pattern.toString()
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteral
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
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
    fun `Given a create literal template property command, when creating, it does not create null values`() {
        val command = dummyCreateLiteralTemplatePropertyCommand().copy(
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
            resourceService.create(
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(orderLiteral)
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
            resourceService.create(
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
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
        val minLiteral = ThingId("L123")
        val maxLiteral = ThingId("L124")
        val patternLiteral = ThingId("L125")
        val orderLiteral = ThingId("L126")

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns propertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(minLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(maxLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = command.pattern.toString()
            )
        } returns createLiteral(patternLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteral
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(orderLiteral)
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
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = command.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = minLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = maxLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = command.pattern.toString()
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = propertyId,
                predicate = Predicates.shPattern,
                `object` = patternLiteral
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
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
    fun `Given a create resource template property command, when creating, it does not create null values`() {
        val command = dummyCreateResourceTemplatePropertyCommand().copy(
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
            resourceService.create(
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(orderLiteral)
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
            resourceService.create(
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
                userId = command.contributorId,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri
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
