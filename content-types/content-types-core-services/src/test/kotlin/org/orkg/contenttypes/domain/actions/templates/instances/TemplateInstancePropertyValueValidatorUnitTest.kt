package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

internal class TemplateInstancePropertyValueValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = mockk()

    private val templateInstancePropertyValueValidator = TemplateInstancePropertyValueValidator(
        ThingIdValidator(thingRepository), // it is easier to mock thingRepository calls than thingIdValidator calls
        classRepository,
        statementRepository,
        abstractTemplatePropertyValueValidator
    )

    @Test
    fun `Given a template instance update command, when validating its properties, it returns success`() {
        val temp1 = CreateLiteralCommandPart("1") // datatype is irrelevant, as it will be re-assigned by the service
        val temp2 = CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("C28"))
        )
        val command = updateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.field to listOf("#temp1", "R1"),
                Predicates.description to listOf("L123"),
                Predicates.hasHeadingLevel to listOf("#temp1"),
                Predicates.hasWikidataId to listOf("L123"),
                Predicates.hasAuthor to listOf("#temp2", "R1", "R123")
            ),
            resources = mapOf(
                "#temp2" to temp2
            ),
            literals = mapOf(
                "#temp1" to temp1
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createTemplate(),
            templateInstance = createTemplateInstance(),
            validationCache = mapOf(
                "R123" to Either.right(
                    createResource(
                        id = ThingId("R123"),
                        label = "word",
                        classes = setOf(ThingId("C28"))
                    )
                ),
                "R1" to Either.right(
                    createResource(
                        label = "other",
                        classes = setOf(ThingId("C28"))
                    )
                ),
                "L123" to Either.right(
                    createLiteral(
                        id = ThingId("L123"),
                        label = "10",
                        datatype = Literals.XSD.DECIMAL.prefixedUri
                    )
                )
            )
        )

        every { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) } just runs
        every { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) } just runs

        val result = templateInstancePropertyValueValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.validationCache shouldBe state.validationCache + mapOf(
                "#temp2" from command,
                "#temp1" from command
            )
            it.statementsToAdd shouldBe setOf(
                BakedStatement("R54631", Predicates.field.value, "#temp1"),
                BakedStatement("R54631", Predicates.field.value, "R1"),
                BakedStatement("R54631", Predicates.description.value, "L123"),
                BakedStatement("R54631", Predicates.hasHeadingLevel.value, "#temp1"),
                BakedStatement("R54631", Predicates.hasWikidataId.value, "L123"),
                BakedStatement("R54631", Predicates.hasAuthor.value, "#temp2"),
                BakedStatement("R54631", Predicates.hasAuthor.value, "R123")
            )
            it.statementsToRemove shouldBe setOf(
                BakedStatement("R54631", Predicates.field.value, "L1"),
                BakedStatement("R54631", Predicates.description.value, "L1"),
                BakedStatement("R54631", Predicates.hasHeadingLevel.value, "L1"),
                BakedStatement("R54631", Predicates.hasWikidataId.value, "L1")
            )
            it.literals shouldBe mapOf(
                "#temp1" to CreateLiteralCommandPart(
                    label = "1",
                    dataType = Literals.XSD.INT.prefixedUri
                )
            )
        }

        verify { abstractTemplatePropertyValueValidator.validateCardinality(any(), any()) }
        verify { abstractTemplatePropertyValueValidator.validateObject(any(), any(), any()) }
    }

    @Test
    fun `Given a template instance update command, when provided property is not defined by template, it throws an exception`() {
        val command = updateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("Unknown") to listOf("L123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createTemplate(),
            templateInstance = createTemplateInstance()
        )

        shouldThrow<UnknownTemplateProperties> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Unknown properties for template "R54631": "Unknown"."""
        }
    }
}
