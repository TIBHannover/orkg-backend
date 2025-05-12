package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface ResourceUseCases :
    CreateResourceUseCase,
    UpdateResourceUseCase,
    DeleteResourceUseCase,
    RetrieveResourceUseCase,
    OtherResourceUseCases

interface UnsafeResourceUseCases :
    CreateResourceUseCase,
    UpdateResourceUseCase,
    DeleteResourceUseCase

// FIXME: we need to refactor those as well
interface OtherResourceUseCases : RetrieveContributorUseCase

interface RetrieveResourceUseCase {
    fun existsById(id: ThingId): Boolean

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        includeClasses: Set<ThingId> = emptySet(),
        excludeClasses: Set<ThingId> = emptySet(),
        baseClass: ThingId? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<Resource>

    // Legacy methods:
    fun findAllPapersByTitle(title: String?): List<Resource>

    fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource>

    /** Find a paper resource by DOI. */
    fun findById(id: ThingId): Optional<Resource>

    fun findPaperByTitle(title: String): Optional<Resource>

    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>

    fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
}

interface CreateResourceUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val contributorId: ContributorId,
        val label: String,
        val classes: Set<ThingId> = emptySet(),
        val extractionMethod: ExtractionMethod? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
        val modifiable: Boolean = true,
    )
}

interface UpdateResourceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val classes: Set<ThingId>? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
        val extractionMethod: ExtractionMethod? = null,
        val modifiable: Boolean? = null,
        val visibility: Visibility? = null,
        val verified: Boolean? = null,
    )
}

interface DeleteResourceUseCase {
    // legacy methods:
    fun delete(id: ThingId, contributorId: ContributorId)

    fun deleteAll()
}

interface RetrieveContributorUseCase {
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
}
