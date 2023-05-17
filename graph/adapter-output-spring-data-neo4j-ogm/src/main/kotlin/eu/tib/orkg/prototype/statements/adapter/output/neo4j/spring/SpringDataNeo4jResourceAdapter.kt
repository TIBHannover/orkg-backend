package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
) : ResourceRepository {
    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource? =
        neo4jRepository.findByIdAndClassesContaining(id, classes)?.toResource()

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jResourceIdGenerator.nextIdentity()
        } while (neo4jRepository.existsById(id))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#resource.id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE]),
            CacheEvict(key = "#resource.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(resource: Resource) {
        neo4jRepository.save(resource.toNeo4jResource(neo4jRepository))
    }

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteById(id: ThingId) {
        neo4jRepository.deleteById(id)
    }

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE])
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAll(pageable).map(Neo4jResource::toResource)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE])
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map(Neo4jResource::toResource)

    override fun findAllPapersByLabel(label: String): Iterable<Resource> =
        neo4jRepository.findAllPapersByLabel(label).map(Neo4jResource::toResource)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByLabel(labelSearchString.value, pageable)
            is FuzzySearchString -> neo4jRepository.findAllByLabelContaining(labelSearchString.value, pageable)
        }.map(Neo4jResource::toResource)

    override fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClass(`class`, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(
        `class`: ThingId,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`, createdBy, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(`class`: ThingId, labelSearchString: SearchString, pageable: Pageable): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByClassAndLabel(
                `class` = `class`,
                label = labelSearchString.value,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByClassAndLabelContaining(
                `class` = `class`,
                label = labelSearchString.value,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        labelSearchString: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByClassAndLabelAndCreatedBy(
                `class` = `class`,
                label = labelSearchString.value,
                createdBy = createdBy,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByClassAndLabelContainingAndCreatedBy(
                `class` = `class`,
                label = labelSearchString.value,
                createdBy = createdBy,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClasses(
            includeClasses,
            excludeClasses,
            pageable
        ).map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllIncludingAndExcludingClassesByLabel(
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                label = labelSearchString.value,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllIncludingAndExcludingClassesByLabelContaining(
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                label = labelSearchString.value,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findPaperByLabel(label: String): Optional<Resource> =
        neo4jRepository.findPaperByLabel(label).map(Neo4jResource::toResource)

    override fun findByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findByClassAndObservatoryId(`class`, id).map(Neo4jResource::toResource)

    override fun findPaperById(id: ThingId): Optional<Resource> =
        neo4jRepository.findPaperById(id)
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
        neo4jRepository.findAllByClassInAndVisibility(classes, visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedByClassIn(classes, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: Visibility,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassInAndVisibilityAndObservatoryId(classes, visibility, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassInAndObservatoryId(
        classes: Set<ThingId>,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllListedByClassInAndObservatoryId(classes, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllContributorIds(pageable: Pageable) =
        neo4jRepository.findAllContributorIds(pageable)

    override fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findComparisonsByOrganizationId(id, pageable).map(Neo4jResource::toResource)
}
