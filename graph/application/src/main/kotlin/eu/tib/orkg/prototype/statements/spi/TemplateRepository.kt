package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.TemplatedResource
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*

interface TemplateRepository {

    fun findTemplateSpecs(id: ThingId): Optional<TemplatedResource>

    fun formattedLabelFor(id: ThingId, classes: Set<ThingId>): FormattedLabel?
}
