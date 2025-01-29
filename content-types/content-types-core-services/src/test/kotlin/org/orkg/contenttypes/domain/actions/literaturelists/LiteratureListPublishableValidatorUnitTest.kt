package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.actions.PublishLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.input.testing.fixtures.publishLiteratureListCommand

internal class LiteratureListPublishableValidatorUnitTest : MockkBaseTest {
    private val literatureListService: LiteratureListUseCases = mockk()

    private val literatureListPublishableValidator = LiteratureListPublishableValidator(literatureListService)

    @Test
    fun `Given a literature list publish command, when literature list is unpublished, it returns success`() {
        val literatureList = createLiteratureList()
        val command = publishLiteratureListCommand().copy(id = literatureList.id)
        val state = PublishLiteratureListState()

        every { literatureListService.findById(literatureList.id) } returns Optional.of(literatureList)

        literatureListPublishableValidator(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.literatureListVersionId shouldBe null
        }

        verify(exactly = 1) { literatureListService.findById(literatureList.id) }
    }

    @Test
    fun `Given a literature list publish command, when literature list is published, it throws an exception`() {
        val literatureList = createLiteratureList().copy(published = true)
        val command = publishLiteratureListCommand().copy(id = literatureList.id)
        val state = PublishLiteratureListState()

        every { literatureListService.findById(literatureList.id) } returns Optional.of(literatureList)

        assertThrows<LiteratureListAlreadyPublished> { literatureListPublishableValidator(command, state) }

        verify(exactly = 1) { literatureListService.findById(literatureList.id) }
    }

    @Test
    fun `Given a literature list publish command, when literature list does not exist, it throws an exception`() {
        val command = publishLiteratureListCommand()
        val state = PublishLiteratureListState()

        every { literatureListService.findById(command.id) } returns Optional.empty()

        assertThrows<LiteratureListNotFound> { literatureListPublishableValidator(command, state) }

        verify(exactly = 1) { literatureListService.findById(command.id) }
    }
}
