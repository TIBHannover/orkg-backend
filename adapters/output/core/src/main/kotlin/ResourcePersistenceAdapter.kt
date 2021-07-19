package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ResourcePersistenceAdapter(private val neo4jResourceRepository: Neo4jResourceRepository) :
    ResourceRepository {

    override fun save(resource: Resource): Resource =
        neo4jResourceRepository.save(resource.toNeo4jResource()).toResource()

    override fun findById(resourceId: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(resourceId).map(Neo4jResource::toResource)

}

@Configuration
@ComponentScan
open class SpringConfiguration