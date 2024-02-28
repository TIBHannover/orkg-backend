package org.orkg.contenttypes.domain.actions.template.property

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.TemplatePropertyState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class TemplatePropertyTemplateValidatorUnitTest {
    private val statementRepository: StatementRepository = mockk()

    private val templatePropertyTemplateValidator = TemplatePropertyTemplateValidator(statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementRepository)
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when validating template metadata, it returns success`(command: CreateTemplatePropertyCommand) {
        val state = TemplatePropertyState()
        val template = createResource(id = command.templateId, classes = setOf(Classes.nodeShape))

        every {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = template,
                predicate = createPredicate(Predicates.shClosed),
                `object` = createLiteral(label = "false", datatype = Literals.XSD.BOOLEAN.prefixedUri)
            ),
            createStatement(
                subject = template,
                predicate = createPredicate(Predicates.shProperty),
                `object` = createResource(classes = setOf(Classes.propertyShape))
            )
        )

        val result = templatePropertyTemplateValidator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe null
            it.propertyCount shouldBe 1
        }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        }
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when the template has no statements, it returns success`(command: CreateTemplatePropertyCommand) {
        val state = TemplatePropertyState()

        every {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        } returns pageOf()

        val result = templatePropertyTemplateValidator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe null
            it.propertyCount shouldBe 0
        }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        }
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when template is closed, it throws an exception`(command: CreateTemplatePropertyCommand) {
        val state = TemplatePropertyState()
        val template = createResource(id = command.templateId, classes = setOf(Classes.nodeShape))

        every {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = template,
                predicate = createPredicate(Predicates.shClosed),
                `object` = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
            )
        )

        assertThrows<TemplateClosed> { templatePropertyTemplateValidator(command, state) }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.templateId,
                pageable = PageRequests.ALL
            )
        }
    }

    companion object {
        @JvmStatic
        fun createTemplatePropertyCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(dummyCreateLiteralTemplatePropertyCommand()),
            Arguments.of(dummyCreateResourceTemplatePropertyCommand())
        )
    }
}
