package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class ResourcePersistenceAdapter(
    private val neo4jResourceRepository: Neo4jResourceRepository
) : ResourceRepository {

    override fun save(resource: Resource) {
        TODO("Not yet implemented")
    }

    override fun findById(resourceId: ResourceId?): Resource? =
        neo4jResourceRepository.findByResourceId(resourceId).map(Neo4jResource::toResource).orElse(null)
}

@Configuration
@ComponentScan
open class SpringConfiguration
