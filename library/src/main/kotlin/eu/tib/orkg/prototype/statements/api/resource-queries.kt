package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResourceUseCase {
    fun exists(id: ResourceId): Boolean
    fun findByIdAndClasses(id: ResourceId, classes: Set<ClassId>): ResourceRepresentation?

    // TODO: Needed by problem service. May need better solution.
    fun map(action: IterableResourcesGenerator): Iterable<ResourceRepresentation>
    fun map(action: PagedResourcesGenerator): Page<ResourceRepresentation>
    fun map(action: ResourceGenerator): ResourceRepresentation

    // Legacy methods:
    fun findAll(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByClass(pageable: Pageable, id: ClassId): Page<ResourceRepresentation>
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<ResourceRepresentation>
    fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<ResourceRepresentation>
    fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation>

    fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<ResourceRepresentation>
    fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation>

    fun findAllByDOI(doi: String): Iterable<ResourceRepresentation>
    fun findAllByFeatured(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByLabel(pageable: Pageable, label: String): Page<ResourceRepresentation>
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ResourceRepresentation>
    fun findAllByListed(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByNonFeatured(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByTitle(title: String?): Iterable<ResourceRepresentation>
    fun findAllByUnlisted(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<ResourceRepresentation>
    fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<ResourceRepresentation>
    fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Page<ResourceRepresentation>
    fun findByDOI(doi: String): Optional<ResourceRepresentation>
    fun findById(id: ResourceId?): Optional<ResourceRepresentation>
    fun findByTitle(title: String?): Optional<ResourceRepresentation>
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation>
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation>
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation>
    fun findResourcesByObservatoryIdAndClass(id: ObservatoryId, classes: List<String>, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>
    fun getResourcesByClasses(
        classes: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Optional<Page<ResourceRepresentation>>
    fun hasStatements(id: ResourceId): Boolean
}

fun interface ResourceGenerator {
    fun generate(): Resource
}

fun interface IterableResourcesGenerator {
    fun generate(): Iterable<Resource>
}

fun interface PagedResourcesGenerator {
    fun generate(): Page<Resource>
}
