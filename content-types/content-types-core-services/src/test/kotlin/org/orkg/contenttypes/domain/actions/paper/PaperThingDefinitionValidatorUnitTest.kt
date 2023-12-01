package org.orkg.contenttypes.domain.actions.paper

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass

@Nested
class PaperThingDefinitionValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()

    private val paperThingDefinitionValidator = PaperThingDefinitionValidator(thingRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given a paper create command, when validating its thing definitions, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        val `class` = createClass(ThingId("R2000"))

        every { thingRepository.findByThingId(`class`.id) } returns Optional.of(`class`)

        val result = paperThingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findByThingId(`class`.id) }
    }

    @Test
    fun `Given a paper create command, when no things are defined, it returns success`() {
        val command = dummyCreatePaperCommand().let {
            it.copy(
                contents = CreatePaperUseCase.CreateCommand.PaperContents(
                    resources = emptyMap(),
                    literals = emptyMap(),
                    predicates = emptyMap(),
                    lists = emptyMap(),
                    contributions = it.contents!!.contributions
                )
            )
        }
        val state = PaperState()

        val result = paperThingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }
}