package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand

internal class LiteratureListSectionsUpdateValidatorUnitTest {
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator = mockk()

    private val literatureListSectionsUpdateValidator = LiteratureListSectionsUpdateValidator(abstractLiteratureListSectionValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractLiteratureListSectionValidator)
    }

    @Test
    fun `Given a literature list update command, when no literature list sections are defined, it does nothing`() {
        val command = dummyUpdateLiteratureListCommand().copy(sections = null)
        val literatureList = createDummyLiteratureList()
        val state = UpdateLiteratureListState(literatureList = literatureList)

        literatureListSectionsUpdateValidator(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a literature list update command, when validating literature list sections, it returns success`() {
        val command = dummyUpdateLiteratureListCommand()
        val literatureList = createDummyLiteratureList()
        val state = UpdateLiteratureListState(literatureList = literatureList)
        val validIds = mutableSetOf(ThingId("R154686"), ThingId("R6416"))

        every { abstractLiteratureListSectionValidator.validate(any(), validIds) } just runs

        literatureListSectionsUpdateValidator(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        command.sections!!.forEach { section ->
            verify(exactly = 1) { abstractLiteratureListSectionValidator.validate(section, validIds) }
        }
    }
}
