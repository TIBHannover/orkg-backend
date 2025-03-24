package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.IdentifierCreator
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand

internal class PaperIdentifierCreatorUnitTest : MockkBaseTest {
    private val identifierCreator: IdentifierCreator = mockk()

    private val paperIdentifierCreator = PaperIdentifierCreator(identifierCreator)

    @Test
    fun `Given a paper create command, it crates new paper identifiers`() {
        val command = createPaperCommand()
        val paperId = ThingId("R123")
        val state = CreatePaperState(paperId = paperId)

        every { identifierCreator.create(command.contributorId, command.identifiers, Identifiers.paper, paperId) } just runs

        val result = paperIdentifierCreator(command, state)

        result.asClue {
            it.validationCache.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) { identifierCreator.create(command.contributorId, command.identifiers, Identifiers.paper, paperId) }
    }
}
