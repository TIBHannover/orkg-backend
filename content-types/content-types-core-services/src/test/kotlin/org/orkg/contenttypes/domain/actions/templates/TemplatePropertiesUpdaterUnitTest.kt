package org.orkg.contenttypes.domain.actions.templates

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
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplatePropertiesUpdaterUnitTest {
    private val abstractTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater = mockk()

    private val templatePropertiesUpdater = TemplatePropertiesUpdater(abstractTemplatePropertiesUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertiesUpdater)
    }

    @Test
    fun `Given a template update command, when properties are not set, it does nothing`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = null
        )
        val state = UpdateTemplateState(template)

        templatePropertiesUpdater(command, state)
    }

    @Test
    fun `Given a template update command, when properties are set, it validates each property`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand()
        val state = UpdateTemplateState(
            template = template,
            statements = listOf(createStatement(subject = createResource(template.id))).groupBy { it.subject.id }
        )

        every {
            abstractTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = command.properties!!,
                oldProperties = state.template!!.properties,
                statements = state.statements
            )
        } just runs

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = command.properties!!,
                oldProperties = state.template!!.properties,
                statements = state.statements
            )
        }
    }
}
