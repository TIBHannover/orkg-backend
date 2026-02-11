package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createClass

internal class PaperThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val paperThingsCommandValidator = PaperThingsCommandValidator(thingsCommandValidator)

    @Test
    fun `Given a paper create command, when validating its thing commands, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState()
        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R2000" to Either.right(createClass(ThingId("R2000")))
        )

        every { thingsCommandValidator.validate(command.contents!!, state.validationCache) } returns validationCache

        val result = paperThingsCommandValidator(command, state)

        result.asClue {
            it.validationCache shouldBe validationCache
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { thingsCommandValidator.validate(command.contents!!, state.validationCache) }
    }

    @Test
    fun `Given a paper create command, when no things are defined, it returns success`() {
        val command = createPaperCommand().copy(contents = null)
        val state = CreatePaperState()

        val result = paperThingsCommandValidator(command, state)

        result.asClue {
            it.validationCache.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }
}
