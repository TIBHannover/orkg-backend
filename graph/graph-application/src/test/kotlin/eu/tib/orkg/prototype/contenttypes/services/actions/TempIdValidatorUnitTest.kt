package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.DuplicateTempIds
import eu.tib.orkg.prototype.contenttypes.application.InvalidTempId
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TempIdValidatorUnitTest {
    private val tempIdValidator = TempIdValidator()

    @Test
    fun `Given a paper create command, when validating its temp ids, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        val result = tempIdValidator(command, state)

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
        val command = dummyCreatePaperCommand().copy(contents = null)
        val state = PaperState()

        val result = tempIdValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }

    @Test
    fun `Given a contribution create command, when validating its temp ids, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()

        val result = tempIdValidator(command, state)

        result.asClue {
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4")
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }
    }

    @Test
    fun `Given a list of temp ids, when validating, it returns success`() {
        tempIdValidator.validate(listOf("#temp1", "#temp2", "#temp3", "#temp4"))
    }

    @Test
    fun `Given an empty list of temp ids, when validating, it returns success`() {
        tempIdValidator.validate(emptyList())
    }

    @Test
    fun `Given a list of temp ids, when temp id is too short, it throws an error`() {
        assertThrows<InvalidTempId> {
            tempIdValidator.validate(listOf("#"))
        }
    }

    @Test
    fun `Given a list of temp ids, when temp id is duplicate, it throws an error`() {
        val ids = listOf("#duplicate", "#duplicate")

        val result = assertThrows<DuplicateTempIds> {
            tempIdValidator.validate(ids)
        }

        result.duplicates shouldBe mapOf("#duplicate" to 2)
    }
}
