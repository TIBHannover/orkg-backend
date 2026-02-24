package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonTableRepresentation
import org.orkg.contenttypes.domain.ComparisonTable
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonTableRepresentationAdapter :
    ComparisonTableRowRepresentationAdapter,
    LabeledComparisonPathRepresentationAdapter {
    fun Optional<ComparisonTable>.mapToComparisonTableRepresentation(): Optional<ComparisonTableRepresentation> =
        map { it.toComparisonTableRepresentation() }

    fun Page<ComparisonTable>.mapToComparisonTableRepresentation(): Page<ComparisonTableRepresentation> =
        map { it.toComparisonTableRepresentation() }

    fun List<ComparisonTable>.mapToComparisonTableRepresentation(): List<ComparisonTableRepresentation> =
        map { it.toComparisonTableRepresentation() }

    fun ComparisonTable.toComparisonTableRepresentation(): ComparisonTableRepresentation =
        ComparisonTableRepresentation(
            selectedPaths = selectedPaths.mapToLabeledComparisonPathRepresentation(),
            titles = titles.map { it.toThingReferenceRepresentation() },
            subtitles = subtitles.map { it?.toThingReferenceRepresentation() },
            values = values.mapValues { (_, value) -> value.mapToComparisonTableRowRepresentation() },
        )
}
