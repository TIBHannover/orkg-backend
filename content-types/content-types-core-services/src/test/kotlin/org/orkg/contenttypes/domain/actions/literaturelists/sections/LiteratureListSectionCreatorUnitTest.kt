package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionCreator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTextSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()

    private val literatureListSectionCreator =
        LiteratureListSectionCreator(statementService, abstractLiteratureListSectionCreator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, abstractLiteratureListSectionCreator)
    }

    @ParameterizedTest
    @MethodSource("createLiteratureListSectionCommands")
    fun `Given a literature list section create command, it creates a new section and links it to the existing literature list`(command: CreateLiteratureListSectionCommand) {
        val sectionId = ThingId("R456")
        val state = CreateLiteratureListSectionState()

        every {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        } returns sectionId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.literatureListId,
                predicate = Predicates.hasSection,
                `object` = sectionId
            )
        } just runs

        val result = literatureListSectionCreator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.literatureListId,
                predicate = Predicates.hasSection,
                `object` = sectionId
            )
        }
    }

    companion object {
        @JvmStatic
        fun createLiteratureListSectionCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(dummyCreateListSectionCommand()),
            Arguments.of(dummyCreateTextSectionCommand())
        )
    }
}
