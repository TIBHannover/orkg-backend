package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
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
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.ObjectIsNotAClass
import org.orkg.contenttypes.domain.ObjectIsNotAList
import org.orkg.contenttypes.domain.ObjectIsNotALiteral
import org.orkg.contenttypes.domain.ObjectIsNotAPredicate
import org.orkg.contenttypes.domain.ObjectMustNotBeALiteral
import org.orkg.contenttypes.domain.ResourceIsNotAnInstanceOfTargetClass
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyDecimalLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyFloatLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplateInstance
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

class TemplateInstancePropertyValueValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val templateInstancePropertyValueValidator = TemplateInstancePropertyValueValidator(thingRepository, classRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository, classRepository)
    }

    @Test
    fun `Given a template instance update command, when validating its properties, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.field to listOf("#temp1", "R1"),
                Predicates.description to listOf("L123"),
                Predicates.hasHeadingLevel to listOf("#temp1"),
                Predicates.hasWikidataId to listOf("L123"),
                Predicates.hasAuthor to listOf("#temp2", "R1", "R123")
            ),
            resources = mapOf(
                "#temp2" to ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("C28"))
                )
            ),
            literals = mapOf(
                "#temp1" to LiteralDefinition("1") // datatype is irrelevant, as it will be re-assigned by the service
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate(),
            templateInstance = createDummyTemplateInstance(),
            tempIds = setOf("#temp1", "#temp2"),
            validatedIds = mapOf(
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

        val result = templateInstancePropertyValueValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + mapOf(
                "#temp2" to Either.left("#temp2"),
                "#temp1" to Either.left("#temp1")
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
                "#temp1" to LiteralDefinition(
                    label = "1",
                    dataType = Literals.XSD.INT.prefixedUri
                )
            )
        }
    }

    @Test
    fun `Given a template instance update command, when provided property is not defined by template, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
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
            template = createDummyTemplate(),
            templateInstance = createDummyTemplateInstance()
        )

        shouldThrow<UnknownTemplateProperties> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Unknown properties for template "R54631": "Unknown"."""
        }
    }

    @Test
    fun `Given a template instance update command, when too few values for a property are specified, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = emptyMap(),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate(),
            templateInstance = createDummyTemplateInstance()
        )

        shouldThrow<MissingPropertyValues> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Missing values for property "R23" with predicate "P24". min: "1", found: "0"."""
        }
    }

    @Test
    fun `Given a template instance update command, when too many values for a property are specified, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P24") to listOf("R123", "R123", "R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate(),
            templateInstance = createDummyTemplateInstance()
        )

        shouldThrow<TooManyPropertyValues> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Too many values for property "R23" with predicate "P24". max: "2", found: "3"."""
        }
    }

    /*
     * Test object validation for existing "things"
     */

    @Test
    fun `Given a template instance update command, when existing object value is not a class, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.classes, "Classes")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("R123" to Either.right(createResource(ThingId("R123"))))
        )

        shouldThrow<ObjectIsNotAClass> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a class."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing object value is not a predicate, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.predicates, "Predicates")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("R123" to Either.right(createResource(ThingId("R123"))))
        )

        shouldThrow<ObjectIsNotAPredicate> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a predicate."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing object value is not a list, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.list, "List")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("R123" to Either.right(createResource(ThingId("R123"))))
        )

        shouldThrow<ObjectIsNotAList> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a list."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing object value is not a literal, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasWikidataId to listOf("R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(createDummyOtherLiteralTemplateProperty())
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("R123" to Either.right(createResource(ThingId("R123"))))
        )

        shouldThrow<ObjectIsNotALiteral> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "R123" for template property "R26" with predicate "${Predicates.hasWikidataId}" is not a literal."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing object value must not be a literal, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("L123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.author, "Author")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("L123" to Either.right(createLiteral(ThingId("L123"))))
        )

        shouldThrow<ObjectMustNotBeALiteral> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "L123" for template property "R27" with predicate "P27" must not be a literal."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing object value is not an instance of target class, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("R123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("R123" to Either.right(createResource(ThingId("R123"))))
        )

        shouldThrow<ResourceIsNotAnInstanceOfTargetClass> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not an instance of target class "C123"."""
        }
    }

    @Test
    fun `Given a template instance update command, when existing literal label does not match the required pattern, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.description to listOf("L123")
            ),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyStringLiteralTemplateProperty().copy(
                        minCount = 0,
                        datatype = ObjectIdAndLabel(Classes.string, "String")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("L123" to Either.right(createLiteral(ThingId("L123"))))
        )

        shouldThrow<LabelDoesNotMatchPattern> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Label "Default Label" for object "L123" for property "R24" with predicate "${Predicates.description}" does not match pattern "\d+"."""
        }
    }

    /*
     * Test object validation for objects that need to be created
     */

    @Test
    fun `Given a template instance update command, when validating object literals, it properly assigns xsd data types`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P1") to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition("true")
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyOtherLiteralTemplateProperty().copy(
                        path = ObjectIdAndLabel(ThingId("P1"), "Irrelevant"),
                        datatype = ObjectIdAndLabel(Classes.boolean, "Irrelevant")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance().copy(statements = mapOf(ThingId("P1") to emptyList())),
            tempIds = setOf("#temp1", "#temp2", "#temp3"),
            validatedIds = emptyMap()
        )

        val result = templateInstancePropertyValueValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + mapOf(
                "#temp1" to Either.left("#temp1")
            )
            it.statementsToAdd shouldBe setOf(
                BakedStatement("R54631", "P1", "#temp1"),
            )
            it.statementsToRemove shouldBe emptySet()
            it.literals shouldBe mapOf(
                "#temp1" to LiteralDefinition(
                    label = "true",
                    dataType = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
        }
    }

    @Test
    fun `Given a template instance update command, when validating object literals, it properly assigns custom data types`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P1") to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition("true")
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val `class` = createClass(ThingId("C123"))
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyOtherLiteralTemplateProperty().copy(
                        path = ObjectIdAndLabel(ThingId("P1"), "Irrelevant"),
                        datatype = ObjectIdAndLabel(`class`.id, "Irrelevant")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance().copy(statements = mapOf(ThingId("P1") to emptyList())),
            tempIds = setOf("#temp1", "#temp2", "#temp3"),
            validatedIds = emptyMap()
        )

        every { classRepository.findById(`class`.id) } returns Optional.of(`class`)

        val result = templateInstancePropertyValueValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + mapOf(
                "#temp1" to Either.left("#temp1")
            )
            it.statementsToAdd shouldBe setOf(
                BakedStatement("R54631", "P1", "#temp1"),
            )
            it.statementsToRemove shouldBe emptySet()
            it.literals shouldBe mapOf(
                "#temp1" to LiteralDefinition(
                    label = "true",
                    dataType = `class`.uri!!.toString()
                )
            )
        }

        verify(exactly = 1) { classRepository.findById(`class`.id) }
    }

    @Test
    fun `Given a template instance update command, when validating object literals, it uses xsd string as a fallback data type`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P1") to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition("true")
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyOtherLiteralTemplateProperty().copy(
                        path = ObjectIdAndLabel(ThingId("P1"), "Irrelevant"),
                        datatype = ObjectIdAndLabel(ThingId("C123"), "Irrelevant")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance().copy(statements = mapOf(ThingId("P1") to emptyList())),
            tempIds = setOf("#temp1", "#temp2", "#temp3"),
            validatedIds = emptyMap()
        )

        every { classRepository.findById(ThingId("C123")) } returns Optional.empty()

        val result = templateInstancePropertyValueValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + mapOf(
                "#temp1" to Either.left("#temp1")
            )
            it.statementsToAdd shouldBe setOf(
                BakedStatement("R54631", "P1", "#temp1"),
            )
            it.statementsToRemove shouldBe emptySet()
            it.literals shouldBe mapOf(
                "#temp1" to LiteralDefinition(
                    label = "true",
                    dataType = Literals.XSD.STRING.prefixedUri
                )
            )
        }

        verify(exactly = 1) { classRepository.findById(ThingId("C123")) }
    }

    @Test
    fun `Given a template instance update command, when temp object value is not a class, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("#temp1")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "irrelevant",
                    classes = emptySet()
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.classes, "Classes")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ObjectIsNotAClass> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R27" with predicate "P27" is not a class."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object value is not a predicate, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("#temp1")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "irrelevant",
                    classes = emptySet()
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.predicates, "Predicates")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ObjectIsNotAPredicate> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R27" with predicate "P27" is not a predicate."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object value is not a list, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("#temp1")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "irrelevant",
                    classes = emptySet()
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.list, "List")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ObjectIsNotAList> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R27" with predicate "P27" is not a list."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object value is not a literal, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasWikidataId to listOf("#temp1")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "irrelevant",
                    classes = emptySet()
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(createDummyOtherLiteralTemplateProperty())
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ObjectIsNotALiteral> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R26" with predicate "${Predicates.hasWikidataId}" is not a literal."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object value must not be a literal, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "irrelevant"
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(Classes.author, "Author")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ObjectMustNotBeALiteral> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R27" with predicate "P27" must not be a literal."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object value is not an instance of target class, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                ThingId("P27") to listOf("#temp1")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinition(
                    label = "irrelevant",
                    classes = setOf()
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyResourceTemplateProperty().copy(
                        minCount = 0,
                        `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<ResourceIsNotAnInstanceOfTargetClass> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" for template property "R27" with predicate "P27" is not an instance of target class "C123"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label does not match the required pattern, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.description to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition("Default label")
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyStringLiteralTemplateProperty().copy(
                        minCount = 0,
                        datatype = ObjectIdAndLabel(Classes.string, "String")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<LabelDoesNotMatchPattern> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Label "Default label" for object "#temp1" for property "R24" with predicate "${Predicates.description}" does not match pattern "\d+"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (int) is lower than minInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "5",
                    dataType = Literals.XSD.INT.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyNumberLiteralTemplateProperty().copy(
                        minCount = 0,
                        minInclusive = 10,
                        datatype = ObjectIdAndLabel(Classes.integer, "Integer")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooLow> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "5" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (decimal) is lower than minInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "5.0",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyDecimalLiteralTemplateProperty().copy(
                        minCount = 0,
                        minInclusive = 10.0,
                        datatype = ObjectIdAndLabel(Classes.decimal, "Decimal")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooLow> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "5.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10.0"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (float) is lower than minInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "5.0",
                    dataType = Literals.XSD.FLOAT.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyFloatLiteralTemplateProperty().copy(
                        minCount = 0,
                        minInclusive = 10.0F,
                        datatype = ObjectIdAndLabel(Classes.float, "Float")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooLow> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "5.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10.0"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (int) is higher than maxInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "10",
                    dataType = Literals.XSD.INT.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyNumberLiteralTemplateProperty().copy(
                        minCount = 0,
                        maxInclusive = 5
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooHigh> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "10" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (decimal) is higher than maxInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "10.0",
                    dataType = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyDecimalLiteralTemplateProperty().copy(
                        minCount = 0,
                        maxInclusive = 5.0,
                        datatype = ObjectIdAndLabel(Classes.decimal, "Decimal")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooHigh> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "10.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5.0"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object label (float) is higher than maxInclusive, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasHeadingLevel to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition(
                    label = "10.0",
                    dataType = Literals.XSD.FLOAT.prefixedUri
                )
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyFloatLiteralTemplateProperty().copy(
                        minCount = 0,
                        maxInclusive = 5.0F,
                        datatype = ObjectIdAndLabel(Classes.float, "Float")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<NumberTooHigh> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Number "10.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5.0"."""
        }
    }

    @Test
    fun `Given a template instance update command, when temp object does not match expected data type, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand().copy(
            statements = mapOf(
                Predicates.hasWikidataId to listOf("#temp1")
            ),
            resources = emptyMap(),
            literals = mapOf(
                "#temp1" to LiteralDefinition("not a number") // datatype is irrelevant, as it will be re-assigned by the service
            ),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate().copy(
                properties = listOf(
                    createDummyOtherLiteralTemplateProperty().copy(
                        minCount = 0,
                        datatype = ObjectIdAndLabel(Classes.integer, "Integer")
                    )
                )
            ),
            templateInstance = createDummyTemplateInstance(),
            validatedIds = mapOf("#temp1" to Either.left("#temp1"))
        )

        shouldThrow<InvalidLiteral> { templateInstancePropertyValueValidator(command, state) }.asClue {
            it.message shouldBe """Object "#temp1" with value "not a number" for property "R26" with predicate "${Predicates.hasWikidataId}" is not a valid "Integer"."""
        }
    }
}
