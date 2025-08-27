package org.orkg.dataimport.domain

import dev.forkhandles.values.ofOrNull
import org.orkg.common.DOI
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.csv.CSVSchema
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod

object Schemes {
    val paperCSV = CSVSchema {
        header {
            namespace("paper") {
                closed()
                value("title") {
                    columnValueType(Classes.string)
                }
                value("doi") {
                    columnValueType(Classes.string)
                    columnValueConstraint { DOI.ofOrNull(it) ?: throw IllegalArgumentException("""Invalid DOI "$it".""") }
                }
                value("authors") {
                    columnValueType(Classes.string)
                }
                value("publication_month") {
                    columnValueType(Classes.integer)
                }
                value("publication_year") {
                    columnValueType(Classes.integer)
                }
                value("research_field") {
                    columnValueType(Classes.researchField)
                    columnValueConstraint(::ThingId)
                }
                value("url") {
                    columnValueType(Classes.uri)
                }
                value("published_in") {
                    columnValueType(Classes.venue)
                }
            }
            namespace("contribution") {
                closed()
                value("research_problem") {
                    columnValueType(Classes.problem)
                }
                value("extraction_method") {
                    columnValueType(Classes.string)
                    columnValueConstraint { ExtractionMethod.parse(it.uppercase().trim()) }
                }
            }
            namespace("orkg") {
                headerValueConstraint(::ThingId)
            }
            namespace("resource") {
                columnValueType(Classes.resource)
            }
        }
        value {
            namespace("orkg") {
                columnValueType(Classes.thing)
                columnValueConstraint(::ThingId)
            }
            namespace("resource") {
                columnValueType(Classes.resource)
            }
        }
        types {
            type("text", Classes.string)
            type("decimal", Classes.decimal)
            type("integer", Classes.integer)
            type("boolean", Classes.boolean)
            type("date", Classes.date)
            type("url", Classes.uri)
        }
    }
}
