package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ResourcePersistenceAdapter(private val neo4jResourceRepository: Neo4jResourceRepository) :
    ResourceRepository {

    override fun save(resource: Resource): Resource =
        neo4jResourceRepository.save(resource.toNeo4jResource()).toResource()

    override fun count(): Long = neo4jResourceRepository.count()

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAll(pageable).map(Neo4jResource::toResource)

    override fun findById(resourceId: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(resourceId)
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Resource> =
        neo4jResourceRepository.findAllByLabel(label, pageable).map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource> =
        neo4jResourceRepository.findAllByLabelContaining(part, pageable).map(Neo4jResource::toResource)

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), part, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(id.toString(), part, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.toList().map(ClassId::toString), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.toList().map(ClassId::toString), label, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(
        pageable: Pageable,
        ids: Array<ClassId>,
        part: String
    ): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(
            ids.toList().map(ClassId::toString),
            part,
            pageable
        ).map(Neo4jResource::toResource)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jResourceRepository.findByDOI(doi).map(Neo4jResource::toResource)

    override fun findByTitle(title: String?): Optional<Resource> =
        neo4jResourceRepository.findByLabel(title).map(Neo4jResource::toResource)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByDOI(doi).map(Neo4jResource::toResource)

    override fun findAllByTitle(title: String?): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(title!!).map(Neo4jResource::toResource)

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findPapersByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findComparisonsByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findProblemsByObservatoryId(id).map(Neo4jResource::toResource)
}

internal fun Resource.toNeo4jResource(): Neo4jResource =
    Neo4jResource(
        resourceId = this.id!!,
        label = this.label,
        createdBy = this.createdBy,
        observatoryId = this.observatoryId,
        extractionMethod = this.extractionMethod,
        organizationId = this.organizationId
    )
