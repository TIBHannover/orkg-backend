package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation

interface FieldPerProblemRepresentationAdapter : ResourceRepresentationAdapter {

    fun List<FieldWithFreq>.mapToFieldWithFreqRepresentation(
        capabilities: MediaTypeCapabilities
    ): List<FieldWithFreqRepresentation> {
        val resources = map { it.field }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toFieldWithFreqRepresentation(usageCounts, formattedLabels) }
    }

    fun FieldWithFreq.toFieldWithFreqRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): FieldWithFreqRepresentation =
        FieldWithFreqRepresentation(field.toResourceRepresentation(usageCounts, formattedLabels), freq)
}
