package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSmartReviewCommand

class SmartReviewSectionsCreateValidatorUnitTest {
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator = mockk()

    private val smartReviewSectionsCreateValidator = SmartReviewSectionsCreateValidator(abstractSmartReviewSectionValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractSmartReviewSectionValidator)
    }

    @Test
    fun `Given a smart review create command, when no smart review sections are defined, it does nothing`() {
        val command = dummyCreateSmartReviewCommand().copy(sections = emptyList())
        val state = CreateSmartReviewState()

        smartReviewSectionsCreateValidator(command, state).asClue {
            it.smartReviewId shouldBe state.smartReviewId
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review create command, when validating smart review sections, it returns success`() {
        val command = dummyCreateSmartReviewCommand()
        val state = CreateSmartReviewState()

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionsCreateValidator(command, state).asClue {
            it.smartReviewId shouldBe state.smartReviewId
            it.authors.size shouldBe 0
        }

        command.sections.forEach { section ->
            verify(exactly = 1) { abstractSmartReviewSectionValidator.validate(section, any()) }
        }
    }
}
