package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneStatementCommand
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class RosettaStoneStatementCreatorUnitTest : MockkBaseTest {
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()
    private val thingRepository: ThingRepository = mockk()

    private val rosettaStoneStatementCreator =
        RosettaStoneStatementCreator(rosettaStoneStatementRepository, thingRepository, fixedClock)

    @Test
    fun `Given a rosetta stone statement create command, when creating a new rosetta stone statement, it returns success`() {
        val command = createRosettaStoneStatementCommand()
        val state = CreateRosettaStoneStatementState(
            rosettaStoneTemplate = createRosettaStoneTemplate(),
            tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5"),
            validatedIds = mapOf(
                "R258" to Either.right(createResource(ThingId("R258"))),
                "R369" to Either.right(createResource(ThingId("R369"))),
                "R987" to Either.right(createResource(ThingId("R987"))),
                "R654" to Either.right(createResource(ThingId("R654"))),
                "R321" to Either.right(createResource(ThingId("R321"))),
                "R741" to Either.right(createResource(ThingId("R741"))),
                "C123" to Either.right(createClass(ThingId("C123"))),
                "#temp1" to Either.left("#temp1"),
                "#temp2" to Either.left("#temp2"),
                "#temp3" to Either.left("#temp3"),
                "#temp4" to Either.left("#temp4"),
                "#temp5" to Either.left("#temp5")
            ),
            tempId2Thing = mapOf(
                "#temp1" to ThingId("Temp1"),
                "#temp2" to ThingId("Temp2"),
                "#temp3" to ThingId("Temp3"),
                "#temp4" to ThingId("Temp4"),
                "#temp5" to ThingId("Temp5")
            )
        )
        val rosettaStoneStatementId = ThingId("R123")

        every {
            rosettaStoneStatementRepository.nextIdentity()
        } returns ThingId("R123") andThen ThingId("R123") andThenThrows IllegalStateException()
        every {
            thingRepository.findById(ThingId("Temp1"))
        } returns Optional.of(createResource(ThingId("Temp1")))
        every {
            thingRepository.findById(ThingId("Temp2"))
        } returns Optional.of(createLiteral(ThingId("Temp2")))
        every {
            thingRepository.findById(ThingId("Temp3"))
        } returns Optional.of(createPredicate(ThingId("Temp3")))
        every {
            thingRepository.findById(ThingId("Temp4"))
        } returns Optional.of(createResource(ThingId("Temp4"), classes = setOf(Classes.list)))
        every {
            thingRepository.findById(ThingId("Temp5"))
        } returns Optional.of(createClass(ThingId("Temp5")))
        every { rosettaStoneStatementRepository.save(any()) } just runs

        val result = rosettaStoneStatementCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe rosettaStoneStatementId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.tempId2Thing shouldBe state.tempId2Thing
        }

        verify(exactly = 2) { rosettaStoneStatementRepository.nextIdentity() }
        verify(exactly = 1) { thingRepository.findById(ThingId("Temp1")) }
        verify(exactly = 1) { thingRepository.findById(ThingId("Temp2")) }
        verify(exactly = 1) { thingRepository.findById(ThingId("Temp3")) }
        verify(exactly = 1) { thingRepository.findById(ThingId("Temp4")) }
        verify(exactly = 1) { thingRepository.findById(ThingId("Temp5")) }
        verify(exactly = 1) { rosettaStoneStatementRepository.save(any()) }
    }
}
