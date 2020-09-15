package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import eu.tib.orkg.prototype.util.EscapedRegex
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResourceService(
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator
) : ResourceService {

    override fun create(label: String) = create(UUID(0, 0), label, UUID(0, 0), ExtractionMethod.UNKNOWN, UUID(0, 0))

    override fun create(userId: UUID, label: String, observatoryId: UUID, extractionMethod: ExtractionMethod, organizationId: UUID): Resource {
        val resourceId = neo4jResourceIdGenerator.nextIdentity()
        return neo4jResourceRepository.save(Neo4jResource(label = label, resourceId = resourceId, createdBy = userId, observatoryId = observatoryId, extractionMethod = extractionMethod, organizationId = organizationId))
            .toResource()
    }

    override fun create(request: CreateResourceRequest) = create(UUID(0, 0), request, UUID(0, 0), ExtractionMethod.UNKNOWN, UUID(0, 0))

    override fun create(userId: UUID, request: CreateResourceRequest, observatoryId: UUID, extractionMethod: ExtractionMethod, organizationId: UUID): Resource {
        val id = request.id ?: neo4jResourceIdGenerator.nextIdentity()
        val resource = Neo4jResource(label = request.label, resourceId = id, createdBy = userId, observatoryId = observatoryId, extractionMethod = extractionMethod, organizationId = organizationId)
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
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i)^${EscapedRegex(label)}$", pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Resource> {
        val cleaned = part.trim().replace("""\s+""".toRegex(), " ")
        val regex = "(?i).*${"${EscapedRegex(cleaned)}".replace(" ", "\\s+")}.*"
        return neo4jResourceRepository.findAllByLabelMatchesRegex(regex, pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)
    }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), "(?i).*${escapeRegexString(part)}.*", pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id.toString(), "(?i).*${escapeRegexString(part)}.*", createdBy, pageable)
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
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(ids.map { it.value }, "(?i).*${escapeRegexString(part)}.*", pageable)
            .content
            .map(Neo4jResource::toResource)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jResourceRepository.findByDOI(doi)
            .map(Neo4jResource::toResource)

    override fun findByTitle(title: String?): Optional<Resource> =
        neo4jResourceRepository.findByLabel(title)
            .map(Neo4jResource::toResource)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByDOI(doi)
            .map(Neo4jResource::toResource)

    override fun findAllByTitle(title: String?): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(title!!)
            .map(Neo4jResource::toResource)

    override fun findPapersByObservatoryId(id: UUID): Iterable<Resource> =
        neo4jResourceRepository.findPapersByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findComparisonsByObservatoryId(id: UUID): Iterable<Resource> =
        neo4jResourceRepository.findComparisonsByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: UUID): Iterable<Resource> =
        neo4jResourceRepository.findProblemsByObservatoryId(id)
            .map(Neo4jResource::toResource)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        neo4jResourceRepository.findContributorsByResourceId(id)

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

    override fun hasStatements(id: ResourceId) =
        neo4jResourceRepository.checkIfResourceHasStatements(id)

    override fun delete(id: ResourceId) {
        val found = neo4jResourceRepository.findByResourceId(id).get()
        neo4jResourceRepository.delete(found)
    }
}
