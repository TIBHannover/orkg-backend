package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

const val RESOURCE_ID_TO_RESOURCE_CACHE = "resource-id-to-resource"
const val RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE = "resource-id-to-resource-exists"

@Component
@CacheConfig(cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
class SpringDataNeo4jResourceAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator
) : ResourceRepository {
    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource? =
        neo4jRepository.findByIdAndClassesContaining(id.toResourceId(), classes)?.toResource()

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jResourceIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByResourceId(id.toResourceId()))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#resource.id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE]),
            CacheEvict(key = "#resource.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(resource: Resource) {
        neo4jRepository.save(resource.toNeo4jResource())
    }

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteByResourceId(id: ThingId) {
        neo4jRepository.deleteByResourceId(id.toResourceId())
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
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsByResourceId(id.toResourceId())

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE])
    override fun findByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findByResourceId(id.toResourceId()).map(Neo4jResource::toResource)

    override fun findAllByLabel(label: String): Iterable<Resource> =
        neo4jRepository.findAllByLabel(label).map(Neo4jResource::toResource)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByLabelContaining(part, pageable).map(Neo4jResource::toResource)

    override fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClass(`class`.toClassId(), pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(
        `class`: ThingId,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`.toClassId(), createdBy, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(`class`: ThingId, label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndLabel(`class`.toClassId(), label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`.toClassId(), createdBy, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelMatchesRegex(`class`: ThingId, label: String, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelMatchesRegex(`class`.toClassId(), label, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelMatchesRegexAndCreatedBy(
        `class`: ThingId,
        label: String,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndLabelMatchesRegexAndCreatedBy(`class`.toClassId(), label, createdBy, pageable)
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

    override fun findByLabel(label: String): Optional<Resource> =
        neo4jRepository.findByLabel(label).map(Neo4jResource::toResource)

    override fun findByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findByClassAndObservatoryId(`class`.toClassId(), id).map(Neo4jResource::toResource)

    override fun findPaperByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findPaperByResourceId(id.toResourceId())
            .map(Neo4jResource::toResource)

    override fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllPapersByVerified(verified, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByVisibility(visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListed(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListed(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllPapersByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllPapersByVisibility(visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedPapers(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedPapers(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassInAndVisibility(classes.toClassIds(), visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedByClassIn(classes.toClassIds(), pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: Visibility,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassInAndVisibilityAndObservatoryId(classes.toClassIds(), visibility, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassInAndObservatoryId(
        classes: Set<ThingId>,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllListedByClassInAndObservatoryId(classes.toClassIds(), id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllContributorIds(pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorIds(pageable).map(::ContributorId)

    override fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findComparisonsByOrganizationId(id, pageable).map(Neo4jResource::toResource)

    private fun Resource.toNeo4jResource() =
        // We need to fetch the original resource, so "resources" is set properly.
        neo4jRepository.findByResourceId(id.toResourceId()).orElse(Neo4jResource()).apply {
            resourceId = this@toNeo4jResource.id.toResourceId()
            label = this@toNeo4jResource.label
            createdBy = this@toNeo4jResource.createdBy
            createdAt = this@toNeo4jResource.createdAt
            observatoryId = this@toNeo4jResource.observatoryId
            extractionMethod = this@toNeo4jResource.extractionMethod
            verified = this@toNeo4jResource.verified
            visibility = this@toNeo4jResource.visibility
            organizationId = this@toNeo4jResource.organizationId
            classes = this@toNeo4jResource.classes
        }
}
