package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.ThingId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.FormattedLabelRepository

interface FormattedLabelRepresentationAdapter {
    val formattedLabelRepository: FormattedLabelRepository
    val flags: FeatureFlagService

    fun formatLabelFor(resources: List<Resource>): Map<ThingId, FormattedLabel?> =
        if (flags.isFormattedLabelsEnabled())
            resources.associate { it.id to formattedLabelRepository.formattedLabelFor(it.id, it.classes) }
        else emptyMap()
}
