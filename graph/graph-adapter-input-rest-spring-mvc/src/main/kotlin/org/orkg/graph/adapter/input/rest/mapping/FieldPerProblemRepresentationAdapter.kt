package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation

interface FieldPerProblemRepresentationAdapter : ResourceRepresentationAdapter {

    fun List<FieldWithFreq>.mapToFieldWithFreqRepresentation(): List<FieldWithFreqRepresentation> {
        val resources = map { it.field }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toFieldWithFreqRepresentation(usageCounts, formattedLabels) }
    }

    fun FieldWithFreq.toFieldWithFreqRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): FieldWithFreqRepresentation =
        FieldWithFreqRepresentation(field.toResourceRepresentation(usageCounts, formattedLabels), freq)
}
