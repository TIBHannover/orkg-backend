package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
import org.orkg.contenttypes.domain.testing.fixtures.createDummyDecimalLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyFloatLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyUntypedTemplateProperty
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

class AbstractTemplatePropertyValueValidatorUnitTest {
    private val abstractTemplatePropertyValueValidator = AbstractTemplatePropertyValueValidator()

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified()
    }

    /*
     * Test cardinality validation
     */

    @Test
    fun `Given a template instance update command, when specified value count is within bounds, it returns success`() {
        val property = createDummyUntypedTemplateProperty()
        val propertyInstances = listOf("R123")

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances) }
    }

    @Test
    fun `Given a template property, when too few values for a property are specified, it throws an exception`() {
        val property = createDummyUntypedTemplateProperty()
        val propertyInstances = emptyList<String>()

        shouldThrow<MissingPropertyValues> {
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
        }.asClue {
            it.message shouldBe """Missing values for property "R23" with predicate "P24". min: "1", found: "0"."""
        }
    }

    @Test
    fun `Given a template property, when too many values for a property are specified, it throws an exception`() {
        val property = createDummyUntypedTemplateProperty()
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
        val property = createDummyResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.classes, "Classes")
        )
        val id = "D123"
        val `object` = createClass(ThingId("D123")).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with classes as class constraint, when object value is not a class, it throws an exception`() {
        val property = createDummyResourceTemplateProperty().copy(
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
        val property = createDummyResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.predicates, "Predicates")
        )
        val id = "P123"
        val `object` = createPredicate(ThingId("P123")).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with predicates as class constraint, when object value is not a predicate, it throws an exception`() {
        val property = createDummyResourceTemplateProperty().copy(
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
        val property = createDummyResourceTemplateProperty().copy(
            minCount = 0,
            `class` = ObjectIdAndLabel(Classes.list, "List")
        )
        val id = "R123"
        val `object` = createResource(ThingId("R123"), classes = setOf(Classes.list)).toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a resource template property with list as class constraint, when object value is not a list, it throws an exception`() {
        val property = createDummyResourceTemplateProperty().copy(
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
    fun `Given a resource template property, when object value is not an instance of target class, it throws an exception`() {
        val property = createDummyResourceTemplateProperty().copy(
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
    fun `Given a resource template property, when object value is a literal, it throws an exception`() {
        val property = createDummyResourceTemplateProperty().copy(
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
        val property = createDummyOtherLiteralTemplateProperty()
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
        val property = createDummyStringLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ObjectIdAndLabel(Classes.string, "String")
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
        val property = createDummyStringLiteralTemplateProperty().copy(
            minCount = 0,
            pattern = """\w+""",
            datatype = ObjectIdAndLabel(Classes.string, "String")
        )
        val id = "L123"
        val `object` = createLiteral(ThingId("L123"), label = "word").toThingDefinition()

        assertDoesNotThrow { abstractTemplatePropertyValueValidator.validateObject(property, id, `object`) }
    }

    @Test
    fun `Given a number literal template property, when object label is within bounds, it returns success`() {
        val property = createDummyNumberLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 0,
            maxInclusive = 10,
            datatype = ObjectIdAndLabel(Classes.integer, "Integer")
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
        val property = createDummyNumberLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 10,
            datatype = ObjectIdAndLabel(Classes.integer, "Integer")
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
        val property = createDummyNumberLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = 5
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
        val property = createDummyDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 0.0,
            maxInclusive = 10.0,
            datatype = ObjectIdAndLabel(Classes.decimal, "Decimal")
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
        val property = createDummyDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 10.0,
            datatype = ObjectIdAndLabel(Classes.decimal, "Decimal")
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
        val property = createDummyDecimalLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = 5.0,
            datatype = ObjectIdAndLabel(Classes.decimal, "Decimal")
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
        val property = createDummyFloatLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 0.0F,
            maxInclusive = 10.0F,
            datatype = ObjectIdAndLabel(Classes.float, "Float")
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
        val property = createDummyFloatLiteralTemplateProperty().copy(
            minCount = 0,
            minInclusive = 10.0F,
            datatype = ObjectIdAndLabel(Classes.float, "Float")
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
        val property = createDummyFloatLiteralTemplateProperty().copy(
            minCount = 0,
            maxInclusive = 5.0F,
            datatype = ObjectIdAndLabel(Classes.float, "Float")
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
        val property = createDummyOtherLiteralTemplateProperty().copy(
            minCount = 0,
            datatype = ObjectIdAndLabel(Classes.integer, "Integer")
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
}
