package org.orkg.dataimport.domain.testing.fixtures

import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.csv.CSVSchema
import org.orkg.graph.domain.Classes

fun createCSVSchema() = CSVSchema {
    header {
        namespace("closed-namespace") {
            closed()
            value("the only possible value") {
                columnValueType(Classes.boolean)
                columnValueConstraint(regexConstraint(Regex("""[A-Za-z ]+""")))
            }
        }
        namespace("open-namespace") {
            headerValueConstraint(regexConstraint(Regex("""[\w ]+""")))
            columnValueConstraint(regexConstraint(Regex("""[\w ]+""")))
            columnValueType(Classes.string)
        }
    }
    value {
        namespace("open-value-namespace") {
            columnValueType(Classes.boolean)
            columnValueConstraint(regexConstraint(Regex("""[A-Za-z ]+""")))
        }
        namespace("closed-value-namespace") {
            closed()
            value("option1")
            value("option2")
            columnValueType(Classes.string)
        }
    }
    types {
        type("text", Classes.string)
        type("decimal", Classes.decimal)
        type("int", Classes.integer)
        type("boolean", Classes.boolean)
    }
}

fun createCSVHeaders() = listOf(
    CSVHeader(
        column = 1,
        name = "abc",
        namespace = null,
        columnType = null,
    ),
    CSVHeader(
        column = 2,
        name = "the only possible value",
        namespace = "closed-namespace",
        columnType = Classes.boolean,
    ),
    CSVHeader(
        column = 3,
        name = "a",
        namespace = "open-namespace",
        columnType = Classes.string,
    ),
    CSVHeader(
        column = 4,
        name = "b",
        namespace = "open-namespace",
        columnType = Classes.string,
    )
)

private fun regexConstraint(regex: Regex): (String) -> Unit =
    { if (!it.matches(regex)) throw IllegalArgumentException("""Value "$it" does not match pattern "$regex".""") }
