package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand

internal class PaperTempIdValidatorUnitTest {
    private val paperTempIdValidator = PaperTempIdValidator()

    @Test
    fun `Given a paper create command, when validating its temp ids, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState()

        val result = paperTempIdValidator(command, state)

        result.asClue {
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4")
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }

    @Test
    fun `Given a paper create command, when it has no contributions, it returns success`() {
        val command = createPaperCommand().copy(contents = null)
        val state = CreatePaperState()

        val result = paperTempIdValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }
}
