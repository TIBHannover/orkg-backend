package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand

class TemplatePropertyUpdaterUnitTest {
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater = mockk()

    private val templatePropertyUpdater = TemplatePropertyUpdater(abstractTemplatePropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertyUpdater)
    }

    //
    // Literal property
    //

    @Test
    fun `Given an update literal template property command, when contents are equal, it does nothing`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createDummyLiteralTemplateProperty()
        val command = property.toUpdateLiteralTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
        }
    }

    @Test
    fun `Given an update literal template property command, when contents have changed, it updates the property`() {
        val template = createDummyTemplate()
        val property = createDummyLiteralTemplateProperty()
        val command = property.toUpdateLiteralTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property
        )

        every {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    //
    // Resource property
    //

    @Test
    fun `Given an update resource template property command, when contents are equal, it does nothing`() {
        val template = createDummyTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createDummyResourceTemplateProperty()
        val command = property.toUpdateResourceTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
        }
    }

    @Test
    fun `Given an update resource template property command, when contents have changed, it updates the property`() {
        val template = createDummyTemplate()
        val property = createDummyResourceTemplateProperty()
        val command = property.toUpdateResourceTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property
        )

        every {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    private fun LiteralTemplateProperty.toUpdateLiteralTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateLiteralPropertyCommand = UpdateLiteralPropertyCommand(
        templatePropertyId = id,
        contributorId = contributorId,
        templateId = templateId,
        label = label,
        placeholder = placeholder,
        description = description,
        minCount = minCount,
        maxCount = maxCount,
        pattern = pattern,
        path = path.id,
        datatype = datatype.id
    )

    private fun ResourceTemplateProperty.toUpdateResourceTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateResourcePropertyCommand = UpdateResourcePropertyCommand(
        templatePropertyId = id,
        contributorId = contributorId,
        templateId = templateId,
        label = label,
        placeholder = placeholder,
        description = description,
        minCount = minCount,
        maxCount = maxCount,
        pattern = pattern,
        path = path.id,
        `class` = `class`.id
    )
}
