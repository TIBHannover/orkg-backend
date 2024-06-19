package org.orkg.contenttypes.domain.actions

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
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDatatype
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateUntypedTemplatePropertyCommand
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate

class AbstractTemplatePropertyValidatorUnitTest {
    private val predicateRepository: PredicateRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val abstractTemplatePropertyValidator = AbstractTemplatePropertyValidator(predicateRepository, classRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(predicateRepository, classRepository)
    }

    @Test
    fun `Given an untyped template property definition, when validating, it returns success`() {
        val property = dummyCreateUntypedTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
    }

    @Test
    fun `Given a literal template property definition, when validating, it returns success`() {
        val property = dummyCreateOtherLiteralTemplatePropertyCommand()

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
        val property = dummyCreateOtherLiteralTemplatePropertyCommand().copy(minCount = -1)
        val exception = InvalidMinCount(property.minCount!!)

        assertThrows<InvalidMinCount> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when max count is invalid, it throws an exception`() {
        val property = dummyCreateOtherLiteralTemplatePropertyCommand().copy(maxCount = -1)
        val exception = InvalidMaxCount(property.maxCount!!)

        assertThrows<InvalidMaxCount> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when max count is lower than min count, it throws an exception`() {
        val property = dummyCreateOtherLiteralTemplatePropertyCommand().copy(minCount = 2, maxCount = 1)
        val exception = InvalidCardinality(property.minCount!!, property.maxCount!!)

        assertThrows<InvalidCardinality> { abstractTemplatePropertyValidator.validate(property) } shouldBe exception
    }

    @Test
    fun `Given a template property definition, when max count is zero and min count is more than zero, it returns success`() {
        val property = dummyCreateUntypedTemplatePropertyCommand().copy(minCount = 1, maxCount = 0)

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
    }

    @Test
    fun `Given a template property definition, when path predicate does not exist, it throws an exception`() {
        val property = dummyCreateOtherLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.empty()

        assertThrows<PredicateNotFound> { abstractTemplatePropertyValidator.validate(property) }

        verify(exactly = 1) { predicateRepository.findById(property.path) }
    }

    @Test
    fun `Given a literal template property definition, when datatype does not exist, it throws an exception`() {
        val property = dummyCreateOtherLiteralTemplatePropertyCommand()

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
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }.asClue {
            it.property shouldBe "label"
        }
    }

    @Test
    fun `Given a template property definition, when placeholder is invalid, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand().copy(placeholder = "\n")
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }.asClue {
            it.property shouldBe "placeholder"
        }
    }

    @Test
    fun `Given a template property definition, when description is invalid, it throws an exception`() {
        val property = dummyCreateResourceTemplatePropertyCommand().copy(description = "\n")
        assertThrows<InvalidLabel> { abstractTemplatePropertyValidator.validate(property) }.asClue {
            it.property shouldBe "description"
        }
    }

    @Test
    fun `Given a string template property definition, when datatype does not match xsd string, it throws an exception`() {
        val property = dummyCreateStringLiteralTemplatePropertyCommand().copy(datatype = Classes.integer)
        val exception = InvalidDatatype(property.datatype, Classes.string)

        assertThrows<InvalidDatatype> { abstractTemplatePropertyValidator.validate(property) }.message shouldBe exception.message
    }

    @Test
    fun `Given a string template property definition, when regex pattern is invalid, it throws an exception`() {
        val property = dummyCreateStringLiteralTemplatePropertyCommand().copy(pattern = "\\")
        val exception = InvalidRegexPattern(property.pattern!!, RuntimeException())

        assertThrows<InvalidRegexPattern> { abstractTemplatePropertyValidator.validate(property) }.message shouldBe exception.message
    }

    @Test
    fun `Given a string template property definition, when regex pattern is valid, it returns success`() {
        val property = dummyCreateStringLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a string template property definition, when regex pattern is null, it returns success`() {
        val property = dummyCreateStringLiteralTemplatePropertyCommand().copy(pattern = null)

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a number template property definition, when datatype does not match xsd integer, decimal or float, it throws an exception`() {
        val property = dummyCreateNumberLiteralTemplatePropertyCommand().copy(datatype = Classes.string)
        val exception = InvalidDatatype(property.datatype, Classes.integer, Classes.decimal, Classes.float, Classes.double)

        assertThrows<InvalidDatatype> { abstractTemplatePropertyValidator.validate(property) }.message shouldBe exception.message
    }

    @Test
    fun `Given a number template property definition, when bounds are valid, it returns success`() {
        val property = dummyCreateNumberLiteralTemplatePropertyCommand()

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a number template property definition, when min bound is null, it returns success`() {
        val property = dummyCreateNumberLiteralTemplatePropertyCommand().copy(minInclusive = null)

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a number template property definition, when max bound is null, it returns success`() {
        val property = dummyCreateNumberLiteralTemplatePropertyCommand().copy(minInclusive = null)

        every { predicateRepository.findById(property.path) } returns Optional.of(createPredicate(property.path))
        every { classRepository.findById(property.datatype) } returns Optional.of(createClass(property.datatype))

        abstractTemplatePropertyValidator.validate(property)

        verify(exactly = 1) { predicateRepository.findById(property.path) }
        verify(exactly = 1) { classRepository.findById(property.datatype) }
    }

    @Test
    fun `Given a number template property definition, when min bound is higher than max bound, it throws an exception`() {
        val property = dummyCreateNumberLiteralTemplatePropertyCommand().copy(minInclusive = 5, maxInclusive = 2)
        val exception = InvalidBounds(5, 2)

        assertThrows<InvalidBounds> { abstractTemplatePropertyValidator.validate(property) }.message shouldBe exception.message
    }
}
