package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplateInstance
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

class TemplateInstanceSubjectUpdaterUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val templateInstanceSubjectUpdater = TemplateInstanceSubjectUpdater(resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a template instance update command, when subject resource is not an instance of the template target class, it updates the resource`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            templateInstance = createDummyTemplateInstance().copy(
                root = createResource(classes = emptySet())
            ),
            template = createDummyTemplate()
        )
        val targetResource = state.templateInstance!!.root.copy(classes = setOf(state.template!!.targetClass))

        every { resourceRepository.save(targetResource) } just runs

        val result = templateInstanceSubjectUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance!!.copy(
                root = targetResource
            )
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { resourceRepository.save(targetResource) }
    }

    @Test
    fun `Given a template instance update command, when subject resource is already an instance of the template target class, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            templateInstance = createDummyTemplateInstance(),
            template = createDummyTemplate()
        )

        val result = templateInstanceSubjectUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }
    }
}
