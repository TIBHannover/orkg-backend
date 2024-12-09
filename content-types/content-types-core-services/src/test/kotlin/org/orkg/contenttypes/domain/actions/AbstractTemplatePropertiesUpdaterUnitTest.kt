package org.orkg.contenttypes.domain.actions

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
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.testing.fixtures.createDummyOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyUntypedTemplateProperty
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyDefinition
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId

internal class AbstractTemplatePropertiesUpdaterUnitTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater = mockk()
    private val abstractTemplatePropertyDeleter: AbstractTemplatePropertyDeleter = mockk()

    private val abstractTemplatePropertiesUpdater = AbstractTemplatePropertiesUpdater(
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
    fun `Given a template update command, when properties are unchanged, it does nothing`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(MockUserId.USER)
        val properties = template.properties.map { it.toTemplatePropertyDefinition() }

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = emptyMap()
        )
    }

    @Test
    fun `Given a template update command, when a property is removed, it deletes the old property`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(MockUserId.USER)
        val properties = template.properties.dropLast(1).map { it.toTemplatePropertyDefinition() }

        every {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties.last().id)
        } just runs

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = emptyMap()
        )

        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties.last().id)
        }
    }

    @Test
    fun `Given a template update command, when a property is removed, it deletes the old property and updates the order of the following properties`() {
        val template = createDummyTemplate().copy(
            properties = listOf(
                createDummyUntypedTemplateProperty(),
                createDummyStringLiteralTemplateProperty()
            )
        )
        val contributorId = ContributorId(MockUserId.USER)
        val properties = template.properties.drop(1).map { it.toTemplatePropertyDefinition() }
        val statements = template.properties.associate { it.id to listOf(createStatement(subject = createResource(it.id))) }

        every {
            abstractTemplatePropertyUpdater.update(
                statements = any(),
                contributorId = contributorId,
                order = 0,
                newProperty = properties[0],
                oldProperty = template.properties[1]
            )
        } just runs
        every {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties[0].id)
        } just runs

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = statements
        )

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = statements[template.properties[1].id]!!,
                contributorId = contributorId,
                order = 0,
                newProperty = properties[0],
                oldProperty = template.properties[1]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties[0].id)
        }
    }

    @Test
    fun `Given a template update command, when a property is added, it creates a new property`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(MockUserId.USER)
        val properties = template.properties.map { it.toTemplatePropertyDefinition() } + dummyCreateOtherLiteralTemplatePropertyCommand()

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 5,
                property = properties[5]
            )
        } returns ThingId("irrelevant")

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = emptyMap()
        )

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 5,
                property = properties[5]
            )
        }
    }

    @Test
    fun `Given a template update command, when a property is added, it creates a new property and updates the order of the following properties`() {
        val template = createDummyTemplate().copy(
            properties = listOf(createDummyOtherLiteralTemplateProperty())
        )
        val contributorId = ContributorId(MockUserId.USER)
        val properties = listOf(dummyUpdateResourceTemplatePropertyCommand(), template.properties.single().toTemplatePropertyDefinition())
        val statements = template.properties.associate { it.id to listOf(createStatement(subject = createResource(it.id))) }

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 0,
                property = properties[0]
            )
        } returns ThingId("irrelevant")
        every {
            abstractTemplatePropertyUpdater.update(
                statements = any(),
                contributorId = contributorId,
                order = 1,
                newProperty = properties[1],
                oldProperty = template.properties[0]
            )
        } just runs

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = statements
        )

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 0,
                property = properties[0]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = statements[template.properties.single().id]!!,
                contributorId = contributorId,
                order = 1,
                newProperty = properties[1],
                oldProperty = template.properties[0]
            )
        }
    }

    @Test
    fun `Given a template update command, when a property is replaced, it deletes the old property and creates a new one`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(MockUserId.USER)
        val properties = template.properties.dropLast(1).map { it.toTemplatePropertyDefinition() } + dummyUpdateResourceTemplatePropertyCommand()

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 4,
                property = properties[4]
            )
        } returns ThingId("irrelevant")
        every {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties[4].id)
        } just runs

        abstractTemplatePropertiesUpdater.update(
            contributorId = contributorId,
            subjectId = template.id,
            newProperties = properties,
            oldProperties = template.properties,
            statements = emptyMap()
        )

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = contributorId,
                templateId = template.id,
                order = 4,
                property = properties[4]
            )
        }
        verify(exactly = 1) {
            abstractTemplatePropertyDeleter.delete(contributorId, template.id, template.properties[4].id)
        }
    }
}
