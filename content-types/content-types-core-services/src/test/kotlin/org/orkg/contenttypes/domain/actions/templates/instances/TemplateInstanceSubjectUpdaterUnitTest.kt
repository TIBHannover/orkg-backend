package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

internal class TemplateInstanceSubjectUpdaterUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val templateInstanceSubjectUpdater = TemplateInstanceSubjectUpdater(resourceRepository)

    @Test
    fun `Given a template instance update command, when subject resource is not an instance of the template target class, it updates the resource`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            templateInstance = createTemplateInstance().copy(
                root = createResource(classes = emptySet())
            ),
            template = createTemplate()
        )
        val targetResource = state.templateInstance!!.root.copy(classes = setOf(state.template!!.targetClass.id))

        every { resourceRepository.save(targetResource) } just runs

        val result = templateInstanceSubjectUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance!!.copy(
                root = targetResource
            )
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { resourceRepository.save(targetResource) }
    }

    @Test
    fun `Given a template instance update command, when subject resource is already an instance of the template target class, it returns success`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            templateInstance = createTemplateInstance(),
            template = createTemplate()
        )

        val result = templateInstanceSubjectUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }
    }
}
