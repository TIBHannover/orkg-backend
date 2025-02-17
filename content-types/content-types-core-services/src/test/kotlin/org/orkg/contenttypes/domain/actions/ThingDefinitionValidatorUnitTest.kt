package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createResource

@Nested
internal class ThingDefinitionValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val thingDefinitionValidator = object : ThingDefinitionValidator(thingRepository, classRepository) {}

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

        every { thingRepository.findById(r123.id) } returns Optional.of(r123)
        every { thingRepository.findById(c2000.id) } returns Optional.of(c2000)

        val validatedIds = mutableMapOf<String, Either<String, Thing>>()
        val tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4")

        thingDefinitionValidator.validateThingDefinitionsInPlace(
            thingDefinitions = contents,
            tempIds = tempIds,
            validatedIds = validatedIds
        )

        validatedIds shouldBe mapOf(
            "#temp1" to Either.left("#temp1"),
            "R123" to Either.right(r123),
            "C2000" to Either.right(c2000)
        )

        verify(exactly = 1) { thingRepository.findById(r123.id) }
        verify(exactly = 1) { thingRepository.findById(c2000.id) }
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

        every { thingRepository.findById(any()) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(any()) }
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

        every { thingRepository.findById(any()) } returns Optional.of(resource)

        assertThrows<ThingIsNotAClass> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(any()) }
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
            thingDefinitionValidator.validateThingDefinitionsInPlace(
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
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of literal is too long, it throws an exception`() {
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
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when label of literal does not match datatype constraints, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "not a number",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralLabel> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when datatype of literal is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "imvalid",
                    dataType = "foo_bar:string"
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralDatatype> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
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
            thingDefinitionValidator.validateThingDefinitionsInPlace(
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
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given template instance contents, when label of list is invalid, it throws an exception`() {
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to ClassDefinition(
                    label = "\n"
                )
            )
        )

        assertThrows<InvalidLabel> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given thing definitions, when specified class uri is not absolute, it throws an exception`() {
        val uri = ParsedIRI("invalid")
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to ClassDefinition(
                    label = "irrelevant",
                    uri = uri
                )
            )
        )

        assertThrows<URINotAbsolute> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }

    @Test
    fun `Given thing definitions, when specified class uri already exists, it throws an exception`() {
        val uri = ParsedIRI("https://orkg.org/class/C1")
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to ClassDefinition(
                    label = "irrelevant",
                    uri = uri
                )
            )
        )
        val `class` = createClass(uri = uri)

        every { classRepository.findByUri(uri.toString()) } returns Optional.of(`class`)

        assertThrows<URIAlreadyInUse> {
            thingDefinitionValidator.validateThingDefinitionsInPlace(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { classRepository.findByUri(uri.toString()) }
    }
}
