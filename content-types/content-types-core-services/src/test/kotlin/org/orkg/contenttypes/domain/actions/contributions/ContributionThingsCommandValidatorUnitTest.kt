package org.orkg.contenttypes.domain.actions.contributions

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
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createContributionCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createClass

@Nested
internal class ContributionThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val contributionThingsCommandValidator = ContributionThingsCommandValidator(thingsCommandValidator)

    @Test
    fun `Given a contribution create command, when validating its thing commands, it returns success`() {
        val command = createContributionCommand()
        val state = ContributionState()
        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R2000" to Either.right(createClass(ThingId("R2000")))
        )

        every { thingsCommandValidator.validate(command, state.validationCache) } returns validationCache

        val result = contributionThingsCommandValidator(command, state)

        result.asClue {
            it.validationCache shouldBe validationCache
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { thingsCommandValidator.validate(command, state.validationCache) }
    }
}
