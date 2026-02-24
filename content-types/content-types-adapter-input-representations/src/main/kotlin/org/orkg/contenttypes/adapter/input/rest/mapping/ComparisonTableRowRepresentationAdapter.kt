package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonTableRowRepresentation
import org.orkg.contenttypes.domain.ComparisonTableRow
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonTableRowRepresentationAdapter : ThingReferenceRepresentationAdapter {
    fun Optional<ComparisonTableRow>.mapToComparisonTableRowRepresentation(): Optional<ComparisonTableRowRepresentation> =
        map { it.toComparisonTableRowRepresentation() }

    fun Page<ComparisonTableRow>.mapToComparisonTableRowRepresentation(): Page<ComparisonTableRowRepresentation> =
        map { it.toComparisonTableRowRepresentation() }

    fun List<ComparisonTableRow>.mapToComparisonTableRowRepresentation(): List<ComparisonTableRowRepresentation> =
        map { it.toComparisonTableRowRepresentation() }

    fun ComparisonTableRow.toComparisonTableRowRepresentation(): ComparisonTableRowRepresentation =
        ComparisonTableRowRepresentation(
            values = values.map { it?.toThingReferenceRepresentation() },
            children = children.mapValues { (_, value) -> value.mapToComparisonTableRowRepresentation() }
        )
}
