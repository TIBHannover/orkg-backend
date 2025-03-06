package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.createLiteratureListCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class LiteratureListVersionResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val literatureListVersionResourceCreator = LiteratureListVersionResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a literature list create command, it crates a new literature list version resource`() {
        val command = createLiteratureListCommand()
        val state = CreateLiteratureListState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.title,
            classes = setOf(Classes.literatureListPublished),
            extractionMethod = command.extractionMethod,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = literatureListVersionResourceCreator(command, state)

        result.asClue {
            it.literatureListId shouldBe id
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
