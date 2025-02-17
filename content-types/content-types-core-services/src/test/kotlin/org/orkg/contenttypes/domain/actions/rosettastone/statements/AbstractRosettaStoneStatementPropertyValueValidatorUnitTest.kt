package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.MissingObjectPositionValue
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.MissingSubjectPositionValue
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.ObjectPositionValueDoesNotMatchPattern
import org.orkg.contenttypes.domain.ObjectPositionValueTooHigh
import org.orkg.contenttypes.domain.ObjectPositionValueTooLow
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.TooManyObjectPositionValues
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.TooManySubjectPositionValues
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.testing.fixtures.createNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralObjectPositionTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createSubjectPositionTemplateProperty
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

internal class AbstractRosettaStoneStatementPropertyValueValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases = mockk()
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = mockk()

    private val abstractRosettaStoneStatementPropertyValueValidator = AbstractRosettaStoneStatementPropertyValueValidator(
        thingRepository, statementRepository, rosettaStoneStatementService, abstractTemplatePropertyValueValidator
    )

    @Test
    fun `Given a rosetta stone statement create command, when validating its properties, it returns success`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = mapOf(
            "#temp1" to LiteralDefinition("1"),
            "#temp2" to ResourceDefinition(
                label = "MOTO",
                classes = setOf(ThingId("C28"))
            )
        )
        val validatedIds: Map<String, Either<String, Thing>> = mapOf(
            "R123" to Either.right(
                createResource(
                    id = ThingId("R123"),
                    label = "word",
                    classes = setOf(ThingId("C28"))
                )
            ),
            "R1" to Either.right(
                createResource(
                    label = "other",
                    classes = setOf(ThingId("C28"))
                )
            ),
            "L123" to Either.right(
                createLiteral(
                    id = ThingId("L123"),
                    label = "10",
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            )
        )
        val tempIds = setOf("#temp1", "#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("R123", "R1", "#temp2")
        val objects = listOf(
            listOf("#temp1", "L123")
        )

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        val result = abstractRosettaStoneStatementPropertyValueValidator.validate(
            templateProperties = templateProperties,
            thingDefinitions = thingDefinitions,
            validatedIdsIn = validatedIds,
            tempIds = tempIds,
            templateId = templateId,
            subjects = subjects,
            objects = objects
        )

        result shouldBe validatedIds + mapOf(
            "#temp2" to Either.left("#temp2"),
            "#temp1" to Either.left("#temp1")
        )

        verify { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too few object positions are defined, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = emptySet<String>()
        val templateId = ThingId("R456")
        val subjects = listOf("R123", "R1", "#temp2")
        val objects = emptyList<List<String>>()

        shouldThrow<MissingInputPositions> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too many input positions are defined, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = emptySet<String>()
        val templateId = ThingId("R456")
        val subjects = listOf("R123", "R1", "#temp2")
        val objects = listOf(
            listOf("#temp1", "L123"),
            listOf("#temp1", "L123")
        )

        shouldThrow<TooManyInputPositions> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }
    }

    @Test
    fun `Given a rosetta stone statement create command, when inputs contain a rosetta stone statement that contains rosetta stone statements, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty().copy(
                `class` = ObjectIdAndLabel(Classes.rosettaStoneStatement, "irrelevant")
            )
        )
        val templateId = ThingId("R456")
        val rosettaStoneStatement = createRosettaStoneStatement().let {
            it.copy(
                versions = listOf(
                    it.latestVersion.copy(
                        subjects = listOf(createResource(classes = setOf(Classes.rosettaStoneStatement)))
                    )
                )
            )
        }
        val latestVersionId = rosettaStoneStatement.latestVersion.id
        val rosettaStoneStatementResource = createResource(latestVersionId, classes = setOf(Classes.rosettaStoneStatement))
        val subjects = listOf(latestVersionId.value)

        every { thingRepository.findById(latestVersionId) } returns Optional.of(rosettaStoneStatementResource)
        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { rosettaStoneStatementService.findByIdOrVersionId(any()) } returns Optional.of(rosettaStoneStatement)

        assertThrows<NestedRosettaStoneStatement> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = emptyMap(),
                validatedIdsIn = emptyMap(),
                tempIds = emptySet(),
                templateId = templateId,
                subjects = subjects,
                objects = emptyList()
            )
        }

        verify(exactly = 1) { thingRepository.findById(latestVersionId) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(latestVersionId) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when inputs contain a rosetta stone statement that does not contain a rosetta stone statement, it returns success`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty().copy(
                `class` = ObjectIdAndLabel(Classes.rosettaStoneStatement, "irrelevant")
            )
        )
        val templateId = ThingId("R456")
        val rosettaStoneStatement = createRosettaStoneStatement()
        val latestVersionId = rosettaStoneStatement.latestVersion.id
        val rosettaStoneStatementResource = createResource(latestVersionId, classes = setOf(Classes.rosettaStoneStatement))
        val subjects = listOf(latestVersionId.value)

        every { thingRepository.findById(latestVersionId) } returns Optional.of(rosettaStoneStatementResource)
        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { rosettaStoneStatementService.findByIdOrVersionId(any()) } returns Optional.of(rosettaStoneStatement)
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        val result = abstractRosettaStoneStatementPropertyValueValidator.validate(
            templateProperties = templateProperties,
            thingDefinitions = emptyMap(),
            validatedIdsIn = emptyMap(),
            tempIds = emptySet(),
            templateId = templateId,
            subjects = subjects,
            objects = emptyList()
        )

        result shouldBe mapOf(
            latestVersionId.value to Either.right<String, Thing>(rosettaStoneStatementResource)
        )

        verify(exactly = 1) { thingRepository.findById(latestVersionId) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(latestVersionId) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateObject(any(), latestVersionId.value, any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when inputs contain a rosetta stone statement that cannot be found, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty().copy(
                `class` = ObjectIdAndLabel(Classes.rosettaStoneStatement, "irrelevant")
            )
        )
        val templateId = ThingId("R456")
        val rosettaStoneStatement = createRosettaStoneStatement()
        val latestVersionId = rosettaStoneStatement.latestVersion.id
        val rosettaStoneStatementResource = createResource(latestVersionId, classes = setOf(Classes.rosettaStoneStatement))
        val subjects = listOf(latestVersionId.value)

        every { thingRepository.findById(latestVersionId) } returns Optional.of(rosettaStoneStatementResource)
        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { rosettaStoneStatementService.findByIdOrVersionId(any()) } returns Optional.empty()

        assertThrows<RosettaStoneStatementNotFound> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = emptyMap(),
                validatedIdsIn = emptyMap(),
                tempIds = emptySet(),
                templateId = templateId,
                subjects = subjects,
                objects = emptyList()
            )
        }

        verify(exactly = 1) { thingRepository.findById(latestVersionId) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(latestVersionId) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when inputs contain a rosetta stone statement that does not have the specified version, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty().copy(
                `class` = ObjectIdAndLabel(Classes.rosettaStoneStatement, "irrelevant")
            )
        )
        val templateId = ThingId("R456")
        val rosettaStoneStatement = createRosettaStoneStatement()
        val latestVersionId = ThingId("Missing")
        val rosettaStoneStatementResource = createResource(latestVersionId, classes = setOf(Classes.rosettaStoneStatement))
        val subjects = listOf(latestVersionId.value)

        every { thingRepository.findById(latestVersionId) } returns Optional.of(rosettaStoneStatementResource)
        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { rosettaStoneStatementService.findByIdOrVersionId(any()) } returns Optional.of(rosettaStoneStatement)

        assertThrows<RosettaStoneStatementVersionNotFound> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = emptyMap(),
                validatedIdsIn = emptyMap(),
                tempIds = emptySet(),
                templateId = templateId,
                subjects = subjects,
                objects = emptyList()
            )
        }

        verify(exactly = 1) { thingRepository.findById(latestVersionId) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(latestVersionId) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too few subject inputs are specified, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = emptySet<String>()
        val templateId = ThingId("R456")
        val subjects = emptyList<String>()
        val objects = listOf(listOf("#temp1", "L123"))
        val exception = with(templateProperties.first()) { MissingPropertyValues(id, path.id, minCount!!, subjects.size) }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } throws exception

        shouldThrow<MissingSubjectPositionValue> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too few object inputs are specified, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = mapOf(
            "#temp2" to LiteralDefinition("subject")
        )
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = setOf("#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("#temp2")
        val objects = listOf(emptyList<String>())
        val exception = with(templateProperties.last()) { MissingPropertyValues(id, path.id, minCount!!, objects.size) }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs andThenThrows exception
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        shouldThrow<MissingObjectPositionValue> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too many subject inputs are specified, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = emptySet<String>()
        val templateId = ThingId("R456")
        val subjects = listOf("R123", "R789")
        val objects = listOf(listOf("#temp1", "L123"))
        val exception = with(templateProperties.first()) { TooManyPropertyValues(id, path.id, maxCount!!, subjects.size) }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } throws exception

        shouldThrow<TooManySubjectPositionValues> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too many object inputs are specified, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = mapOf(
            "#temp2" to LiteralDefinition("subject")
        )
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap()
        val tempIds = setOf("#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("#temp2")
        val objects = listOf(listOf("R123", "R789", "R147", "R369"))
        val exception = with(templateProperties.last()) { TooManyPropertyValues(id, path.id, maxCount!!, objects.size) }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs andThenThrows exception
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        shouldThrow<TooManyObjectPositionValues> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 1) { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when object position value does not match property pattern, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createStringLiteralObjectPositionTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val literal = createLiteral(ThingId("L123"), label = "15603")
        val validatedIds: Map<String, Either<String, Thing>> = mapOf(
            "L123" to Either.right(literal),
            "R123" to Either.right(createResource(ThingId("R123")))
        )
        val tempIds = setOf("#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("R123")
        val objects = listOf(listOf("L123", "L789", "L147", "L369"))
        val exception = with(templateProperties.last() as StringLiteralTemplateProperty) {
            LabelDoesNotMatchPattern(id, literal.id.value, path.id, literal.label, pattern!!)
        }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs andThenThrows exception

        shouldThrow<ObjectPositionValueDoesNotMatchPattern> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when object position value is too low, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createNumberLiteralTemplateProperty()
        )
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val literal = createLiteral(ThingId("L123"), label = "-20")
        val validatedIds: Map<String, Either<String, Thing>> = mapOf(
            "L123" to Either.right(literal),
            "R123" to Either.right(createResource(ThingId("R123")))
        )
        val tempIds = setOf("#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("R123")
        val objects = listOf(listOf("L123", "L789", "L147", "L369"))
        val exception = with(templateProperties.last() as NumberLiteralTemplateProperty) {
            NumberTooLow(id, literal.id.value, path.id, literal.label, minInclusive!!)
        }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs andThenThrows exception

        shouldThrow<ObjectPositionValueTooLow> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when object position value is too high, it throws an exception`() {
        val templateProperties = listOf(
            createSubjectPositionTemplateProperty(),
            createNumberLiteralTemplateProperty()
        )
        val literal = createLiteral(ThingId("L123"), label = "20")
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val validatedIds: Map<String, Either<String, Thing>> = mapOf(
            "L123" to Either.right(literal),
            "R123" to Either.right(createResource(ThingId("R123")))
        )
        val tempIds = setOf("#temp2")
        val templateId = ThingId("R456")
        val subjects = listOf("R123")
        val objects = listOf(listOf("L123", "L789", "L147", "L369"))
        val exception = with(templateProperties.last() as NumberLiteralTemplateProperty) {
            NumberTooHigh(id, literal.id.value, path.id, literal.label, maxInclusive!!)
        }

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs andThenThrows exception

        shouldThrow<ObjectPositionValueTooHigh> {
            abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = templateProperties,
                thingDefinitions = thingDefinitions,
                validatedIdsIn = validatedIds,
                tempIds = tempIds,
                templateId = templateId,
                subjects = subjects,
                objects = objects
            )
        }

        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify(exactly = 2) { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }
}
