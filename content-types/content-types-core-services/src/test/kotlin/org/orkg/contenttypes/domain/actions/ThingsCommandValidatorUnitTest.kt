package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.from
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
import java.util.Optional

@Nested
internal class ThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val thingsCommandValidator = ThingsCommandValidator(thingRepository, classRepository)

    @Test
    fun `Given paper contents, when valid, it returns success`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to CreateResourceCommandPart(
                    label = "MOTO",
                    classes = setOf(ThingId("C2000"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralCommandPart(
                    label = "0.1",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = mapOf(
                "#temp3" to CreatePredicateCommandPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to CreateListCommandPart(
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

        val validationCache = mutableMapOf<String, Either<CreateThingCommandPart, Thing>>()

        val result = thingsCommandValidator.validate(contents, validationCache)

        result shouldBe mapOf(
            "#temp1" from contents,
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
                "#temp1" to CreateResourceCommandPart(
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
            thingsCommandValidator.validate(
                thingsCommand = contents,
                validationCache = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for resource does not resolve to a class, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to CreateResourceCommandPart(
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

        assertThrows<ThingIsNotAClass> { thingsCommandValidator.validate(contents, mutableMapOf()) }

        verify(exactly = 1) { thingRepository.findById(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for a resource is reserved, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to CreateResourceCommandPart(
                    label = "MOTO",
                    classes = setOf(reservedClassIds.first())
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<ReservedClass> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when label of resource is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to CreateResourceCommandPart(
                    label = "\n"
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when label of literal is too long, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to CreateLiteralCommandPart(
                    label = "a".repeat(MAX_LABEL_LENGTH + 1)
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when label of literal does not match datatype constraints, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to CreateLiteralCommandPart(
                    label = "not a number",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when datatype of literal is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to CreateLiteralCommandPart(
                    label = "imvalid",
                    dataType = "foo_bar:string"
                )
            ),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<InvalidLiteralDatatype> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when label of predicate is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = mapOf(
                "#temp1" to CreatePredicateCommandPart(
                    label = "\n"
                )
            ),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given paper contents, when label of list is invalid, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = mapOf(
                "#temp1" to CreateListCommandPart(
                    label = "\n"
                )
            ),
            contributions = emptyList()
        )

        assertThrows<InvalidLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given template instance contents, when label of list is invalid, it throws an exception`() {
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to CreateClassCommandPart(
                    label = "\n"
                )
            )
        )

        assertThrows<InvalidLabel> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given a things command, when specified class uri is not absolute, it throws an exception`() {
        val uri = ParsedIRI.create("invalid")
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to CreateClassCommandPart(
                    label = "irrelevant",
                    uri = uri
                )
            )
        )

        assertThrows<URINotAbsolute> { thingsCommandValidator.validate(contents, mutableMapOf()) }
    }

    @Test
    fun `Given a things command, when specified class uri already exists, it throws an exception`() {
        val uri = ParsedIRI.create("https://orkg.org/class/C1")
        val contents = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to CreateClassCommandPart(
                    label = "irrelevant",
                    uri = uri
                )
            )
        )
        val `class` = createClass(uri = uri)

        every { classRepository.findByUri(uri.toString()) } returns Optional.of(`class`)

        assertThrows<URIAlreadyInUse> { thingsCommandValidator.validate(contents, mutableMapOf()) }

        verify(exactly = 1) { classRepository.findByUri(uri.toString()) }
    }
}
