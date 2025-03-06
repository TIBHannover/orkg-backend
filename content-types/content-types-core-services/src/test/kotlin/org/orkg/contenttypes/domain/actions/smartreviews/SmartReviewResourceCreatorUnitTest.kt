package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class SmartReviewResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val smartReviewResourceCreator = SmartReviewResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a smart review create command, it crates a new smart review resource`() {
        val command = createSmartReviewCommand()
        val state = CreateSmartReviewState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.title,
            classes = setOf(Classes.smartReview),
            extractionMethod = command.extractionMethod,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = smartReviewResourceCreator(command, state)

        result.asClue {
            it.smartReviewId shouldBe id
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
