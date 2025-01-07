package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.RosettaStoneTemplateService
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class RosettaStoneTemplateExistenceValidatorUnitTest : MockkBaseTest {
    private val rosettaStoneTemplateService: RosettaStoneTemplateService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val rosettaStoneTemplateExistenceValidator =
        RosettaStoneTemplateExistenceValidator(rosettaStoneTemplateService, resourceRepository)

    @Test
    fun `Given a rosetta stone template update command, when checking for rosetta stone template existence, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState()
        val root = createResource(
            id = rosettaStoneTemplate.id,
            label = rosettaStoneTemplate.label,
            classes = setOf(Classes.rosettaNodeShape)
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(RosettaStoneTemplate.Companion) {
            every { resourceRepository.findById(rosettaStoneTemplate.id) } returns Optional.of(root)
            every { rosettaStoneTemplateService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { RosettaStoneTemplate.from(root, statements) } returns rosettaStoneTemplate

            rosettaStoneTemplateExistenceValidator(command, state).asClue {
                it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
                it.statements shouldBe statements
                it.isUsedInRosettaStoneStatement shouldBe false
            }

            verify(exactly = 1) { resourceRepository.findById(rosettaStoneTemplate.id) }
            verify(exactly = 1) { rosettaStoneTemplateService.findSubgraph(root) }
            verify(exactly = 1) { RosettaStoneTemplate.from(root, statements) }
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when checking for template existence and rosetta stone template is not found, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState()

        every { resourceRepository.findById(rosettaStoneTemplate.id) } returns Optional.empty()

        shouldThrow<RosettaStoneTemplateNotFound> { rosettaStoneTemplateExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(rosettaStoneTemplate.id) }
    }
}
