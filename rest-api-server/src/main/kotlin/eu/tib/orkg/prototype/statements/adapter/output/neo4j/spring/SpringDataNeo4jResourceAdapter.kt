package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResourceAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
) : ResourceRepository {
    override fun nextIdentity(): ResourceId = neo4jResourceIdGenerator.nextIdentity()

    override fun save(resource: Resource): Resource =
        neo4jRepository.save<Neo4jResource?>(resource.toNeo4jResource()).toResource()

    override fun delete(id: ResourceId) {
        neo4jRepository.findByResourceId(id).ifPresent {
            neo4jRepository.delete(it)
        }
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(): Iterable<Resource> = neo4jRepository.findAll().map(Neo4jResource::toResource)

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAll(pageable).map(Neo4jResource::toResource)

    override fun findByResourceId(id: ResourceId?): Optional<Resource> =
        neo4jRepository.findByResourceId(id).map(Neo4jResource::toResource)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByLabel(label, pageable).map(Neo4jResource::toResource)

    override fun findAllByLabel(label: String): Iterable<Resource> =
        neo4jRepository.findAllByLabel(label).map(Neo4jResource::toResource)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jResource::toResource)

    override fun findAllByClass(`class`: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClass(`class`, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(
        `class`: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`, createdBy, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndLabel(`class`, label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`, createdBy, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContaining(`class`: String, label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelContaining(`class`, label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelContainingAndCreatedBy(`class`, label, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllExcludingClass(classes, pageable).map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabel(
        classes: List<String>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllExcludingClassByLabel(classes, label, pageable).map(Neo4jResource::toResource)

    override fun findAllExcludingClassByLabelContaining(
        classes: List<String>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllExcludingClassByLabelContaining(classes, label, pageable).map(Neo4jResource::toResource)

    override fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long> =
        neo4jRepository.getIncomingStatementsCount(ids)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jRepository.findByDOI(doi).map(Neo4jResource::toResource)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jRepository.findAllByDOI(doi).map(Neo4jResource::toResource)

    override fun findByLabel(label: String?): Optional<Resource> =
        neo4jRepository.findByLabel(label).map(Neo4jResource::toResource)

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findPapersByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findComparisonsByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findProblemsByObservatoryId(id).map(Neo4jResource::toResource)

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        neo4jRepository.findContributorsByResourceId(id)

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean =
        neo4jRepository.checkIfResourceHasStatements(id)

    override fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByVerifiedIsTrue(pageable).map(Neo4jResource::toResource)

    override fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByVerifiedIsFalse(pageable).map(Neo4jResource::toResource)

    override fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByFeaturedIsTrue(pageable).map(Neo4jResource::toResource)

    override fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByFeaturedIsFalse(pageable).map(Neo4jResource::toResource)

    override fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByUnlistedIsTrue(pageable).map(Neo4jResource::toResource)

    override fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByUnlistedIsFalse(pageable).map(Neo4jResource::toResource)

    override fun findPaperByResourceId(id: ResourceId): Optional<Resource> =
        neo4jRepository.findPaperByResourceId(id).map(Neo4jResource::toResource)

    override fun findAllVerifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllVerifiedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllUnverifiedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllFeaturedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllFeaturedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllNonFeaturedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllUnlistedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllUnlistedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllListedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedPapers(pageable).map(Neo4jResource::toResource)

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllFeaturedResourcesByClass(classes, unlisted, pageable).map(Neo4jResource::toResource)

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> = neo4jRepository.findAllFeaturedResourcesByClass(classes, featured, unlisted, pageable)
        .map(Neo4jResource::toResource)
}

private fun Resource.toNeo4jResource() = Neo4jResource().apply {
    resourceId = this@toNeo4jResource.id
    label = this@toNeo4jResource.label
    createdBy = this@toNeo4jResource.createdBy
    createdAt = this@toNeo4jResource.createdAt
    observatoryId = this@toNeo4jResource.observatoryId
    extractionMethod = this@toNeo4jResource.extractionMethod
    verified = this@toNeo4jResource.verified
    featured = this@toNeo4jResource.featured
    unlisted = this@toNeo4jResource.unlisted
    organizationId = this@toNeo4jResource.organizationId
    classes = this@toNeo4jResource.classes
}