package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

interface ResourceRepository {
    fun save(resource: Resource)
    fun findById(resourceId: ResourceId?): Resource?
}
