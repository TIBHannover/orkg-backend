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
    // Legacy methods:
    fun findAll(pageable: Pageable): Page<Resource>
    fun findAllByClass(pageable: Pageable, id: ClassId): Page<Resource>
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ClassId, createdBy: ContributorId): Page<Resource>
    fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<Resource>

    fun findAllByClassAndLabelContaining(pageable: Pageable, id: ClassId, part: String): Page<Resource>
    fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<Resource>

    fun findAllByDOI(doi: String): Iterable<Resource>
    fun findAllByFeatured(pageable: Pageable): Page<Resource>
    fun findAllByLabel(pageable: Pageable, label: String): Page<Resource>
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Resource>
    fun findAllByListed(pageable: Pageable): Page<Resource>
    fun findAllByNonFeatured(pageable: Pageable): Page<Resource>
    fun findAllByTitle(title: String?): Iterable<Resource>
    fun findAllByUnlisted(pageable: Pageable): Page<Resource>
    fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<Resource>
    fun findAllExcludingClassByLabel(pageable: Pageable, ids: Array<ClassId>, label: String): Page<Resource>
    fun findAllExcludingClassByLabelContaining(pageable: Pageable, ids: Array<ClassId>, part: String): Page<Resource>
    fun findByDOI(doi: String): Optional<Resource>
    fun findById(id: ResourceId?): Optional<Resource>
    fun findByTitle(title: String?): Optional<Resource>
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun getResourcesByClasses(
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource>
    fun hasStatements(id: ResourceId): Boolean
}
