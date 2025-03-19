package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import java.util.Optional

@Nested
internal class PaperThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val paperThingsCommandValidator = PaperThingsCommandValidator(thingRepository, classRepository)

    @Test
    fun `Given a paper create command, when validating its thing commands, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState()

        val `class` = createClass(ThingId("R2000"))

        every { thingRepository.findById(`class`.id) } returns Optional.of(`class`)

        val result = paperThingsCommandValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findById(`class`.id) }
    }

    @Test
    fun `Given a paper create command, when no things are defined, it returns success`() {
        val command = createPaperCommand().let {
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
        val state = CreatePaperState()

        val result = paperThingsCommandValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }
}
