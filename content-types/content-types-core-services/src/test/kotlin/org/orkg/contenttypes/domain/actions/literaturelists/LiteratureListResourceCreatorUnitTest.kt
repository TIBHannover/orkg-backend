package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteratureListCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class LiteratureListResourceCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val literatureListResourceCreator = LiteratureListResourceCreator(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a literature list create command, it crates a new literature list resource`() {
        val command = dummyCreateLiteratureListCommand()
        val state = CreateLiteratureListState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.literatureList),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = literatureListResourceCreator(command, state)

        result.asClue {
            it.literatureListId shouldBe id
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
