package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.ResearchProblemNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

class TemplateRelationsValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()

    private val templateRelationsValidator = TemplateRelationsValidator(resourceRepository, predicateRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, predicateRepository)
    }

    @Test
    fun `Given a template create command, when validating its relations, it returns success`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every {
            resourceRepository.findById(command.relations.researchFields.first())
        } returns Optional.of(createResource(classes = setOf(Classes.researchField)))
        every {
            resourceRepository.findById(command.relations.researchProblems.first())
        } returns Optional.of(createResource(classes = setOf(Classes.problem)))
        every {
            predicateRepository.findById(command.relations.predicate!!)
        } returns Optional.of(createPredicate())

        val result = templateRelationsValidator(command, state)

        result.asClue {
            it.templateId shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findById(command.relations.researchFields.first()) }
        verify(exactly = 1) { resourceRepository.findById(command.relations.researchProblems.first()) }
        verify(exactly = 1) { predicateRepository.findById(command.relations.predicate!!) }
    }

    @Test
    fun `Given a template create command, when related research field is missing, it throws an exception`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every {
            resourceRepository.findById(command.relations.researchFields.first())
        } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { templateRelationsValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.relations.researchFields.first()) }
    }

    @Test
    fun `Given a template create command, when related research problem is missing, it throws an exception`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every {
            resourceRepository.findById(command.relations.researchFields.first())
        } returns Optional.of(createResource(classes = setOf(Classes.researchField)))
        every {
            resourceRepository.findById(command.relations.researchProblems.first())
        } returns Optional.empty()

        assertThrows<ResearchProblemNotFound> { templateRelationsValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.relations.researchFields.first()) }
        verify(exactly = 1) { resourceRepository.findById(command.relations.researchProblems.first()) }
    }

    @Test
    fun `Given a template create command, when related predicate is missing, it throws an exception`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every {
            resourceRepository.findById(command.relations.researchFields.first())
        } returns Optional.of(createResource(classes = setOf(Classes.researchField)))
        every {
            resourceRepository.findById(command.relations.researchProblems.first())
        } returns Optional.of(createResource(classes = setOf(Classes.problem)))
        every {
            predicateRepository.findById(command.relations.predicate!!)
        } returns Optional.empty()

        assertThrows<PredicateNotFound> { templateRelationsValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.relations.researchFields.first()) }
        verify(exactly = 1) { resourceRepository.findById(command.relations.researchProblems.first()) }
        verify(exactly = 1) { predicateRepository.findById(command.relations.predicate!!) }
    }
}
