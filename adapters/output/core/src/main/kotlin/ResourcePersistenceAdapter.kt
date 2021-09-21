package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository.ResourceContributors
import eu.tib.orkg.prototype.util.toExactSearchString
import eu.tib.orkg.prototype.util.toSearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ResourcePersistenceAdapter(
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
    private val client: Neo4jClient
) : ResourceRepository {

    override fun nextIdentity(): ResourceId = neo4jResourceIdGenerator.nextIdentity()

    override fun save(resource: Resource): Resource =
        neo4jResourceRepository.save(resource.toNeo4jResource()).toResource()

    override fun count(): Long = neo4jResourceRepository.count()

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAll(pageable).map(Neo4jResource::toResource)

    override fun findById(resourceId: ResourceId?): Optional<Resource> =
        neo4jResourceRepository.findByResourceId(resourceId)
            .map(Neo4jResource::toResource)

    fun findAllById(ids: List<ResourceId>): Iterable<Resource> =
        neo4jResourceRepository.findAllByResourceIdIn(ids)
            .map(Neo4jResource::toResource)

    override fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByLabel(label.toExactSearchString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByLabelExactly(label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(label.toExactSearchString())
            .map(Neo4jResource::toResource)

    override fun findByLabelExactly(label: String): Optional<Resource> {
        val result = neo4jResourceRepository.findAllByLabel(label.toExactSearchString())
            .map(Neo4jResource::toResource)
            .single()
        return Optional.of(result)
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource> =
        // TODO: Work-around because we do not have full-text indexing. Fix if we have.
        neo4jResourceRepository.findAllByLabelMatchesRegex(part.toSearchString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClass(id: ClassId, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByClass(id.toString(), pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(id: ClassId, createdBy: ContributorId, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(id: ClassId, label: String, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabel(id.toString(), label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        id: ClassId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelAndCreatedBy(id.toString(), label, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(id: ClassId, part: String, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContaining(id.toString(), part.toSearchString(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        id: ClassId,
        part: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jResourceRepository.findAllByClassAndLabelContainingAndCreatedBy(
            id.toString(),
            part.toSearchString(),
            createdBy,
            pageable
        )
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(ids: Array<ClassId>, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClass(ids.toList().map(ClassId::toString), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(ids: Array<ClassId>, label: String, pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabel(ids.toList().map(ClassId::toString), label, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(
        ids: Array<ClassId>,
        part: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jResourceRepository.findAllExcludingClassByLabelContaining(
            ids.toList().map(ClassId::toString),
            part.toSearchString(),
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

    override fun findPaperByResourceId(id: ResourceId): Optional<Resource> =
        neo4jResourceRepository.findPaperByResourceId(id).map(Neo4jResource::toResource)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findComparisonsByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jResourceRepository.findProblemsByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        client
            .query(
                """
                MATCH (n:Resource {resource_id: ${'$'}id})
                CALL apoc.path.subgraphAll(n, {relationshipFilter:'>'})
                YIELD relationships
                UNWIND relationships as rel
                WITH rel AS p, startNode(rel) AS s, endNode(rel) AS o, n
                WHERE p.created_by <> "00000000-0000-0000-0000-000000000000" AND p.created_at>=n.created_at
                RETURN n.resource_id AS id, (p.created_by) AS createdBy, MAX(p.created_at) AS createdAt
                ORDER BY createdAt
                """.trimIndent()
            )
            .bind(id).to("id")
            .fetchAs(ResourceContributors::class.java)
            .mappedBy { _, record ->
                ResourceContributors(
                    id = record.get("id").asString(),
                    createdBy = record.get("createdBy").asString(),
                    createdAt = record.get("createdAt").asString(),
                )
            }
            .all()

    override fun delete(id: ResourceId) = neo4jResourceRepository.deleteByResourceId(id)

    override fun findAllVerifiedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByVerifiedIsTrue(pageable).map(Neo4jResource::toResource)

    override fun findAllUnverifiedResources(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllByVerifiedIsFalse(pageable).map(Neo4jResource::toResource)

    override fun findAllVerifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllVerifiedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jResourceRepository.findAllUnverifiedPapers(pageable).map(Neo4jResource::toResource)

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean =
        neo4jResourceRepository.checkIfResourceHasStatements(id)
}

internal fun Resource.toNeo4jResource(): Neo4jResource =
    Neo4jResource(
        resourceId = this.id!!,
        label = this.label,
        createdBy = this.createdBy,
        observatoryId = this.observatoryId,
        extractionMethod = this.extractionMethod,
        organizationId = this.organizationId,
        verified = this.verified
    )
