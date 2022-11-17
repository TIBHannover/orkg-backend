package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.TemplatedResource
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*

interface TemplateRepository {

    fun findTemplateSpecs(resourceId: ResourceId): Optional<TemplatedResource>

    fun formattedLabelFor(resourceId: ResourceId, classes: Set<ClassId>): FormattedLabel?
}
