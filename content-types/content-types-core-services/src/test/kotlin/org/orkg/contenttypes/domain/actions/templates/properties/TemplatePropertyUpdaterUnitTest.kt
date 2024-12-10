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
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.OtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createUntypedTemplateProperty
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplatePropertyUpdaterUnitTest {
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
    // Untyped property
    //

    @Test
    fun `Given an update untyped template property command, when contents are equal, it does nothing`() {
        val template = createTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createUntypedTemplateProperty()
        val command = property.toUpdateUntypedTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }
    }

    @Test
    fun `Given an update untyped template property command, when contents have changed, it updates the property`() {
        val template = createTemplate()
        val property = createUntypedTemplateProperty()
        val command = property.toUpdateUntypedTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        every { abstractTemplatePropertyUpdater.update(any(), any(), any(), any(), any()) } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = state.statements[property.id].orEmpty(),
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    //
    // String literal property
    //

    @Test
    fun `Given an update string literal template property command, when contents are equal, it does nothing`() {
        val template = createTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createStringLiteralTemplateProperty()
        val command = property.toUpdateStringLiteralTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }
    }

    @Test
    fun `Given an update string literal template property command, when contents have changed, it updates the property`() {
        val template = createTemplate()
        val property = createStringLiteralTemplateProperty()
        val command = property.toUpdateStringLiteralTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        every { abstractTemplatePropertyUpdater.update(any(), any(), any(), any(), any()) } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = state.statements[property.id].orEmpty(),
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    //
    // Number literal property
    //

    @Test
    fun `Given an update number literal template property command, when contents are equal, it does nothing`() {
        val template = createTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createNumberLiteralTemplateProperty()
        val command = property.toUpdateNumberLiteralTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }
    }

    @Test
    fun `Given an update number literal template property command, when contents have changed, it updates the property`() {
        val template = createTemplate()
        val property = createNumberLiteralTemplateProperty()
        val command = property.toUpdateNumberLiteralTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        every { abstractTemplatePropertyUpdater.update(any(), any(), any(), any(), any()) } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = state.statements[property.id].orEmpty(),
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    //
    // Literal property
    //

    @Test
    fun `Given an update literal template property command, when contents are equal, it does nothing`() {
        val template = createTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createOtherLiteralTemplateProperty()
        val command = property.toUpdateOtherLiteralTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }
    }

    @Test
    fun `Given an update literal template property command, when contents have changed, it updates the property`() {
        val template = createTemplate()
        val property = createOtherLiteralTemplateProperty()
        val command = property.toUpdateOtherLiteralTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        every { abstractTemplatePropertyUpdater.update(any(), any(), any(), any(), any()) } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = state.statements[property.id].orEmpty(),
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
        val template = createTemplate()
        val contributorId = ContributorId(UUID.randomUUID())
        val property = createResourceTemplateProperty()
        val command = property.toUpdateResourceTemplatePropertyCommand(contributorId, template.id)
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }
    }

    @Test
    fun `Given an update resource template property command, when contents have changed, it updates the property`() {
        val template = createTemplate()
        val property = createResourceTemplateProperty()
        val command = property.toUpdateResourceTemplatePropertyCommand(ContributorId(UUID.randomUUID()), template.id).copy(
            label = "updated label"
        )
        val state = UpdateTemplatePropertyState(
            template = template,
            templateProperty = property,
            statements = mapOf(property.id to listOf(createStatement()))
        )

        every { abstractTemplatePropertyUpdater.update(any(), any(), any(), any(), any()) } just runs

        val result = templatePropertyUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateProperty shouldBe state.templateProperty
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractTemplatePropertyUpdater.update(
                statements = state.statements[property.id].orEmpty(),
                contributorId = command.contributorId,
                order = property.order.toInt(),
                newProperty = command,
                oldProperty = property
            )
        }
    }

    private fun UntypedTemplateProperty.toUpdateUntypedTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateUntypedPropertyCommand = UpdateUntypedPropertyCommand(
        templatePropertyId = id,
        contributorId = contributorId,
        templateId = templateId,
        label = label,
        placeholder = placeholder,
        description = description,
        minCount = minCount,
        maxCount = maxCount,
        path = path.id
    )

    private fun StringLiteralTemplateProperty.toUpdateStringLiteralTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateStringLiteralPropertyCommand = UpdateStringLiteralPropertyCommand(
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

    private fun NumberLiteralTemplateProperty.toUpdateNumberLiteralTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateNumberLiteralPropertyCommand = UpdateNumberLiteralPropertyCommand(
        templatePropertyId = id,
        contributorId = contributorId,
        templateId = templateId,
        label = label,
        placeholder = placeholder,
        description = description,
        minCount = minCount,
        maxCount = maxCount,
        minInclusive = minInclusive,
        maxInclusive = maxInclusive,
        path = path.id,
        datatype = datatype.id
    )

    private fun OtherLiteralTemplateProperty.toUpdateOtherLiteralTemplatePropertyCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateOtherLiteralPropertyCommand = UpdateOtherLiteralPropertyCommand(
        templatePropertyId = id,
        contributorId = contributorId,
        templateId = templateId,
        label = label,
        placeholder = placeholder,
        description = description,
        minCount = minCount,
        maxCount = maxCount,
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
        path = path.id,
        `class` = `class`.id
    )
}
