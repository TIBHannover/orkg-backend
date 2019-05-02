package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

@Service
@Transactional
class Neo4jResourceService(
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator
) : ResourceService {

    override fun create(label: String): Resource {
        val resourceId = neo4jResourceIdGenerator.nextIdentity()
        return neo4jResourceRepository.save(Neo4jResource(label = label, resourceId = resourceId))
            .toResource()
    }

    override fun findAll() =
        neo4jResourceRepository.findAll()
            .map(Neo4jResource::toResource)

    override fun findById(id: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(id)
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i)^$label$") // TODO: See declaration
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i).*$part.*") // TODO: See declaration
            .map(Neo4jResource::toResource)

    override fun update(resource: Resource): Resource {
        // already checked by service
        val found = neo4jResourceRepository.findByResourceId(resource.id).get()

        // update all the properties
        found.label = resource.label

        return neo4jResourceRepository.save(found).toResource()
    }
}
