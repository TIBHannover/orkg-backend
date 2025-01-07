package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import java.util.stream.Stream
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteratureListListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteratureListTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

internal class LiteratureListSectionExistenceCreateValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val literatureListSectionExistenceCreateValidator =
        LiteratureListSectionExistenceCreateValidator(resourceRepository)

    @ParameterizedTest
    @MethodSource("createLiteratureListSectionCommands")
    fun `Given a literature list section create command, when searching for an existing literature list, it returns success`(command: CreateLiteratureListSectionCommand) {
        val state = CreateLiteratureListSectionState()
        val literatureList = createResource(id = command.literatureListId, classes = setOf(Classes.literatureList))

        every { resourceRepository.findById(command.literatureListId) } returns Optional.of(literatureList)

        val result = literatureListSectionExistenceCreateValidator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findById(command.literatureListId) }
    }

    @ParameterizedTest
    @MethodSource("createLiteratureListSectionCommands")
    fun `Given a literature list section create command, when existing literature list is published, it throws an exception`(command: CreateLiteratureListSectionCommand) {
        val state = CreateLiteratureListSectionState()
        val literatureList = createResource(id = command.literatureListId, classes = setOf(Classes.literatureListPublished))

        every { resourceRepository.findById(command.literatureListId) } returns Optional.of(literatureList)

        assertThrows<LiteratureListNotModifiable> { literatureListSectionExistenceCreateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.literatureListId) }
    }

    @ParameterizedTest
    @MethodSource("createLiteratureListSectionCommands")
    fun `Given a literature list section create command, when literature list does not exist, it throws an exception`(command: CreateLiteratureListSectionCommand) {
        val state = CreateLiteratureListSectionState()

        every { resourceRepository.findById(command.literatureListId) } returns Optional.empty()

        assertThrows<LiteratureListNotFound> { literatureListSectionExistenceCreateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.literatureListId) }
    }

    companion object {
        @JvmStatic
        fun createLiteratureListSectionCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(dummyCreateLiteratureListListSectionCommand()),
            Arguments.of(dummyCreateLiteratureListTextSectionCommand())
        )
    }
}
