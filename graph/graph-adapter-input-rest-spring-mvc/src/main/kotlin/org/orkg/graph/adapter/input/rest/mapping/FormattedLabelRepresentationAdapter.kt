package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.FORMATTED_LABELS_CAPABILITY
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.FormattedLabelUseCases

interface FormattedLabelRepresentationAdapter {
    val formattedLabelService: FormattedLabelUseCases

    fun formatLabelFor(resources: List<Resource>, capabilities: MediaTypeCapabilities): Map<ThingId, FormattedLabel?> =
        capabilities.getOrDefault(FORMATTED_LABELS_CAPABILITY)
            .map { formattedLabelService.findFormattedLabels(resources) }
            .orElseGet { emptyMap() }
}
