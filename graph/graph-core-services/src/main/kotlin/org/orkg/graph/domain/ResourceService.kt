package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
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
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.collections.List

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
    private val organizationRepository: OrganizationRepository,
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
        organizationId: OrganizationId?,
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

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        repository.findById(id)
            .map { statementRepository.findTimelineByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound(id) }

    override fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable,
    ): Page<Resource> =
        statementRepository.findAllPapersByObservatoryIdAndFilters(observatoryId, filters, visibility, pageable)

    override fun findAllProblemsByOrganizationId(
        id: OrganizationId,
        pageable: Pageable,
    ): Page<Resource> =
        statementRepository.findAllProblemsByOrganizationId(id, pageable)

    override fun update(command: UpdateCommand) {
        if (command.hasNoContents()) return
        val resource = repository.findById(command.id)
            .orElseThrow { ResourceNotFound(command.id) }
        if (!resource.modifiable) {
            throw ResourceNotModifiable(command.id)
        }
        if (Classes.rosettaStoneStatement in resource.classes) {
            throw RosettaStoneStatementResourceNotModifiable(command.id)
        }
        command.label?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        command.classes?.also { validateClasses(it) }
        command.observatoryId?.also { observatoryId ->
            if (observatoryId != resource.observatoryId && observatoryId != ObservatoryId.UNKNOWN && !observatoryRepository.existsById(observatoryId)) {
                throw ObservatoryNotFound(observatoryId)
            }
        }
        command.organizationId?.also { organizationId ->
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
                    throw NeitherOwnerNorCurator.cannotChangeVisibility(resource.createdBy, command.contributorId, resource.id)
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
        val resource = repository.findById(id).orElseThrow { ResourceNotFound(id) }

        if (!resource.modifiable) {
            throw ResourceNotModifiable(resource.id)
        }

        if (thingRepository.isUsedAsObject(resource.id)) {
            throw ResourceInUse(resource.id)
        }

        if (!resource.isOwnedBy(contributorId)) {
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) throw NeitherOwnerNorCurator(resource.createdBy, contributorId, resource.id)
        }

        unsafeResourceUseCases.delete(id, contributorId)
    }

    override fun deleteAll() = repository.deleteAll()

    override fun findAllContributorIds(pageable: Pageable) = repository.findAllContributorIds(pageable)

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
        // allow restoring deleted resources
        source == Visibility.DELETED &&
            target == Visibility.DEFAULT ||
            // allow deletion of resources from any state
            target == Visibility.DELETED
}
