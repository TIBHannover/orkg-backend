package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.stringify
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
    override fun findByIdAndClasses(id: ResourceId, classes: Set<ClassId>): Resource? =
        neo4jRepository.findByIdAndClassesContaining(id, classes.stringify())?.toResource()

    override fun nextIdentity(): ResourceId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ResourceId
        do {
            id = neo4jResourceIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByResourceId(id))
        return id
    }

    override fun save(resource: Resource): Resource = neo4jRepository.save(resource.toNeo4jResource()).toResource()

    override fun deleteByResourceId(id: ResourceId) {
        neo4jRepository.deleteByResourceId(id)
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAll(pageable).map(Neo4jResource::toResource)

    override fun exists(id: ResourceId): Boolean = neo4jRepository.existsByResourceId(id)

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
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClasses(includeClasses, excludeClasses, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClassesByLabel(includeClasses, excludeClasses, label, pageable).map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(
        includeClasses: Set<ClassId>,
        excludeClasses: Set<ClassId>,
        label: String,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClassesByLabelMatchesRegex(includeClasses, excludeClasses, label, pageable)
            .map(Neo4jResource::toResource)

    override fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long> =
        neo4jRepository.getIncomingStatementsCount(ids)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jRepository.findByDOI(doi).map(Neo4jResource::toResource)

    override fun findAllByDOI(doi: String): Iterable<Resource> =
        neo4jRepository.findAllByDOI(doi).map(Neo4jResource::toResource)

    override fun findByLabel(label: String?): Optional<Resource> =
        neo4jRepository.findByLabel(label).map(Neo4jResource::toResource)

    override fun findByClassAndObservatoryId(`class`: String, id: ObservatoryId): Iterable<Resource> =
        neo4jRepository.findByClassAndObservatoryId(`class`, id).map(Neo4jResource::toResource)

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

    override fun findAllFeaturedResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> = neo4jRepository.findAllFeaturedResourcesByObservatoryIdAndClass(id, classes, featured, unlisted, pageable).map(Neo4jResource::toResource)

    override fun findAllResourcesByObservatoryIDAndClass(
        id: ObservatoryId,
        classes: List<String>,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> = neo4jRepository.findAllResourcesByObservatoryIdAndClass(id, classes, unlisted, pageable).map(Neo4jResource::toResource)

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
