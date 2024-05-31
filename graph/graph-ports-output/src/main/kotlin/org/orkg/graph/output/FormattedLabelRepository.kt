package org.orkg.graph.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.TemplatedResource

interface FormattedLabelRepository {
    fun findTemplateSpecs(resourceIdToTemplateTargetClass: Map<ThingId, ThingId>): Map<ThingId, TemplatedResource>
}
