package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
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
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneStatementCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

class RosettaStoneStatementPropertyValueValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = mockk()

    private val rosettaStoneStatementPropertyValueValidator = RosettaStoneStatementPropertyValueValidator(
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
        val command = dummyCreateRosettaStoneStatementCommand().copy(
            subjects = listOf("R123", "R1", "#temp2"),
            objects = listOf(
                listOf("#temp1", "L123")
            ),
            resources = mapOf(
                "#temp2" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("C28"))
                )
            ),
            literals = mapOf(
                "#temp1" to LiteralDefinition("1")
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = CreateRosettaStoneStatementState(
            rosettaStoneTemplate = createDummyRosettaStoneTemplate(),
            tempIds = setOf("#temp1", "#temp2"),
            validatedIds = mapOf(
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
        )

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        val result = rosettaStoneStatementPropertyValueValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + mapOf(
                "#temp2" to Either.left("#temp2"),
                "#temp1" to Either.left("#temp1")
            )
        }

        verify { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too few object positions are defined, it throws an exception`() {
        val command = dummyCreateRosettaStoneStatementCommand().copy(
            subjects = listOf(
                "R123", "R1", "#temp2"
            ),
            objects = emptyList(),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = CreateRosettaStoneStatementState(
            rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        )

        shouldThrow<MissingInputPositions> { rosettaStoneStatementPropertyValueValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when too many input positions are defined, it throws an exception`() {
        val command = dummyCreateRosettaStoneStatementCommand().copy(
            subjects = listOf(
                "R123", "R1", "#temp2"
            ),
            objects = listOf(
                listOf("#temp1", "L123"),
                listOf("#temp1", "L123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = CreateRosettaStoneStatementState(
            rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        )

        shouldThrow<TooManyInputPositions> { rosettaStoneStatementPropertyValueValidator(command, state) }
    }
}
