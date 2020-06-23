package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import java.util.Optional
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResourceService(
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator
) : ResourceService {

    @Autowired
    private lateinit var statementService: Neo4jStatementService

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
            .map { it.toFormatted() }

    override fun findById(id: ResourceId?, formatted: Boolean): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(id)
            .map(Neo4jResource::toResource)
            .map { if (formatted) it.toFormatted() else it }

    override fun findById(id: ResourceId?): Optional<Resource> =
        this.findById(id, true)

    override fun findAllByLabel(pageable: Pageable, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i)^${escapeRegexString(label)}$", pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelMatchesRegex("(?i).*${escapeRegexString(part)}.*", pageable) // TODO: See declaration
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Iterable<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClassAndLabelAndCreatedBy(pageable: Pageable, id: ClassId, label: String, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), "(?i).*${escapeRegexString(part)}.*", pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByClassAndLabelContainingAndCreatedBy(pageable: Pageable, id: ClassId, part: String, createdBy: UUID): Iterable<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id.toString(), "(?i).*${escapeRegexString(part)}.*", createdBy, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.map { it.value }, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(ids.map { it.value }, "(?i).*${escapeRegexString(part)}.*", pageable)
            .content
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jResourceRepository.findByDOI(doi)
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findByTitle(title: String?): Optional<Resource> =
        neo4jResourceRepository.findByLabel(title)
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByDOI(doi)
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByTitle(title: String?): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(title!!)
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

    override fun findAllByObservatoryId(id: UUID): Iterable<Resource> =
        neo4jResourceRepository.findByObservatoryId(id)
            .map(Neo4jResource::toResource)
            .map { it.toFormatted() }

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

        return neo4jResourceRepository.save(found).toResource().toFormatted()
    }

    fun Resource.toFormatted(): Resource {
        if (this.classes.isNotEmpty()) {
            val pagination = createPageable(1, 10, null, false)
            val classId = this.classes.first()
            // Check if the instance is of a templated class
            val found = statementService.findTemplate(classId)
            if (!found.isPresent) return this
            // Check if the templated class has format option
            val format = statementService.checkIfTemplateIsFormatted(found.get().id!!)
            if (!format.isPresent) return this
            // Get format rule
            val formatRule = format.get()
            // Get all statements of the current resource to format the label
            val statements = statementService.findAllBySubject(
                subjectId = this.id!!.value,
                pagination = pagination,
                formatted = false
            )
            if (statements.count() == 0) return this
            // Create a map with predicate -> value
            val properties = statements.map {
                it.predicate.id!!.value to
                    (it.`object` as Literal).label
            }.toMap()
            this.formattedLabel = formatLabel(formatRule, properties)
        }
        return this
    }

    private fun formatLabel(
        formatRule: Literal,
        properties: Map<String, String>
    ): String {
        // Catch JS/Python string format patterns and replace them
        val pattern = """\{\w*}""".toRegex()
        val matches = pattern.findAll(formatRule.label)
        var formattedString = formatRule.label
        matches.forEach {
            val predId = formatRule.label.substring(
                startIndex = it.groups.first()!!.range.first + 1,
                endIndex = it.groups.first()!!.range.last
            )
            if (properties.containsKey(predId))
                formattedString =
                    formattedString.replaceFirst("{$predId}", properties[predId]
                        ?: error("Predicate not found"))
        }
        return formattedString
    }
}
