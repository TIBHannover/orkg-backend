package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreateContributionCommand
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ContributionTempIdValidatorUnitTest {
    private val contributionTempIdValidator = ContributionTempIdValidator()

    @Test
    fun `Given a contribution create command, when validating its temp ids, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()

        val result = contributionTempIdValidator(command, state)

        result.asClue {
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4")
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }
    }
}
