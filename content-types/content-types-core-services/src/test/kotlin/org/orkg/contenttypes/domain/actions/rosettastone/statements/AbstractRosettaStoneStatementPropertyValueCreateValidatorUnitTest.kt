package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.throwables.shouldThrow
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
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.testing.fixtures.createDummyStringLiteralObjectPositionTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummySubjectPositionTemplateProperty
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

class AbstractRosettaStoneStatementPropertyValueCreateValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = mockk()

    private val abstractRosettaStoneStatementPropertyValueValidator = AbstractRosettaStoneStatementPropertyValueValidator(
        thingRepository, abstractTemplatePropertyValueValidator
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository, abstractTemplatePropertyValueValidator)
    }

    @Test
    fun `Given a rosetta stone statement create command, when validating its properties, it returns success`() {
        val templateProperties = listOf(
            createDummySubjectPositionTemplateProperty(),
            createDummyStringLiteralObjectPositionTemplateProperty()
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
            createDummySubjectPositionTemplateProperty(),
            createDummyStringLiteralObjectPositionTemplateProperty()
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
            createDummySubjectPositionTemplateProperty(),
            createDummyStringLiteralObjectPositionTemplateProperty()
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
}
