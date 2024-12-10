package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MismatchedDataType
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
import org.orkg.contenttypes.domain.testing.fixtures.createDecimalLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createFloatLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createUntypedTemplateProperty
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

@Suppress("HttpUrlsUsage")
private const val ORKG_CLASS_NS = "http://orkg.org/orkg/class/"

internal class AbstractTemplatePropertyValueValidatorUnitTest {
    private val classHierarchyRepository: ClassHierarchyRepository = mockk()

    private val abstractTemplatePropertyValueValidator =
        AbstractTemplatePropertyValueValidator(classHierarchyRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(classHierarchyRepository)
    }

    /*
     * Test cardinality validation
     */

    @Test
    fun `Given a template instance update command, when specified value count is within bounds, it returns success`() {
        val property = createUntypedTemplateProperty()
        val propertyInstances = listOf("R123")

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances) }
    }

    @Test
    fun `Given a template property, when too few values for a property are specified, it throws an exception`() {
        val property = createUntypedTemplateProperty()
        val propertyInstances = emptyList<String>()

        shouldThrow<MissingPropertyValues> {
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
        }.asClue {
            it.message shouldBe """Missing values for property "R23" with predicate "P24". min: "1", found: "0"."""
        }
    }

    @Test
    fun `Given a template property, when too many values for a property are specified, it throws an exception`() {
        val property = createUntypedTemplateProperty()
        val propertyInstances = listOf("R123", "R123", "R123")

        shouldThrow<TooManyPropertyValues> {
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
        }.asClue {
            it.message shouldBe """Too many values for property "R23" with predicate "P24". max: "2", found: "3"."""
        }
    }

    /*
     * Test object validation
     */

    @Test
    fun `Given a resource template property with classes as class constraint, when validating a class object, it returns success`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.classes, "Classes")
        )
        val id = "D123"
        val `object` = createClass(ThingId("D123")).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with classes as class constraint, when object value is not a class, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.classes, "Classes")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123")).toThingDefinition()

        shouldThrow<ObjectIsNotAClass> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a class."""
        }
    }

    @Test
    fun `Given a resource template property with predicates as class constraint, when validating a predicate object, it returns success`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.predicates, "Predicates")
        )
        val id = "P123"
        val `object` = createPredicate(ThingId("P123")).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with predicates as class constraint, when object value is not a predicate, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.predicates, "Predicates")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123")).toThingDefinition()

        shouldThrow<ObjectIsNotAPredicate> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a predicate."""
        }
    }

    @Test
    fun `Given a resource template property with list as class constraint, when validating a list object, it returns success`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.list, "List")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123"), classes = setOf(Classes.list)).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with list as class constraint, when object value is not a list, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.list, "List")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123")).toThingDefinition()

        shouldThrow<ObjectIsNotAList> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not a list."""
        }
    }

    @Test
    fun `Given a resource template property, when object value is an instance of target class, it returns success`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123"), classes = setOf(ThingId("C123"))).toThingDefinition()

        abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
    }

    @Test
    fun `Given a resource template property, when object value is not an instance of target class, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123")).toThingDefinition()

        shouldThrow<ResourceIsNotAnInstanceOfTargetClass> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not an instance of target class "C123"."""
        }
    }

    @Test
    fun `Given a resource template property, when object value is not an instance of the target class but an instance of a subclass of target class, it returns success`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123"), classes = setOf(ThingId("Subclass"))).toThingDefinition()

        every { classHierarchyRepository.existsChild(ThingId("C123"), ThingId("Subclass")) } returns true

        abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)

        verify(exactly = 1) { classHierarchyRepository.existsChild(ThingId("C123"), ThingId("Subclass")) }
    }

    @Test
    fun `Given a resource template property, when object value is not an instance of the target class and not an instance of a subclass of target class, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(ThingId("C123"), "Dummy")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123"), classes = setOf(ThingId("Subclass"))).toThingDefinition()

        every { classHierarchyRepository.existsChild(ThingId("C123"), ThingId("Subclass")) } returns false

        shouldThrow<ResourceIsNotAnInstanceOfTargetClass> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R27" with predicate "P27" is not an instance of target class "C123"."""
        }

        verify(exactly = 1) { classHierarchyRepository.existsChild(ThingId("C123"), ThingId("Subclass")) }
    }

    @Test
    fun `Given a resource template property, when object value is a literal, it throws an exception`() {
        val property = createResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.author, "Author")
        )
        val id = "L123"
        val `object` = createLiteral(ThingId("L123")).toThingDefinition()

        shouldThrow<ObjectMustNotBeALiteral> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "L123" for template property "R27" with predicate "P27" must not be a literal."""
        }
    }

    @Test
    fun `Given a literal template property, when object value is not a literal, it throws an exception`() {
        val property = createOtherLiteralTemplateProperty()
        val id = "R123"
        val `object` = createResource(ThingId("R123")).toThingDefinition()

        shouldThrow<ObjectIsNotALiteral> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "R123" for template property "R26" with predicate "${Predicates.hasWikidataId}" is not a literal."""
        }
    }

    @Test
    fun `Given a string literal template property, when literal label does not match the required pattern, it throws an exception`() {
        val property = createStringLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
        )
        val id = "L123"
        val `object` = createLiteral(ThingId("L123")).toThingDefinition()

        shouldThrow<LabelDoesNotMatchPattern> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Label "Default Label" for object "L123" for property "R24" with predicate "${Predicates.description}" does not match pattern "\d+"."""
        }
    }

    @Test
    fun `Given a string literal template property, when literal label matches the required pattern, it returns success`() {
        val property = createStringLiteralTemplateProperty().copy(
            minCount = 0,
            pattern = """\w+""",
            datatype = ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
        )
        val id = "L123"
        val `object` = createLiteral(ThingId("L123"), label = "word").toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a number literal template property, when object label is within bounds, it returns success`() {
        val property = createNumberLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(0),
            maxInclusive = RealNumber(10),
            datatype = ClassReference(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5",
            dataType = Literals.XSD.INT.prefixedUri
        )

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a number literal template property, when object label is lower than minInclusive, it throws an exception`() {
        val property = createNumberLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(10),
            datatype = ClassReference(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5",
            dataType = Literals.XSD.INT.prefixedUri
        )

        shouldThrow<NumberTooLow> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "5" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10"."""
        }
    }

    @Test
    fun `Given a number literal template property, when object label is higher than maxInclusive, it throws an exception`() {
        val property = createNumberLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = RealNumber(5)
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "10",
            dataType = Literals.XSD.INT.prefixedUri
        )

        shouldThrow<NumberTooHigh> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "10" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5"."""
        }
    }

    @Test
    fun `Given a decimal literal template property, when object label is within bounds, it returns success`() {
        val property = createDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(0.0),
            maxInclusive = RealNumber(10.0),
            datatype = ClassReference(Classes.decimal, "Decimal", ParsedIRI(Literals.XSD.DECIMAL.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5.0",
            dataType = Literals.XSD.DECIMAL.prefixedUri
        )

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a decimal literal template property, when object label is lower than minInclusive, it throws an exception`() {
        val property = createDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(10.0),
            datatype = ClassReference(Classes.decimal, "Decimal", ParsedIRI(Literals.XSD.DECIMAL.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5.0",
            dataType = Literals.XSD.DECIMAL.prefixedUri
        )

        shouldThrow<NumberTooLow> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "5.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10.0"."""
        }
    }

    @Test
    fun `Given a decimal literal template property, when object label is higher than maxInclusive, it throws an exception`() {
        val property = createDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = RealNumber(5.0),
            datatype = ClassReference(Classes.decimal, "Decimal", ParsedIRI(Literals.XSD.DECIMAL.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "10.0",
            dataType = Literals.XSD.DECIMAL.prefixedUri
        )

        shouldThrow<NumberTooHigh> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "10.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5.0"."""
        }
    }

    @Test
    fun `Given a float literal template property, when object label is within bounds, it returns success`() {
        val property = createFloatLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(0.0F),
            maxInclusive = RealNumber(10.0F),
            datatype = ClassReference(Classes.float, "Float", ParsedIRI(Literals.XSD.FLOAT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5.0",
            dataType = Literals.XSD.FLOAT.prefixedUri
        )

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a float literal template property, when object label is lower than minInclusive, it throws an exception`() {
        val property = createFloatLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = RealNumber(10.0F),
            datatype = ClassReference(Classes.float, "Float", ParsedIRI(Literals.XSD.FLOAT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "5.0",
            dataType = Literals.XSD.FLOAT.prefixedUri
        )

        shouldThrow<NumberTooLow> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "5.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at least "10.0"."""
        }
    }

    @Test
    fun `Given a float literal template property, when object label is higher than maxInclusive, it throws an exception`() {
        val property = createFloatLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = RealNumber(5.0F),
            datatype = ClassReference(Classes.float, "Float", ParsedIRI(Literals.XSD.FLOAT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "10.0",
            dataType = Literals.XSD.FLOAT.prefixedUri
        )

        shouldThrow<NumberTooHigh> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Number "10.0" for object "#temp1" for property "R25" with predicate "${Predicates.hasHeadingLevel}" must be at most "5.0"."""
        }
    }

    @Test
    fun `Given a literal template property, when object label cannot be parsed by data type, it throws an exception`() {
        val property = createOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "not a number",
            dataType = Literals.XSD.INT.prefixedUri
        )

        shouldThrow<InvalidLiteral> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "#temp1" with value "not a number" for property "R26" with predicate "${Predicates.hasWikidataId}" is not a valid "Integer"."""
        }
    }

    @Test
    fun `Given a literal template property with a custom datatype, when object data type matches the uri of the custom data type, it returns success`() {
        val uri = "https://example.org/classes/software"
        val property = createOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.software, "Software", ParsedIRI(uri))
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "some value",
            dataType = uri
        )

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a literal template property with a custom datatype, when object data type does not match the uri of the custom data type, it throws an exception`() {
        val property = createOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.software, "Software", null)
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "some value",
            dataType = "unknown"
        )

        shouldThrow<MismatchedDataType> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "#temp1" with data type "unknown" for property "R26" with predicate "${Predicates.hasWikidataId}" does not match expected data type "$ORKG_CLASS_NS${Classes.software}"."""
        }
    }

    @Test
    fun `Given a literal template property with a custom datatype, when object data type matches the id of the custom data type, it returns success`() {
        val property = createOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.software, "Software", null)
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "some value",
            dataType = "$ORKG_CLASS_NS${Classes.software}"
        )

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a literal template property with a custom datatype, when object data type does not match the id of the custom data type, it throws an exception`() {
        val property = createOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ClassReference(Classes.software, "Software", null)
        )
        val id = "#temp1"
        val `object` = LiteralDefinition(
            label = "some value",
            dataType = "unknown"
        )

        shouldThrow<MismatchedDataType> {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        }.asClue {
            it.message shouldBe """Object "#temp1" with data type "unknown" for property "R26" with predicate "${Predicates.hasWikidataId}" does not match expected data type "$ORKG_CLASS_NS${Classes.software}"."""
        }
    }
}
