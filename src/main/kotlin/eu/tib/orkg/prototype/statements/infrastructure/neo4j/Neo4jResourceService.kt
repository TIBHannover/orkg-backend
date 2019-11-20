package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
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

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), "(?i).*$part.*", pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.map { it.value }, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(ids.map { it.value }, "(?i).*$part.*", pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun update(request: UpdateResourceRequest): Resource {
        // already checked by service
        val found = neo4jResourceRepository.findByResourceId(request.id).get()

        // update all the properties
        if (request.label != null)
            found.label = request.label
        if (request.classes != null)
            found.classes = request.classes

        return neo4jResourceRepository.save(found).toResource()
    }
}
