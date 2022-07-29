package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResourceService(
    private val repository: ResourceRepository,
    private val statementRepository: StatementRepository,
) : ResourceUseCases {

    override fun create(label: String): ResourceRepresentation = create(
        ContributorId.createUnknownContributor(),
        label,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): ResourceRepresentation {
        var resourceId = repository.nextIdentity()

        // Should be moved to the Generator in the future
        while (repository.findByResourceId(resourceId).isPresent) {
            resourceId = repository.nextIdentity()
        }

        val newResource = Resource(
            label = label,
            id = resourceId,
            createdBy = userId,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            createdAt = OffsetDateTime.now(),
        )
        repository.save(newResource)
        return findById(newResource.id).get()
    }

    override fun create(request: CreateResourceRequest): ResourceRepresentation = create(
        ContributorId.createUnknownContributor(),
        request,
        ObservatoryId.createUnknownObservatory(),
        UNKNOWN,
        OrganizationId.createUnknownOrganization()
    )

    override fun create(
        userId: ContributorId,
        request: CreateResourceRequest,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): ResourceRepresentation {
        val id = request.id ?: repository.nextIdentity()
        val resource = Resource(
            label = request.label,
            id = id,
            createdBy = userId,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            createdAt = OffsetDateTime.now(),
            classes = request.classes
        )
        repository.save(resource)
        return findById(resource.id).get()
    }

    override fun findAll(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAll(pageable) }

    override fun findById(id: ResourceId?): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { repository.findByResourceId(id) }

    override fun findAllByLabel(pageable: Pageable, label: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
        }

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) }

    override fun findAllByClass(pageable: Pageable, id: ClassId): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClass(id.toString(), pageable) }

    override fun findAllByClassAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClassAndCreatedBy(id.toString(), createdBy, pageable) }

    override fun findAllByClassAndLabel(pageable: Pageable, id: ClassId, label: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClassAndLabel(id.toString(), label, pageable) }

    override fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        label: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelAndCreatedBy(
                id.toString(),
                label,
                createdBy,
                pageable
            )
        }

    override fun findAllByClassAndLabelContaining(
        pageable: Pageable,
        id: ClassId,
        part: String
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelContaining(
                id.toString(),
                part.toSearchString(),
                pageable
            )
        }

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ClassId,
        part: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelContainingAndCreatedBy(
                id.toString(),
                part.toSearchString(),
                createdBy,
                pageable
            )
        }

    override fun findAllExcludingClass(pageable: Pageable, ids: Array<ClassId>): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllExcludingClass(ids.map { it.value }, pageable) }

    override fun findAllExcludingClassByLabel(
        pageable: Pageable,
        ids: Array<ClassId>,
        label: String
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllExcludingClassByLabel(ids.map { it.value }, label, pageable) }

    override fun findAllExcludingClassByLabelContaining(
        pageable: Pageable,
        ids: Array<ClassId>,
        part: String
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllExcludingClassByLabelContaining(ids.map { it.value }, part.toSearchString(), pageable)
        }

    override fun findByDOI(doi: String): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { repository.findByDOI(doi) }

    override fun findByTitle(title: String?): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { repository.findByLabel(title) }

    override fun findAllByDOI(doi: String): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findAllByDOI(doi) }

    override fun findAllByTitle(title: String?): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findAllByLabel(title!!) }

    override fun findAllByFeatured(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByFeaturedIsTrue(pageable) }

    override fun findAllByNonFeatured(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByFeaturedIsFalse(pageable) }

    override fun findAllByUnlisted(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByUnlistedIsTrue(pageable) }

    override fun findAllByListed(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByUnlistedIsFalse(pageable) }

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findPapersByObservatoryId(id) }

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findComparisonsByObservatoryId(id) }

    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findProblemsByObservatoryId(id) }

    override fun findResourcesByObservatoryIdAndClass(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            if (featured != null) {
                repository.findAllFeaturedResourcesByObservatoryIDAndClass(
                    id,
                    classes,
                    featured,
                    unlisted,
                    pageable
                )
            }
            repository.findAllResourcesByObservatoryIDAndClass(id, classes, unlisted, pageable)
        }

    override fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors> =
        repository.findContributorsByResourceId(id)

    override fun update(request: UpdateResourceRequest): ResourceRepresentation {
        // already checked by service
        var found = repository.findByResourceId(request.id).get()

        // update all the properties
        if (request.label != null) found = found.copy(label = request.label)
        if (request.classes != null) found = found.copy(classes = request.classes)
        repository.save(found)

        return findById(found.id).get()
    }

    override fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): ResourceRepresentation {
        var found = repository.findByResourceId(id).get()
        found = found.copy(observatoryId = request.observatoryId)
        found = found.copy(organizationId = request.organizationId)
        repository.save(found)

        return findById(found.id).get()
    }

    override fun hasStatements(id: ResourceId) = repository.checkIfResourceHasStatements(id)

    override fun delete(id: ResourceId) {
        val found = repository.findByResourceId(id).get()
        repository.delete(found.id!!)
    }

    override fun removeAll() = repository.deleteAll()

    override fun getResourcesByClasses(
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            if (classes.isNotEmpty()) {
                when (featured) {
                    null -> repository.findAllFeaturedResourcesByClass(
                        classes, unlisted, pageable
                    )
                    else -> repository.findAllFeaturedResourcesByClass(
                        classes, featured, unlisted, pageable
                    )
                }
            } else {
                Page.empty()
            }
        }

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"

    private fun countsFor(resources: List<Resource>): Map<ResourceId, Long> {
        val resourceIds = resources.mapNotNull { it.id }.toSet()
        return statementRepository.countStatementsAboutResources(resourceIds)
    }

    private fun retrieveAndConvertPaged(action: () -> Page<Resource>): Page<ResourceRepresentation> {
        val paged = action()
        return paged.map { it.toResourceRepresentation(countsFor(paged.content)) }
    }

    private fun retrieveAndConvertIterable(action: () -> Iterable<Resource>): Iterable<ResourceRepresentation> {
        val resources = action()
        return resources.map { it.toResourceRepresentation(countsFor(resources.toList())) }
    }

    private fun retrieveAndConvertOptional(action: () -> Optional<Resource>): Optional<ResourceRepresentation> =
        action().map {
            val count = statementRepository.countStatementsAboutResource(it.id!!)
            it.toResourceRepresentation(mapOf(it.id to count))
        }
}

fun Resource.toResourceRepresentation(usageCounts: StatementCounts): ResourceRepresentation =
    object : ResourceRepresentation {
        override val id: ResourceId = this@toResourceRepresentation.id!!
        override val label: String = this@toResourceRepresentation.label
        override val classes: Set<ClassId> = this@toResourceRepresentation.classes
        override val shared: Long = usageCounts[this@toResourceRepresentation.id] ?: 0
        override val extractionMethod: ExtractionMethod = this@toResourceRepresentation.extractionMethod
        override val jsonClass: String = "resource"
        override val createdAt: OffsetDateTime = this@toResourceRepresentation.createdAt
        override val createdBy: ContributorId = this@toResourceRepresentation.createdBy
        override val observatoryId: ObservatoryId = this@toResourceRepresentation.observatoryId
        override val organizationId: OrganizationId = this@toResourceRepresentation.organizationId
        override val featured: Boolean = this@toResourceRepresentation.featured ?: false
        override val unlisted: Boolean = this@toResourceRepresentation.unlisted ?: false
        override val verified: Boolean = this@toResourceRepresentation.verified ?: false
    }
