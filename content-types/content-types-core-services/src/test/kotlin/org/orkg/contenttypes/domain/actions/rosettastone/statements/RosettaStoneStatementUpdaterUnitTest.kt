package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.fixedClock

internal class RosettaStoneStatementUpdaterUnitTest {
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()
    private val thingRepository: ThingRepository = mockk()

    private val rosettaStoneStatementUpdater =
        RosettaStoneStatementUpdater(rosettaStoneStatementRepository, thingRepository, fixedClock)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(rosettaStoneStatementRepository, thingRepository)
    }

    @Test
    fun `Given a rosetta stone statement update command, when updating a rosetta stone statement, it returns success`() {
        val command = dummyUpdateRosettaStoneStatementCommand()
        val originalStatement = createRosettaStoneStatement()
        val r258 = createResource(ThingId("R258"))
        val r369 = createResource(ThingId("R369"))
        val r987 = createResource(ThingId("R987"))
        val r654 = createResource(ThingId("R654"))
        val r321 = createResource(ThingId("R321"))
        val r741 = createResource(ThingId("R741"))
        val c123 = createClass(ThingId("C123"))
        val state = UpdateRosettaStoneStatementState(
            rosettaStoneTemplate = createRosettaStoneTemplate(),
            rosettaStoneStatement = originalStatement,
            tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5"),
            validatedIds = mapOf(
                "R258" to Either.right(r258),
                "R369" to Either.right(r369),
                "R987" to Either.right(r987),
                "R654" to Either.right(r654),
                "R321" to Either.right(r321),
                "R741" to Either.right(r741),
                "C123" to Either.right(c123),
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
        val rosettaStoneStatementVersionId = ThingId("R123")
        val temp1 = createResource(ThingId("Temp1"))
        val temp2 = createLiteral(ThingId("Temp2"))
        val temp3 = createPredicate(ThingId("Temp3"))
        val temp4 = createResource(ThingId("Temp4"), classes = setOf(Classes.list))
        val temp5 = createClass(ThingId("Temp5"))

        every { rosettaStoneStatementRepository.nextIdentity() } returns rosettaStoneStatementVersionId
        every { thingRepository.findByThingId(ThingId("Temp1")) } returns Optional.of(temp1)
        every { thingRepository.findByThingId(ThingId("Temp2")) } returns Optional.of(temp2)
        every { thingRepository.findByThingId(ThingId("Temp3")) } returns Optional.of(temp3)
        every { thingRepository.findByThingId(ThingId("Temp4")) } returns Optional.of(temp4)
        every { thingRepository.findByThingId(ThingId("Temp5")) } returns Optional.of(temp5)
        every { rosettaStoneStatementRepository.save(any()) } just runs

        val result = rosettaStoneStatementUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatement shouldBe state.rosettaStoneStatement
            it.rosettaStoneStatementId shouldBe rosettaStoneStatementVersionId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.tempId2Thing shouldBe state.tempId2Thing
        }

        verify(exactly = 1) { rosettaStoneStatementRepository.nextIdentity() }
        verify(exactly = 1) { thingRepository.findByThingId(ThingId("Temp1")) }
        verify(exactly = 1) { thingRepository.findByThingId(ThingId("Temp2")) }
        verify(exactly = 1) { thingRepository.findByThingId(ThingId("Temp3")) }
        verify(exactly = 1) { thingRepository.findByThingId(ThingId("Temp4")) }
        verify(exactly = 1) { thingRepository.findByThingId(ThingId("Temp5")) }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.save(
                withArg {
                    it.id shouldBe originalStatement.id
                    it.contextId shouldBe originalStatement.contextId
                    it.templateId shouldBe originalStatement.templateId
                    it.templateTargetClassId shouldBe originalStatement.templateTargetClassId
                    it.label shouldBe originalStatement.label
                    it.versions.size shouldBe 2
                    it.versions[0] shouldBe originalStatement.versions.single()
                    it.versions[1].asClue { version ->
                        version.id shouldBe rosettaStoneStatementVersionId
                        version.subjects shouldBe listOf(r258, r369, temp1)
                        version.objects shouldBe listOf(
                            listOf(r987, r654, temp2, temp3),
                            listOf(r321, r741, temp4, temp5)
                        )
                        version.createdAt shouldBe OffsetDateTime.now(fixedClock)
                        version.createdBy shouldBe command.contributorId
                        version.certainty shouldBe command.certainty
                        version.negated shouldBe command.negated
                        version.observatories shouldBe command.observatories
                        version.organizations shouldBe command.organizations
                        version.extractionMethod shouldBe command.extractionMethod
                        version.visibility shouldBe command.visibility
                        version.modifiable shouldBe command.modifiable
                    }
                    it.observatories shouldBe command.observatories
                    it.organizations shouldBe command.organizations
                    it.extractionMethod shouldBe command.extractionMethod
                    it.visibility shouldBe command.visibility
                    it.modifiable shouldBe command.modifiable
                }
            )
        }
    }
}
