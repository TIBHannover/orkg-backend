package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface ResourceRepository {
    fun save(resource: Resource): Resource
    fun findById(resourceId: ResourceId?): Optional<Resource>
}
