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
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

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
        val literalPropertyPlaceholderLiteralId = ThingId("L131")
        val literalPropertyMinLiteralId = ThingId("L123")
        val literalPropertyMaxLiteralId = ThingId("L124")
        val literalPropertyPatternLiteralId = ThingId("L125")
        val literalPropertyOrderLiteralId = ThingId("L126")
        val resourcePropertyId = ThingId("R1326")
        val resourcePropertyPlaceholderLiteralId = ThingId("L132")
        val resourcePropertyMinLiteralId = ThingId("L127")
        val resourcePropertyMaxLiteralId = ThingId("L128")
        val resourcePropertyPatternLiteralId = ThingId("L129")
        val resourcePropertyOrderLiteralId = ThingId("L130")

        // Literal property mocks
        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns literalPropertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "literal property placeholder"
                )
            )
        } returns literalPropertyPlaceholderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.placeholder,
                `object` = literalPropertyPlaceholderLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "1",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns literalPropertyMinLiteralId andThen literalPropertyOrderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMinCount,
                `object` = literalPropertyMinLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "2",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns literalPropertyMaxLiteralId andThen resourcePropertyOrderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMaxCount,
                `object` = literalPropertyMaxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = literalProperty.pattern.toString()
                )
            )
        } returns literalPropertyPatternLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPattern,
                `object` = literalPropertyPatternLiteralId
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
                `object` = literalPropertyOrderLiteralId
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
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns resourcePropertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "resource property placeholder"
                )
            )
        } returns resourcePropertyPlaceholderLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.placeholder,
                `object` = resourcePropertyPlaceholderLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns resourcePropertyMinLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMinCount,
                `object` = resourcePropertyMinLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns resourcePropertyMaxLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMaxCount,
                `object` = resourcePropertyMaxLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.pattern.toString()
                )
            )
        } returns resourcePropertyPatternLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPattern,
                `object` = resourcePropertyPatternLiteralId
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
                `object` = resourcePropertyOrderLiteralId
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
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
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
                subject = literalPropertyId,
                predicate = Predicates.placeholder,
                `object` = literalPropertyPlaceholderLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMinCount,
                `object` = literalPropertyMinLiteralId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shMaxCount,
                `object` = literalPropertyMaxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = literalProperty.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shPattern,
                `object` = literalPropertyPatternLiteralId
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "1",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteralId
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
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
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
                subject = resourcePropertyId,
                predicate = Predicates.placeholder,
                `object` = resourcePropertyPlaceholderLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.minCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMinCount,
                `object` = resourcePropertyMinLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.maxCount.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shMaxCount,
                `object` = resourcePropertyMaxLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = resourceProperty.pattern.toString()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shPattern,
                `object` = resourcePropertyPatternLiteralId
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "2",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteralId
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
            placeholder = null,
            minCount = null,
            maxCount = null,
            pattern = null
        )
        val resourceProperty = dummyCreateResourceTemplatePropertyCommand().copy(
            placeholder = null,
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
        val literalPropertyOrderLiteralId = ThingId("L126")
        val resourcePropertyId = ThingId("R1326")
        val resourcePropertyOrderLiteralId = ThingId("L130")

        // Literal property mocks
        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = literalProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns literalPropertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "1",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns literalPropertyOrderLiteralId
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
                `object` = literalPropertyOrderLiteralId
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
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = resourceProperty.label,
                    classes = setOf(Classes.propertyShape),
                    contributorId = command.contributorId
                )
            )
        } returns resourcePropertyId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "2",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns resourcePropertyOrderLiteralId
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
                `object` = resourcePropertyOrderLiteralId
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
            resourceService.createUnsafe(
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "1",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literalPropertyId,
                predicate = Predicates.shOrder,
                `object` = literalPropertyOrderLiteralId
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
            resourceService.createUnsafe(
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "2",
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourcePropertyId,
                predicate = Predicates.shOrder,
                `object` = resourcePropertyOrderLiteralId
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
