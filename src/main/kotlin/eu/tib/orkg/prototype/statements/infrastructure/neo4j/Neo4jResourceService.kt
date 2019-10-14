package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import org.springframework.data.domain.Pageable

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

    override fun create(request: CreateResourceRequest): Resource {
        val id = request.id ?: neo4jResourceIdGenerator.nextIdentity()
        val resource = Neo4jResource(label = request.label, resourceId = id)
        request.classes.forEach { resource.assignTo(it.toString()) }
        return neo4jResourceRepository.save(resource).toResource()
    }

    override fun findAll(pageable: Pageable): Iterable<Resource> =
        neo4jResourceRepository
            .findAll(pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findById(id: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(id)
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(pageable: Pageable, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i)^$label$", pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i).*$part.*", pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClass(pageable: Pageable, id: ClassId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun update(resource: Resource): Resource {
        // already checked by service
        val found = neo4jResourceRepository.findByResourceId(resource.id).get()

        // update all the properties
        found.label = resource.label

        return neo4jResourceRepository.save(found).toResource()
    }
}
