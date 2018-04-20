package eu.tib.orkg.prototype.statements.infrastructure

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class InMemoryResourceRepository : ResourceRepository {

    private val resources = mutableMapOf<ResourceId, Resource>()

    override fun findById(id: ResourceId): Optional<Resource> {
        val resourceFound = resources[id]
        return if (resourceFound != null)
            Optional.of(resourceFound)
        else
            Optional.empty()
    }

    override fun findAll(): Iterable<ResourceId> =
        resources.keys.toSet()

    override fun add(resource: Resource) {
        resources[resource.id] = resource
    }
}
