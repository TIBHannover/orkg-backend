package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.stringify
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val RESOURCE_ID_TO_RESOURCE_CACHE = "resource-id-to-resource"
const val RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE = "resource-id-to-resource-exists"

@Component
@CacheConfig(cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
class SpringDataNeo4jResourceAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
    private val neo4jClient: Neo4jClient
) : ResourceRepository {
    override fun findByIdAndClasses(id: ResourceId, classes: Set<ThingId>): Resource? =
        neo4jRepository.findByIdAndClassesContaining(id, classes.stringify())?.toResource()

    override fun nextIdentity(): ResourceId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ResourceId
        do {
            id = neo4jResourceIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByResourceId(id))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#resource.id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE]),
            CacheEvict(key = "#resource.id.value", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(resource: Resource) {
        neo4jRepository.save(resource.toNeo4jResource())
    }

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id.value", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteByResourceId(id: ResourceId) {
        neo4jRepository.deleteByResourceId(id)
    }

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAll(pageable).map(Neo4jResource::toResource)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
    override fun exists(id: ResourceId): Boolean = neo4jRepository.existsByResourceId(id)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE])
    override fun findByResourceId(id: ResourceId?): Optional<Resource> =
        neo4jRepository.findByResourceId(id).map(Neo4jResource::toResource)

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

    override fun findAllByClassAndLabelMatchesRegex(`class`: String, label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelMatchesRegex(`class`, label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelMatchesRegexAndCreatedBy(
        `class`: String,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelMatchesRegexAndCreatedBy(`class`, label, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClasses(
            includeClasses.toClassIds(),
            excludeClasses.toClassIds(),
            pageable
        ).map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClassesByLabel(
            includeClasses.toClassIds(),
            excludeClasses.toClassIds(),
            label, pageable
        ).map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClassesByLabelMatchesRegex(
            includeClasses.toClassIds(),
            excludeClasses.toClassIds(),
            label,
            pageable
        ).map(Neo4jResource::toResource)

    override fun findByLabel(label: String?): Optional<Resource> =
        neo4jRepository.findByLabel(label).map(Neo4jResource::toResource)

    override fun findByClassAndObservatoryId(`class`: String, id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findByClassAndObservatoryId(`class`, id).map(Neo4jResource::toResource)

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
        neo4jRepository.findAllFeaturedResourcesByClass(classes, unlisted, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllFeaturedResourcesByClass(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> = neo4jRepository.findAllFeaturedResourcesByClass(classes, featured, unlisted, pageable)
        .map(Neo4jResource::toResource)

    override fun findAllFeaturedResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllFeaturedResourcesByObservatoryIdAndClass(id, classes, featured, unlisted, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllResourcesByObservatoryIdAndClass(id, classes, unlisted, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllContributorIds(pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorIds(pageable).map(::ContributorId)

    override fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findComparisonsByOrganizationId(id, pageable).map(Neo4jResource::toResource)

    private fun Resource.toNeo4jResource() =
        // We need to fetch the original resource, so "resources" is set properly.
        neo4jRepository.findByResourceId(id!!).orElse(Neo4jResource()).apply {
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
}
