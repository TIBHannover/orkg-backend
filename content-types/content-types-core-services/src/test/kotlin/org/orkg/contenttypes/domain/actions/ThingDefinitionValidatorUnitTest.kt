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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createResource

@Nested
class ThingDefinitionValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()

    private val thingDefinitionValidator = object : ThingDefinitionValidator(thingRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given paper contents, when valid, it returns success`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("C2000"))
                )
            ),
            literals = mapOf(
                "#temp2" to LiteralDefinition(
                    label = "0.1",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinition(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinition(
                    label = "list",
                    elements = listOf("R123", "#temp1")
                )
            ),
            contributions = emptyList()
        )

        val r123 = createResource(id = ThingId("R123"))
        val c2000 = createClass(id = ThingId("C2000"))

        every { thingRepository.findByThingId(r123.id) } returns Optional.of(r123)
        every { thingRepository.findByThingId(c2000.id) } returns Optional.of(c2000)

        val validatedIds = mutableMapOf<String, Either<String, Thing>>()
        val tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4")

        thingDefinitionValidator.validateThingDefinitions(
            thingDefinitions = contents,
            tempIds = tempIds,
            validatedIds = validatedIds
        )

        validatedIds shouldBe mapOf(
            "#temp1" to Either.left("#temp1"),
            "R123" to Either.right(r123),
            "C2000" to Either.right(c2000)
        )

        verify(exactly = 1) { thingRepository.findByThingId(r123.id) }
        verify(exactly = 1) { thingRepository.findByThingId(c2000.id) }
    }

    @Test
    fun `Given paper contents, when specified class id for resource is not resolvable, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("R2000"))
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        every { thingRepository.findByThingId(any()) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for resource does not resolve to a class, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("R2000"))
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )
        val resource = createResource()

        every { thingRepository.findByThingId(any()) } returns Optional.of(resource)

        assertThrows<ThingIsNotAClass> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for a resource is reserved, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(reservedClassIds.first())
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<ReservedClass> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of resource is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "\n"
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of literal is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "a".repeat(MAX_LABEL_LENGTH + 1)
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralLabel> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of predicate is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = mapOf(
                "#temp1" to PredicateDefinition(
                    label = "\n"
                )
            ),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of list is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = mapOf(
                "#temp1" to ListDefinition(
                    label = "\n"
                )
            ),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    // TODO: implement test "Given paper contents, when label of class is invalid, it throws an exception",
    //       when implementation for ThingDefinitions with support for classes exists
}
