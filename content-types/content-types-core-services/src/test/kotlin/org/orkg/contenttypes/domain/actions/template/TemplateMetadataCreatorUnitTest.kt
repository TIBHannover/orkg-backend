package org.orkg.contenttypes.domain.actions.template

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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateMetadataCreatorUnitTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templateResourceCreator = TemplateMetadataCreator(literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literalService, statementService)
    }

    @Test
    fun `Given a template create command, when creating the template metadata, it returns success`() {
        val command = dummyCreateTemplateCommand()
        val templateId = ThingId("R123")
        val state = TemplateState(
            templateId = templateId
        )
        val descriptionLiteralId = ThingId("R124")
        val closedLiteralId = ThingId("R125")

        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.description!!
                )
            )
        } returns descriptionLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.description,
                `object` = descriptionLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
        } returns closedLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shClosed,
                `object` = closedLiteralId
            )
        } just runs

        val result = templateResourceCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.description!!
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.description,
                `object` = descriptionLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shClosed,
                `object` = closedLiteralId
            )
        }
    }

    @Test
    fun `Given a template create command, when creating the template metadata, it does not create null values`() {
        val command = dummyCreateTemplateCommand().copy(
            description = null,
            isClosed = false
        )
        val templateId = ThingId("R123")
        val state = TemplateState(
            templateId = templateId
        )

        val result = templateResourceCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
