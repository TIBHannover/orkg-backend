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
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteratureListCommand

class LiteratureListSectionsCreateValidatorUnitTest {
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator = mockk()

    private val literatureListSectionsCreateValidator = LiteratureListSectionsCreateValidator(abstractLiteratureListSectionValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractLiteratureListSectionValidator)
    }

    @Test
    fun `Given a literature list create command, when no literature list sections are defined, it does nothing`() {
        val command = dummyCreateLiteratureListCommand().copy(sections = emptyList())
        val state = CreateLiteratureListState()

        literatureListSectionsCreateValidator(command, state).asClue {
            it.literatureListId shouldBe state.literatureListId
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a literature list create command, when validating literature list sections, it returns success`() {
        val command = dummyCreateLiteratureListCommand()
        val state = CreateLiteratureListState()

        every { abstractLiteratureListSectionValidator.validate(any(), any()) } just runs

        literatureListSectionsCreateValidator(command, state).asClue {
            it.literatureListId shouldBe state.literatureListId
            it.authors.size shouldBe 0
        }

        command.sections.forEach { section ->
            verify(exactly = 1) { abstractLiteratureListSectionValidator.validate(section, any()) }
        }
    }
}
