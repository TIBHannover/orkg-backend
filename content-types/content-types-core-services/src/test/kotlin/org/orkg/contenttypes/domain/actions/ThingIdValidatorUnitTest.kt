package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ThingIdValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val thingIdValidator = ThingIdValidator(thingRepository)

    @Test
    fun `Given a temp id, when id is not in cache and a corresponding thing command exists, it puts the command in the validation cache and returns it`() {
        val id = "#temp"
        val command = CreateResourceCommandPart("label")
        val thingCommands = mapOf(
            id to command
        )
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>()

        thingIdValidator.validate(id, thingCommands, validationCache) shouldBe Either.left(command)

        validationCache shouldBe mapOf(id to Either.left(command))
    }

    @Test
    fun `Given a temp id, when id is in cache, it returns the cached value`() {
        val id = "#temp"
        val command = CreateResourceCommandPart("label")
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>(
            id to Either.left(command)
        )

        thingIdValidator.validate(id, thingCommands, validationCache) shouldBe Either.left(command)

        validationCache shouldBe mapOf(id to Either.left(command))
    }

    @Test
    fun `Given a temp id, when id is not in cache and there is no corresponding thing command, it throws an exception`() {
        val id = "#temp"
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>()

        shouldThrow<ThingNotDefined> { thingIdValidator.validate(id, thingCommands, validationCache) }

        validationCache shouldBe emptyMap()
    }

    @Test
    fun `Given a thing id, when id is not in cache and thing exists, it puts the thing in the validation cache and returns it`() {
        val thing = createResource()
        val id = thing.id.value
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(ThingId(id)) } returns Optional.of(thing)

        thingIdValidator.validate(id, thingCommands, validationCache) shouldBe Either.right(thing)

        validationCache shouldBe mapOf(id to Either.right(thing))

        verify(exactly = 1) { thingRepository.findById(ThingId(id)) }
    }

    @Test
    fun `Given a thing id, when id is in cache, it returns the cached value`() {
        val thing = createResource()
        val id = thing.id.value
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>(
            id to Either.right(thing)
        )

        thingIdValidator.validate(id, thingCommands, validationCache) shouldBe Either.right(thing)

        validationCache shouldBe mapOf(id to Either.right(thing))
    }

    @Test
    fun `Given a thing id, when id is not in cache and thing cannot be found, it throws an exception`() {
        val id = "R1"
        val thingCommands = emptyMap<String, CreateThingCommandPart>()
        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(ThingId(id)) } returns Optional.empty()

        shouldThrow<ThingNotFound> { thingIdValidator.validate(id, thingCommands, validationCache) }

        validationCache shouldBe emptyMap()

        verify(exactly = 1) { thingRepository.findById(ThingId(id)) }
    }
}
