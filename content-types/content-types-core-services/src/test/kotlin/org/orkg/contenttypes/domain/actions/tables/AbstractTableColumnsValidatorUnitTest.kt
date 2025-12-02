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
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
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
        val temp1 = CreateLiteralCommandPart("header1")
        val thingCommands = mapOf<String, CreateThingCommandPart>(
            "#temp1" to temp1
        )
        val rows = listOf(
            CreateRowCommand(
                label = "header",
                data = listOf("L123", "L456", "#temp1")
            )
        )
        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "L123" to Either.right(l123),
            "#temp1" to Either.left(temp1)
        )

        every { thingRepository.findById(ThingId("L456")) } returns Optional.of(l456)

        val result = abstractTableColumnsValidator.validate(rows, thingCommands, validationCache)

        result shouldBe mapOf(
            "L123" to Either.right(l123),
            "L456" to Either.right(l456),
            "#temp1" to Either.left(temp1)
        )

        verify(exactly = 1) { thingRepository.findById(ThingId("L456")) }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and temp id cannot be resolved, it throws an exception`() {
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val rows = listOf(
            CreateRowCommand(
                label = "header",
                data = listOf("#temp1")
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        shouldThrow<ThingNotDefined> {
            abstractTableColumnsValidator.validate(rows, thingCommands, validationCache)
        }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and thing id cannot be resolved, it throws an exception`() {
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val rows = listOf(
            CreateRowCommand(
                label = "header",
                data = listOf("L123")
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(ThingId("L123")) } returns Optional.empty()

        shouldThrow<ThingNotFound> {
            abstractTableColumnsValidator.validate(rows, thingCommands, validationCache)
        }

        verify(exactly = 1) { thingRepository.findById(ThingId("L123")) }
    }

    @ParameterizedTest
    @MethodSource("nonLiteralThing")
    fun `Given a list of table rows, when validating column definitions and header row contains a non-literal thing, it throws an exception`(thing: Thing) {
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val rows = listOf(
            CreateRowCommand(
                label = "header",
                data = listOf(thing.id.value)
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(thing.id) } returns Optional.of(thing)

        shouldThrow<TableHeaderValueMustBeLiteral> {
            abstractTableColumnsValidator.validate(rows, thingCommands, validationCache)
        }

        verify(exactly = 1) { thingRepository.findById(thing.id) }
    }

    @Test
    fun `Given a list of table rows, when validating column definitions and header row contains a non-string literal, it throws an exception`() {
        val literal = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val rows = listOf(
            CreateRowCommand(
                label = "header",
                data = listOf(literal.id.value)
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrow<TableHeaderValueMustBeLiteral> {
            abstractTableColumnsValidator.validate(rows, thingCommands, validationCache)
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
