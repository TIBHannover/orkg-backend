package org.orkg.contenttypes.domain.actions.templates

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplatePropertiesUpdaterUnitTest : MockkBaseTest {
    private val abstractTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater = mockk()

    private val templatePropertiesUpdater = TemplatePropertiesUpdater(abstractTemplatePropertiesUpdater)

    @Test
    fun `Given a template update command, when properties are not set, it does nothing`() {
        val template = createTemplate()
        val command = updateTemplateCommand().copy(
            properties = null
        )
        val state = UpdateTemplateState(template)

        templatePropertiesUpdater(command, state)
    }

    @Test
    fun `Given a template update command, when properties are set, it validates each property`() {
        val template = createTemplate()
        val command = updateTemplateCommand()
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
