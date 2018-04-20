package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface ResourceRepository {

    fun findById(id: ResourceId): Optional<Resource>

    fun findAll(): Iterable<ResourceId>

    fun add(resource: Resource)
}
