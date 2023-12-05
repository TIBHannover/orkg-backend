package org.orkg.contenttypes.domain.actions.template

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
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.contenttypes.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral

class TemplatePropertiesCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templatePropertiesCreator = TemplatePropertiesCreator(resourceService, literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, literalService, statementService)
    }

    @Test
    fun `Given a create template command, when creating template properties, it returns success`() {
        val literalProperty = dummyCreateLiteralTemplatePropertyCommand()
        val resourceProperty = dummyCreateResourceTemplatePropertyCommand()
        val command = dummyCreateTemplateCommand().copy(
            properties = listOf(literalProperty, resourceProperty)
        )
        val state = TemplateState(
            templateId = ThingId("R123")
        )
        val literalPropertyId = ThingId("R1325")
        val literalPropertyMinLiteral = ThingId("L123")
        val literalPropertyMaxLiteral = ThingId("L124")
        val literalPropertyPatternLiteral = ThingId("L125")
        val literalPropertyOrderLiteral = ThingId("L126")
        val resourcePropertyId = ThingId("R1326")
        val resourcePropertyMinLiteral = ThingId("L127")
        val resourcePropertyMaxLiteral = ThingId("L128")
        val resourcePropertyPatternLiteral = ThingId("L129")
        val resourcePropertyOrderLiteral = ThingId("L130")

        // Literal property mocks
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns literalPropertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = "1",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(literalPropertyMinLiteral) andThen createLiteral(literalPropertyOrderLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMinCount,
                `object` = literalPropertyMinLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(literalPropertyMaxLiteral) andThen createLiteral(resourcePropertyOrderLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMaxCount,
                `object` = literalPropertyMaxLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = literalProperty.pattern.toString()
            )
        } returns createLiteral(literalPropertyPatternLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPattern,
                `object` = literalPropertyPatternLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shDatatype,
                `object` = literalProperty.datatype
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPath,
                `object` = literalProperty.path
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = literalPropertyId
            )
        } just runs

        // Resource property mocks
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns resourcePropertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(resourcePropertyMinLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMinCount,
                `object` = resourcePropertyMinLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(resourcePropertyMaxLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMaxCount,
                `object` = resourcePropertyMaxLiteral
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.pattern.toString()
            )
        } returns createLiteral(resourcePropertyPatternLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPattern,
                `object` = resourcePropertyPatternLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shClass,
                `object` = resourceProperty.`class`
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPath,
                `object` = resourceProperty.path
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = resourcePropertyId
            )
        } just runs

        val result = templatePropertiesCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        // Literal property mocks
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMinCount,
                `object` = literalPropertyMinLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMaxCount,
                `object` = literalPropertyMaxLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = literalProperty.pattern.toString()
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPattern,
                `object` = literalPropertyPatternLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shDatatype,
                `object` = literalProperty.datatype
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPath,
                `object` = literalProperty.path
            )
        }
        verify(exactly = 2) {
            literalService.create(
                userId = command.contributorId,
                label = "1",
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = literalPropertyId
            )
        }

        // Resource property mocks
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMinCount,
                `object` = resourcePropertyMinLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMaxCount,
                `object` = resourcePropertyMaxLiteral
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = resourceProperty.pattern.toString()
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPattern,
                `object` = resourcePropertyPatternLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shClass,
                `object` = resourceProperty.`class`
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPath,
                `object` = resourceProperty.path
            )
        }
        verify(exactly = 2) {
            literalService.create(
                userId = command.contributorId,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = resourcePropertyId
            )
        }
    }

    @Test
    fun `Given a create template command, when creating template properties, it does not create null values`() {
        val literalProperty = dummyCreateLiteralTemplatePropertyCommand().copy(
            minCount = null,
            maxCount = null,
            pattern = null
        )
        val resourceProperty = dummyCreateResourceTemplatePropertyCommand().copy(
            minCount = null,
            maxCount = null,
            pattern = null
        )
        val command = dummyCreateTemplateCommand().copy(
            properties = listOf(literalProperty, resourceProperty)
        )
        val state = TemplateState(
            templateId = ThingId("R123")
        )
        val literalPropertyId = ThingId("R1325")
        val literalPropertyOrderLiteral = ThingId("L126")
        val resourcePropertyId = ThingId("R1326")
        val resourcePropertyOrderLiteral = ThingId("L130")

        // Literal property mocks
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns literalPropertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = "1",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(literalPropertyOrderLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shDatatype,
                `object` = literalProperty.datatype
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPath,
                `object` = literalProperty.path
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = literalPropertyId
            )
        } just runs

        // Resource property mocks
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns resourcePropertyId
        every {
            literalService.create(
                userId = command.contributorId,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns createLiteral(resourcePropertyOrderLiteral)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shClass,
                `object` = resourceProperty.`class`
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPath,
                `object` = resourceProperty.path
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteral
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = resourcePropertyId
            )
        } just runs

        val result = templatePropertiesCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        // Literal property mocks
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shDatatype,
                `object` = literalProperty.datatype
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPath,
                `object` = literalProperty.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = "1",
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = literalPropertyId
            )
        }

        // Resource property mocks
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shClass,
                `object` = resourceProperty.`class`
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPath,
                `object` = resourceProperty.path
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteral
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shProperty,
                `object` = resourcePropertyId
            )
        }
    }
}
