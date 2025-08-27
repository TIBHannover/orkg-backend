package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.adapter.input.rest.CSVRepresentation
import org.orkg.dataimport.domain.csv.CSV
import org.springframework.data.domain.Page
import java.util.Optional

interface CSVRepresentationAdapter {
    fun Optional<CSV>.mapToCSVRepresentation(): Optional<CSVRepresentation> =
        map { it.toCSVRepresentation() }

    fun Page<CSV>.mapToCSVRepresentation(): Page<CSVRepresentation> =
        map { it.toCSVRepresentation() }

    fun CSV.toCSVRepresentation(): CSVRepresentation =
        CSVRepresentation(id, name, type, format, state, createdBy, createdAt)
}
