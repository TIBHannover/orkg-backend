package org.orkg.contenttypes.domain.actions

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
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate

class AbstractTemplatePropertyValidatorUnitTest {
    private val predicateRepository: PredicateRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val abstractTemplatePropertyValidator = object : AbstractTemplatePropertyValidator(predicateRepository, classRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(predicateRepository, classRepository)
    }

    @Test
    fun `Given a literal template property definition, when validating, it returns success`() {
        val property = dummyCreateLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a resource template property definition, when validating, it returns success`() {
        val property = dummyCreateResourceTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.`class`) } returns Optional.of(createClass(property.`class`))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.`class`) }
    }

    @Test
    fun `Given a template property definition, when min count is invalid, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand().copy(minCount = -1)
        val exception = InvalidMinCount(property.minCount!!)

        assertThrows<InvalidMinCount> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when max count is invalid, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand().copy(maxCount = -1)
        val exception = InvalidMaxCount(property.maxCount!!)

        assertThrows<InvalidMaxCount> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when max count is lower than min count, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand().copy(minCount = 2, maxCount = 1)
        val exception = InvalidCardinality(property.minCount!!, property.maxCount!!)

        assertThrows<InvalidCardinality> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when regex pattern is invalid, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand().copy(pattern = "\\")
        val exception = InvalidRegexPattern(property.pattern!!, RuntimeException())

        assertThrows<InvalidRegexPattern> { abstractTemplatePropertyValidator.validate(property) }.message shouldBe exception.message
    }

    @Test
    fun `Given a template property definition, when path predicate does not exist, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.empty()

        assertThrows<PredicateNotFound> { abstractTemplatePropertyValidator.validate(property) }

        verify(exactly = 1) { predicateRepository.findById(property.path) }
    }

    @Test
    fun `Given a literal template property definition, when datatype does not exist, it throws an exception`() {
        val property = dummyCreateLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.empty()

        assertThrows<ClassNotFound> { abstractTemplatePropertyValidator.validate(property) }

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a resource template property definition, when class does not exist, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.`class`) } returns Optional.empty()

        assertThrows<ClassNotFound> { abstractTemplatePropertyValidator.validate(property) }

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.`class`) }
    }

    @Test
    fun `Given a template property definition, when label is invalid, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand().copy(label = "\n")
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }
    }

    @Test
    fun `Given a template property definition, when placeholder is invalid, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand().copy(placeholder = "\n")
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }
    }

    @Test
    fun `Given a template property definition, when description is invalid, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand().copy(description = "\n")
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }
    }
}
