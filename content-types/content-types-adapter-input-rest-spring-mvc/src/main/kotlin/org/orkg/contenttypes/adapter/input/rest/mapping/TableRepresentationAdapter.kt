package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.TableRepresentation
import org.orkg.contenttypes.adapter.input.rest.TableRepresentation.*
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.Table.Row
import org.orkg.contenttypes.domain.ThingReference
import org.springframework.data.domain.Page

interface TableRepresentationAdapter : ThingReferenceRepresentationAdapter {

    fun Optional<Table>.mapToTableRepresentation(): Optional<TableRepresentation> =
        map { it.toTableRepresentation() }

    fun Page<Table>.mapToTableRepresentation(): Page<TableRepresentation> =
        map { it.toTableRepresentation() }

    fun Table.toTableRepresentation(): TableRepresentation =
        TableRepresentation(
            id = id,
            label = label,
            rows = rows.map { it.toRowRepresentation() },
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility,
            unlistedBy = unlistedBy
        )

    fun Row.toRowRepresentation(): RowRepresentation =
        RowRepresentation(
            label = label,
            data = data.map { thing -> thing?.let { ThingReference.from(it).toThingReferenceRepresentation() } }
        )
}
