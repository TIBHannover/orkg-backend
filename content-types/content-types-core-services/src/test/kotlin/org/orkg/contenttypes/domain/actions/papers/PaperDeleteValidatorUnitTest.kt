package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperInUse
import org.orkg.contenttypes.domain.actions.DeletePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.deletePaperCommand
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource

internal class PaperDeleteValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val paperDeleteValidator = PaperDeleteValidator(thingRepository)

    @Test
    fun `Given a paper delete command, when paper is not used as an object, it returns success`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState().copy(paper = createResource(id = paper.id))

        every { thingRepository.isUsedAsObject(paper.id) } returns false

        paperDeleteValidator(command, state) shouldBe state

        verify(exactly = 1) { thingRepository.isUsedAsObject(paper.id) }
    }

    @Test
    fun `Given a paper delete command, when paper is used as an object, it throws an exception`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState().copy(paper = createResource(id = paper.id))

        every { thingRepository.isUsedAsObject(paper.id) } returns true

        shouldThrow<PaperInUse> { paperDeleteValidator(command, state) }

        verify(exactly = 1) { thingRepository.isUsedAsObject(paper.id) }
    }

    @Test
    fun `Given a paper delete command, when paper is null, it does nothing`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState()

        paperDeleteValidator(command, state) shouldBe state
    }
}
