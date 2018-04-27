package eu.tib.orkg.prototype.statements.infrastructure

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class InMemoryResourceRepository : ResourceRepository {
    private val resources = mutableMapOf<ResourceId, Resource>()
    private var counter = 0L

    override fun findById(id: ResourceId): Optional<Resource> {
        val resourceFound = resources[id]
        return if (resourceFound != null)
            Optional.of(resourceFound)
        else
            Optional.empty()
    }

    override fun findAll(): Iterable<Resource> =
        resources.values.toSet()

    override fun findByLabel(searchString: String) =
        resources.filter {
            it.value.label.contains(searchString, true)
        }.values.toSet()

    override fun add(resource: Resource) {
        resources[resource.id] = resource
    }

    override fun nextIdentity(): ResourceId {
        counter++
        return ResourceId("$counter")
    }
}
