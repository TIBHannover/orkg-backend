package org.orkg.contenttypes.domain.actions.rosettastone.templates

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
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class RosettaStoneTemplatePropertiesUpdaterUnitTest {
    private val abstractTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater = mockk()

    private val rosettaStoneTemplatePropertiesUpdater = RosettaStoneTemplatePropertiesUpdater(abstractTemplatePropertiesUpdater)

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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = null
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        rosettaStoneTemplatePropertiesUpdater(command, state)
    }

    @Test
    fun `Given a template update command, when properties are set, it validates each property`() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            statements = listOf(createStatement(subject = createResource(rosettaStoneTemplate.id))).groupBy { it.subject.id }
        )

        every {
            abstractTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = command.properties!!,
                oldProperties = state.rosettaStoneTemplate!!.properties,
                statements = state.statements
            )
        } just runs

        rosettaStoneTemplatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = command.properties!!,
                oldProperties = state.rosettaStoneTemplate!!.properties,
                statements = state.statements
            )
        }
    }
}
