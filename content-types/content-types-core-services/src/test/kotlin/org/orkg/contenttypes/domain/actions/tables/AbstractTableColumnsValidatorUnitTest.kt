package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.RowDefinition
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional
import java.util.stream.Stream

internal class AbstractTableColumnsValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val abstractTableColumnsValidator = AbstractTableColumnsValidator(thingRepository)

    @Test
    fun `Given a list of table rows, when validating column definitions, it returns success`() {
        val l123 = createLiteral(ThingId("L123"))
        val l456 = createLiteral(ThingId("L456"))
        val thingDefinitions = mapOf<String, ThingDefinition>(
            "#temp1" to LiteralDefinition("header1")
        )
        val rows = listOf(
            RowDefinition(
                label = "header",
                data = listOf("L123", "L456", "#temp1")
            )
        )
        val tempIds = setOf("#temp2", "#temp3")
        val validatedIds = mapOf<String, Either<String, Thing>>(
            "L123" to Either.right(l123),
            "#temp1" to Either.left("#temp1")
        )

        every { thingRepository.findById(ThingId("L456")) } returns Optional.of(l456)

        val result = abstractTableColumnsValidator.validate(thingDefinitions, rows, tempIds, validatedIds)

        result shouldBe mapOf(
            "L123" to Either.right(l123),
            "L456" to Either.right(l456),
            "#temp1" to Either.left("#temp1")
        )

        verify(exactly = 1) { thingRepository.findById(ThingId("L456")) }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and temp id cannot be resolved, it throws an exception`() {
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val rows = listOf(
            RowDefinition(
                label = "header",
                data = listOf("#temp1")
            )
        )
        val tempIds = emptySet<String>()
        val validatedIds = emptyMap<String, Either<String, Thing>>()

        shouldThrow<ThingNotDefined> {
            abstractTableColumnsValidator.validate(thingDefinitions, rows, tempIds, validatedIds)
        }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and thing id cannot be resolved, it throws an exception`() {
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val rows = listOf(
            RowDefinition(
                label = "header",
                data = listOf("L123")
            )
        )
        val tempIds = emptySet<String>()
        val validatedIds = emptyMap<String, Either<String, Thing>>()

        every { thingRepository.findById(ThingId("L123")) } returns Optional.empty()

        shouldThrow<ThingNotFound> {
            abstractTableColumnsValidator.validate(thingDefinitions, rows, tempIds, validatedIds)
        }

        verify(exactly = 1) { thingRepository.findById(ThingId("L123")) }
    }

    @ParameterizedTest
    @MethodSource("nonLiteralThing")
    fun `Given a list of table rows, when validating column definitions and header row contains a non-literal thing, it throws an exception`(thing: Thing) {
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val rows = listOf(
            RowDefinition(
                label = "header",
                data = listOf(thing.id.value)
            )
        )
        val tempIds = emptySet<String>()
        val validatedIds = emptyMap<String, Either<String, Thing>>()

        every { thingRepository.findById(thing.id) } returns Optional.of(thing)

        shouldThrow<TableHeaderValueMustBeLiteral> {
            abstractTableColumnsValidator.validate(thingDefinitions, rows, tempIds, validatedIds)
        }

        verify(exactly = 1) { thingRepository.findById(thing.id) }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and header row contains a non-string literal, it throws an exception`() {
        val literal = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
        val thingDefinitions = emptyMap<String, ThingDefinition>()
        val rows = listOf(
            RowDefinition(
                label = "header",
                data = listOf(literal.id.value)
            )
        )
        val tempIds = emptySet<String>()
        val validatedIds = emptyMap<String, Either<String, Thing>>()

        every { thingRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrow<TableHeaderValueMustBeLiteral> {
            abstractTableColumnsValidator.validate(thingDefinitions, rows, tempIds, validatedIds)
        }

        verify(exactly = 1) { thingRepository.findById(literal.id) }
    }

    companion object {
        @JvmStatic
        fun nonLiteralThing(): Stream<Thing> = Stream.of(
            createResource(),
            createPredicate(),
            createClass(),
        )
    }
}
