package org.orkg.graph.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Resource

interface FormattedLabelUseCases : RetrieveFormattedLabelUseCase

interface RetrieveFormattedLabelUseCase {
    fun findFormattedLabels(resources: List<Resource>): Map<ThingId, FormattedLabel?>
}
