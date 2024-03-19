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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyDeleter
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyDefinition

class TemplatePropertiesUpdaterUnitTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater = mockk()
    private val abstractTemplatePropertyDeleter: AbstractTemplatePropertyDeleter = mockk()

    private val templatePropertiesUpdater = TemplatePropertiesUpdater(
        abstractTemplatePropertyCreator, abstractTemplatePropertyUpdater, abstractTemplatePropertyDeleter
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            abstractTemplatePropertyCreator,
            abstractTemplatePropertyUpdater,
            abstractTemplatePropertyDeleter
        )
    }

    @Test
    fun `Given a template update command, when properties are not set, it does nothing`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = null
        )
        val state = UpdateTemplateState(
            template = template
        )

        templatePropertiesUpdater(command, state)
    }

    @Test
    fun `Given a template update command, when properties are unchanged, it does nothing`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = template.properties.map { it.toTemplatePropertyDefinition() }
        )
        val state = UpdateTemplateState(
            template = template
        )

        templatePropertiesUpdater(command, state)
    }

    @Test
    fun `Given a template update command, when a property is removed, it deletes the old property`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = template.properties.take(1).map { it.toTemplatePropertyDefinition() }
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[1].id)
        } just runs

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[1].id)
        }
    }

    @Test
    fun `Given a template update command, when a property is removed, it deletes the old property and updates the order of the following properties`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = template.properties.drop(1).map { it.toTemplatePropertyDefinition() }
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = 1,
                newProperty = command.properties!![0],
                oldProperty = template.properties[1]
            )
        } just runs
        every {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[0].id)
        } just runs

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = 1,
                newProperty = command.properties!![0],
                oldProperty = template.properties[1]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[0].id)
        }
    }

    @Test
    fun `Given a template update command, when a property is added, it creates a new property`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = template.properties.map { it.toTemplatePropertyDefinition() } + dummyCreateLiteralTemplatePropertyCommand()
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 3,
                property = command.properties!![2]
            )
        } returns ThingId("irrelevant")

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 3,
                property = command.properties!![2]
            )
        }
    }

    @Test
    fun `Given a template update command, when a property is added, it creates a new property and updates the order of the following properties`() {
        val template = createDummyTemplate().copy(
            properties = listOf(createDummyLiteralTemplateProperty())
        )
        val command = dummyUpdateTemplateCommand().copy(
            properties = listOf(dummyUpdateResourceTemplatePropertyCommand(), template.properties.single().toTemplatePropertyDefinition())
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 1,
                property = command.properties!![0]
            )
        } returns ThingId("irrelevant")
        every {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = 2,
                newProperty = command.properties!![1],
                oldProperty = template.properties[0]
            )
        } just runs

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 1,
                property = command.properties!![0]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = 2,
                newProperty = command.properties!![1],
                oldProperty = template.properties[0]
            )
        }
    }

    @Test
    fun `Given a template update command, when a property is replaced, it deletes the old property and creates a new one`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            properties = listOf(template.properties.first().toTemplatePropertyDefinition(), dummyUpdateResourceTemplatePropertyCommand())
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 2,
                property = command.properties!![1]
            )
        } returns ThingId("irrelevant")
        every {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[1].id)
        } just runs

        templatePropertiesUpdater(command, state)

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = 2,
                property = command.properties!![1]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, template.properties[1].id)
        }
    }
}
