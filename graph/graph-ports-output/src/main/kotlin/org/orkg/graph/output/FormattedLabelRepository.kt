package org.orkg.graph.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.TemplatedResource

interface FormattedLabelRepository {

    fun findTemplateSpecs(id: ThingId): Optional<TemplatedResource>

    fun formattedLabelFor(id: ThingId, classes: Set<ThingId>): FormattedLabel?
}
