package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.ResearchProblemNotFound
import org.orkg.contenttypes.domain.TemplateRelations
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.TemplateRelationsCommand
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class TemplateRelationsUpdateValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()

    private val templateRelationsUpdateValidator = TemplateRelationsUpdateValidator(resourceRepository, predicateRepository)

    @Test
    fun `Given a template update command, when validating its relations, it returns success`() {
        val command = updateTemplateCommand()
        val state = UpdateTemplateState(template = createTemplate())

        every {
            resourceRepository.findById(command.relations!!.researchFields.first())
        } returns Optional.of(createResource(classes = setOf(Classes.researchField)))
        every {
            resourceRepository.findById(command.relations!!.researchProblems.first())
        } returns Optional.of(createResource(classes = setOf(Classes.problem)))
        every {
            predicateRepository.findById(command.relations!!.predicate!!)
        } returns Optional.of(createPredicate())

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceRepository.findById(command.relations!!.researchFields.first()) }
        verify(exactly = 1) { resourceRepository.findById(command.relations!!.researchProblems.first()) }
        verify(exactly = 1) { predicateRepository.findById(command.relations!!.predicate!!) }
    }

    @Test
    fun `Given a template update command, when relations are null, it returns success`() {
        val command = updateTemplateCommand().copy(relations = null)
        val state = UpdateTemplateState(template = createTemplate())

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }

    @Test
    fun `Given a template update command, when relations are empty, it returns success`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand()
        )
        val state = UpdateTemplateState(template = createTemplate())

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }

    @Test
    fun `Given a template update command, when related research field is missing, it throws an exception`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = listOf(ThingId("R24"))
            )
        )
        val state = UpdateTemplateState(template = createTemplate())

        every {
            resourceRepository.findById(command.relations!!.researchFields.first())
        } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { templateRelationsUpdateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.relations!!.researchFields.first()) }
    }

    @Test
    fun `Given a template update command, when related research problem is missing, it throws an exception`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchProblems = listOf(ThingId("R29"))
            )
        )
        val state = UpdateTemplateState(template = createTemplate())

        every {
            resourceRepository.findById(command.relations!!.researchProblems.first())
        } returns Optional.empty()

        assertThrows<ResearchProblemNotFound> { templateRelationsUpdateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.relations!!.researchProblems.first()) }
    }

    @Test
    fun `Given a template update command, when related predicate is missing, it throws an exception`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = emptyList(),
                researchProblems = emptyList(),
                predicate = ThingId("P23")
            )
        )
        val state = UpdateTemplateState(template = createTemplate())

        every {
            predicateRepository.findById(command.relations!!.predicate!!)
        } returns Optional.empty()

        assertThrows<PredicateNotFound> { templateRelationsUpdateValidator(command, state) }

        verify(exactly = 1) { predicateRepository.findById(command.relations!!.predicate!!) }
    }

    @Test
    fun `Given a template update command, when validating its relations, it only checks for previously unknown research fields`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = listOf(ThingId("R123"), ThingId("R789"))
            )
        )
        val state = UpdateTemplateState(
            template = createTemplate().copy(
                relations = TemplateRelations(
                    researchFields = listOf(
                        ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                        ObjectIdAndLabel(ThingId("R456"), "irrelevant")
                    )
                )
            )
        )

        every {
            resourceRepository.findById(ThingId("R789"))
        } returns Optional.of(createResource(classes = setOf(Classes.researchField)))

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R789")) }
    }

    @Test
    fun `Given a template update command, when validating its relations, it only checks for previously unknown research problems`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchProblems = listOf(ThingId("R123"), ThingId("R789"))
            )
        )
        val state = UpdateTemplateState(
            template = createTemplate().copy(
                relations = TemplateRelations(
                    researchProblems = listOf(
                        ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                        ObjectIdAndLabel(ThingId("R456"), "irrelevant")
                    )
                )
            )
        )

        every {
            resourceRepository.findById(ThingId("R789"))
        } returns Optional.of(createResource(classes = setOf(Classes.problem)))

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R789")) }
    }

    @Test
    fun `Given a template update command, when validating its relations, it only checks for previously unknown predicates`() {
        val command = updateTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                predicate = ThingId("R123")
            )
        )
        val state = UpdateTemplateState(
            template = createTemplate().copy(
                relations = TemplateRelations(
                    predicate = ObjectIdAndLabel(ThingId("R123"), "irrelevant")
                )
            )
        )

        val result = templateRelationsUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }
}
