package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jTemplateRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.TemplatedResource
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jTemplateAdapter(
    private val neo4jRepository: Neo4jTemplateRepository
) : TemplateRepository {

    override fun findTemplateSpecs(resourceId: ResourceId): Optional<TemplatedResource> =
        neo4jRepository.findTemplateSpecs(resourceId)

    override fun formattedLabelFor(resourceId: ResourceId, classes: Set<ClassId>): FormattedLabel? {
        if (classes.isEmpty()) return null
        val templatedResource = findTemplateSpecs(resourceId)
        if (!templatedResource.isPresent) return null
        return FormattedLabel.ofOrNull(templatedResource.get().composeFormattedLabel())
    }
}
