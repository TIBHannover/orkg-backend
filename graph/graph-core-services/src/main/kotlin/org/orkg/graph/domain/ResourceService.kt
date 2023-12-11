package org.orkg.graph.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val paperClass = ThingId("Paper")
private val comparisonClass = ThingId("Comparison")
private val reservedClassIds = setOf(
    ThingId("Literal"),
    ThingId("Class"),
    ThingId("Predicate"),
    ThingId("Resource"),
    Classes.list
)

@Service
@Transactional
class ResourceService(
    private val repository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository
) : ResourceUseCases {
    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun create(command: CreateResourceUseCase.CreateCommand): ThingId {
        val id = command.id ?: repository.nextIdentity()
        if (command.classes.isNotEmpty() && (!classRepository.existsAll(command.classes) || command.classes.any { it in reservedClassIds })) {
            throw InvalidClassCollection(command.classes)
        }
        val resource = Resource(
            id = id,
            label = command.label,
            classes = command.classes,
            extractionMethod = command.extractionMethod,
            createdAt = OffsetDateTime.now(),
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor(),
            observatoryId = command.observatoryId ?: ObservatoryId.createUnknownObservatory(),
            organizationId = command.organizationId ?: OrganizationId.createUnknownOrganization()
        )
        repository.save(resource)
        return id
    }

    override fun create(label: String): Resource =
        create(CreateResourceUseCase.CreateCommand(label = label))
            .let { findById(it).get() }

    override fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource =
        create(
            CreateResourceUseCase.CreateCommand(
                label = label,
                contributorId = userId,
                observatoryId = observatoryId,
                extractionMethod = extractionMethod,
                organizationId = organizationId,
            )
        )
            .let { findById(it).get() }

    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource? =
        repository.findByIdAndClasses(id, classes)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
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
            observatoryId = observatoryId,
            organizationId = organizationId
        )

    override fun findById(id: ThingId): Optional<Resource> =
        repository.findById(id)

    override fun findAllByLabel(label: SearchString, pageable: Pageable): Page<Resource> =
        repository.findAllByLabel(label, pageable)

    override fun findAllByClass(pageable: Pageable, id: ThingId): Page<Resource> =
        repository.findAllByClass(id, pageable)

    override fun findAllByClassAndCreatedBy(
        pageable: Pageable,
        id: ThingId,
        createdBy: ContributorId
    ): Page<Resource> =
        repository.findAllByClassAndCreatedBy(id, createdBy, pageable)

    override fun findAllByClassAndLabel(id: ThingId, label: SearchString, pageable: Pageable): Page<Resource> =
        repository.findAllByClassAndLabel(id, label, pageable)

    override fun findAllByClassAndLabelAndCreatedBy(
        id: ThingId,
        label: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        repository.findAllByClassAndLabelAndCreatedBy(
            id,
            label,
            createdBy,
            pageable
        )

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource> {
        validateClassFilter(includeClasses, excludeClasses)
        return repository.findAllIncludingAndExcludingClasses(includeClasses, excludeClasses, pageable)
    }

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: SearchString,
        pageable: Pageable
    ): Page<Resource> {
        validateClassFilter(includeClasses, excludeClasses)
        return repository.findAllIncludingAndExcludingClassesByLabel(
            includeClasses,
            excludeClasses,
            label,
            pageable
        )
    }

    override fun findByDOI(doi: String): Optional<Resource> =
        statementRepository.findByDOI(doi)
            .filter(Resource::hasPublishableClasses)

    fun validateClassFilter(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>) {
        for (includedClass in includeClasses)
            if (includedClass in excludeClasses)
                throw InvalidClassFilter(includedClass)
    }

    override fun findByTitle(title: String): Optional<Resource> =
        repository.findPaperByLabel(title)

    override fun findAllByTitle(title: String?): Iterable<Resource> =
        repository.findAllPapersByLabel(title!!)

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> repository.findAllListed(pageable)
            VisibilityFilter.UNLISTED -> repository.findAllByVisibility(Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> repository.findAllByVisibility(Visibility.FEATURED, pageable)
            VisibilityFilter.NON_FEATURED -> repository.findAllByVisibility(Visibility.DEFAULT, pageable)
            VisibilityFilter.DELETED -> repository.findAllByVisibility(Visibility.DELETED, pageable)
        }

    override fun findAllPapersByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        repository.findAllByClassAndObservatoryId(paperClass, id, pageable)

    override fun findAllComparisonsByObservatoryId(
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        repository.findAllByClassAndObservatoryId(comparisonClass, id, pageable)

    override fun findAllProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        statementRepository.findProblemsByObservatoryId(id, pageable)

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

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        repository.findById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun update(command: UpdateResourceUseCase.UpdateCommand) {
        // already checked by service
        var found = repository.findById(command.id).get()

        // update all the properties
        if (command.label != null) found = found.copy(label = command.label!!)
        if (command.classes != null) {
            if (command.classes!!.isNotEmpty() && (!classRepository.existsAll(command.classes!!) || command.classes!!.any { it in reservedClassIds })) {
                throw InvalidClassCollection(command.classes!!)
            }
            found = found.copy(classes = command.classes!!)
        }
        if (command.observatoryId != null) found = found.copy(observatoryId = command.observatoryId!!)
        if (command.organizationId != null) found = found.copy(organizationId = command.organizationId!!)
        if (command.extractionMethod != null) found = found.copy(extractionMethod = command.extractionMethod!!)

        repository.save(found)
    }

    override fun delete(id: ThingId) {
        val resource = repository.findById(id).orElseThrow { ResourceNotFound.withId(id) }

        if (statementRepository.checkIfResourceHasStatements(resource.id))
            throw ResourceUsedInStatement(resource.id)

        repository.deleteById(resource.id)
    }

    override fun removeAll() = repository.deleteAll()

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
        else -> findAllByVisibility(visibility, pageable)
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

    override fun getFeaturedResourceFlag(id: ThingId): Boolean =
        repository.findById(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedResourceFlag(id: ThingId): Boolean =
        repository.findById(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        repository.findAllComparisonsByOrganizationId(id, pageable)

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        statementRepository.findAllProblemsByOrganizationId(id, pageable)

    override fun hasStatements(id: ThingId): Boolean = statementRepository.checkIfResourceHasStatements(id)

    private fun setVerifiedFlag(resourceId: ThingId, verified: Boolean) {
        val result = repository.findById(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound.withId(resourceId) }
        resultObj = resultObj.copy(verified = verified)
        repository.save(resultObj)
    }
}
