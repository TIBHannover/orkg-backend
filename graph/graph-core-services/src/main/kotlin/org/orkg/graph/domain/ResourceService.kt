package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.List
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase.UpdateCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ResourceService(
    private val repository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository,
    private val contributorRepository: ContributorRepository,
    private val thingRepository: ThingRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository
) : ResourceUseCases {
    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun create(command: CreateResourceUseCase.CreateCommand): ThingId {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        validateClasses(command.classes)
        command.id?.also { id -> repository.findById(id).ifPresent { throw ResourceAlreadyExists(id) } }
        return unsafeResourceUseCases.create(command)
    }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        baseClass: ThingId?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<Resource> =
        repository.findAll(
            pageable = pageable,
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            includeClasses = includeClasses,
            excludeClasses = excludeClasses,
            baseClass = baseClass,
            observatoryId = observatoryId,
            organizationId = organizationId
        )

    override fun findById(id: ThingId): Optional<Resource> =
        repository.findById(id)

    override fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource> =
        statementRepository.findByDOI(doi, classes)

    override fun findPaperByTitle(title: String): Optional<Resource> =
        repository.findPaperByLabel(title)

    override fun findAllPapersByTitle(title: String?): List<Resource> =
        repository.findAllPapersByLabel(title!!)

    override fun findAllProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        statementRepository.findAllProblemsByObservatoryId(id, pageable)

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> repository.findAllListedByClassInAndObservatoryId(classes, id, pageable)
            VisibilityFilter.UNLISTED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.UNLISTED, id, pageable)
            VisibilityFilter.FEATURED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.FEATURED, id, pageable)
            VisibilityFilter.NON_FEATURED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.DEFAULT, id, pageable)
            VisibilityFilter.DELETED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.DELETED, id, pageable)
        }

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        repository.findById(id)
            .map { statementRepository.findTimelineByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource> =
        statementRepository.findAllPapersByObservatoryIdAndFilters(observatoryId, filters, visibility, pageable)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        repository.findById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun update(command: UpdateCommand) {
        if (command.hasNoContents()) return
        val resource = repository.findById(command.id)
            .orElseThrow { ResourceNotFound.withId(command.id) }
        if (!resource.modifiable) {
            throw ResourceNotModifiable(command.id)
        }
        command.label?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        command.classes?.also { validateClasses(it) }
        command.observatoryId?.let { observatoryId ->
            if (observatoryId != resource.observatoryId && observatoryId != ObservatoryId.UNKNOWN && !observatoryRepository.existsById(observatoryId)) {
                throw ObservatoryNotFound(observatoryId)
            }
        }
        command.organizationId?.let { organizationId ->
            if (organizationId != resource.organizationId && organizationId != OrganizationId.UNKNOWN && organizationRepository.findById(organizationId).isEmpty) {
                throw OrganizationNotFound(organizationId)
            }
        }
        val contributor by lazy {
            contributorRepository.findById(command.contributorId)
                .orElseThrow { ContributorNotFound(command.contributorId) }
        }
        if (command.visibility != null && command.visibility != resource.visibility) {
            if (!resource.isOwnedBy(command.contributorId) || !isAllowedVisibilityChangeByOwner(command.visibility!!, resource.visibility)) {
                if (!contributor.isCurator) {
                    throw NeitherOwnerNorCurator.cannotChangeVisibility(resource.id)
                }
            }
        }
        if (command.verified != null && command.verified != resource.verified && !contributor.isCurator) {
            throw NotACurator.cannotChangeVerifiedStatus(contributor.id)
        }
        val updated = resource.apply(command)
        if (updated != resource) {
            repository.save(updated)
        }
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        val resource = repository.findById(id).orElseThrow { ResourceNotFound.withId(id) }

        if (!resource.modifiable)
            throw ResourceNotModifiable(resource.id)

        if (thingRepository.isUsedAsObject(resource.id))
            throw ResourceInUse(resource.id)

        if (!resource.isOwnedBy(contributorId)) {
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) throw NeitherOwnerNorCurator(contributorId)
        }

        unsafeResourceUseCases.delete(id, contributorId)
    }

    override fun deleteAll() = repository.deleteAll()

    override fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource> = when {
        classes.isNotEmpty() -> when (visibility) {
            VisibilityFilter.ALL_LISTED -> repository.findAllListedByClassIn(classes, pageable)
            VisibilityFilter.UNLISTED -> repository.findAllByClassInAndVisibility(classes, Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> repository.findAllByClassInAndVisibility(classes, Visibility.FEATURED, pageable)
            VisibilityFilter.NON_FEATURED -> repository.findAllByClassInAndVisibility(classes, Visibility.DEFAULT, pageable)
            VisibilityFilter.DELETED -> repository.findAllByClassInAndVisibility(classes, Visibility.DELETED, pageable)
        }
        else -> findAll(visibility = visibility, pageable = pageable)
    }

    override fun findAllContributorIds(pageable: Pageable) = repository.findAllContributorIds(pageable)

    override fun markAsVerified(resourceId: ThingId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ThingId) = setVerifiedFlag(resourceId, false)

    override fun markAsFeatured(resourceId: ThingId) {
        val resource = repository.findById(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.FEATURED,
            unlistedBy = null
        )
        repository.save(modified)
    }

    override fun markAsNonFeatured(resourceId: ThingId) {
        val resource = repository.findById(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.DEFAULT,
            unlistedBy = null
        )
        repository.save(modified)
    }

    override fun markAsUnlisted(resourceId: ThingId, contributorId: ContributorId) {
        val resource = repository.findById(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.UNLISTED,
            unlistedBy = contributorId
        )
        repository.save(modified)
    }

    override fun markAsListed(resourceId: ThingId) {
        val resource = repository.findById(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.DEFAULT,
            unlistedBy = null
        )
        repository.save(modified)
    }

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        statementRepository.findAllProblemsByOrganizationId(id, pageable)

    private fun setVerifiedFlag(resourceId: ThingId, verified: Boolean) {
        val result = repository.findById(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound.withId(resourceId) }
        resultObj = resultObj.copy(verified = verified)
        repository.save(resultObj)
    }

    private fun validateClasses(classes: Set<ThingId>): Set<ThingId> {
        if (classes.isNotEmpty()) {
            val reserved = classes.intersect(reservedClassIds)
            if (reserved.isNotEmpty()) {
                throw ReservedClass(reserved.first())
            }
            if (!classRepository.existsAllById(classes - reservedClassIds)) {
                throw InvalidClassCollection(classes)
            }
        }
        return classes
    }

    private fun isAllowedVisibilityChangeByOwner(source: Visibility, target: Visibility) =
        source == Visibility.DELETED && target == Visibility.DEFAULT || // allow restoring deleted resources
            target == Visibility.DELETED // allow deletion of resources from any state
}
