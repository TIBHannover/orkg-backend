package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.IterableResourcesGenerator
import eu.tib.orkg.prototype.statements.api.PagedResourcesGenerator
import eu.tib.orkg.prototype.statements.api.ResourceGenerator
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.UpdateResourceUseCase
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.application.InvalidClassCollection
import eu.tib.orkg.prototype.statements.application.InvalidClassFilter
import eu.tib.orkg.prototype.statements.application.ResourceCantBeDeleted
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

typealias FormattedLabels = Map<ThingId, FormattedLabel?>

private val paperClass = ThingId("Paper")
private val comparisonClass = ThingId("Comparison")

@Service
@Transactional
class ResourceService(
    private val comparisonRepository: ComparisonRepository,
    private val contributionRepository: ContributionRepository,
    private val visualizationRepository: VisualizationRepository,
    private val smartReviewRepository: SmartReviewRepository,
    private val repository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val templateRepository: TemplateRepository,
    private val flags: FeatureFlagService,
    private val classRepository: ClassRepository
) : ResourceUseCases {
    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun create(command: CreateResourceUseCase.CreateCommand): ThingId {
        val id = command.id ?: repository.nextIdentity()
        if (command.classes.isNotEmpty() && !classRepository.existsAll(command.classes)) {
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

    override fun create(label: String): ResourceRepresentation =
        create(CreateResourceUseCase.CreateCommand(label = label))
            .let { findById(it).get() }

    override fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): ResourceRepresentation =
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

    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): ResourceRepresentation? =
        retrieveAndConvertNullable { repository.findByIdAndClasses(id, classes) }

    override fun map(action: IterableResourcesGenerator): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable(action::generate)

    override fun map(action: PagedResourcesGenerator): Page<ResourceRepresentation> =
        retrieveAndConvertPaged(action::generate)

    override fun map(action: ResourceGenerator): ResourceRepresentation = retrieveAndConvertNullable(action::generate)!!

    override fun findAll(pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAll(pageable) }

    override fun findById(id: ThingId): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { repository.findByResourceId(id) }

    override fun findAllByLabel(pageable: Pageable, label: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
        }

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) }

    override fun findAllByClass(pageable: Pageable, id: ThingId): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClass(id, pageable) }

    override fun findAllByClassAndCreatedBy(
        pageable: Pageable,
        id: ThingId,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClassAndCreatedBy(id, createdBy, pageable) }

    override fun findAllByClassAndLabel(pageable: Pageable, id: ThingId, label: String): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findAllByClassAndLabel(id, label, pageable) }

    override fun findAllByClassAndLabelAndCreatedBy(
        pageable: Pageable,
        id: ThingId,
        label: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelAndCreatedBy(
                id,
                label,
                createdBy,
                pageable
            )
        }

    override fun findAllByClassAndLabelContaining(
        pageable: Pageable,
        id: ThingId,
        part: String
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelMatchesRegex(
                id,
                part.toSearchString(),
                pageable
            )
        }

    override fun findAllByClassAndLabelContainingAndCreatedBy(
        pageable: Pageable,
        id: ThingId,
        part: String,
        createdBy: ContributorId
    ): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            repository.findAllByClassAndLabelMatchesRegexAndCreatedBy(
                id,
                part.toSearchString(),
                createdBy,
                pageable
            )
        }

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        validateClassFilter(includeClasses, excludeClasses)
        return retrieveAndConvertPaged {
            repository.findAllIncludingAndExcludingClasses(includeClasses, excludeClasses, pageable)
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: String,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        validateClassFilter(includeClasses, excludeClasses)
        return retrieveAndConvertPaged {
            repository.findAllIncludingAndExcludingClassesByLabelMatchesRegex(
                includeClasses,
                excludeClasses,
                label.toExactSearchString(),
                pageable
            )
        }
    }

    override fun findAllIncludingAndExcludingClassesByLabelContaining(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        part: String,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        validateClassFilter(includeClasses, excludeClasses)
        return retrieveAndConvertPaged {
            repository.findAllIncludingAndExcludingClassesByLabelMatchesRegex(
                includeClasses,
                excludeClasses,
                part.toSearchString(),
                pageable
            )
        }
    }

    fun validateClassFilter(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>) {
        for (includedClass in includeClasses)
            if (includedClass in excludeClasses)
                throw InvalidClassFilter(includedClass)
    }

    override fun findByDOI(doi: String): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { statementRepository.findByDOI(doi) }

    override fun findAllPapersByDOI(doi: String, pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllPapersByDOI(doi, pageable) }

    override fun findByTitle(title: String): Optional<ResourceRepresentation> =
        retrieveAndConvertOptional { repository.findByLabel(title) }

    override fun findAllByTitle(title: String?): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findAllByLabel(title!!) }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged {
            when (visibility) {
                VisibilityFilter.ALL_LISTED -> repository.findAllListed(pageable)
                VisibilityFilter.UNLISTED -> repository.findAllByVisibility(Visibility.UNLISTED, pageable)
                VisibilityFilter.FEATURED -> repository.findAllByVisibility(Visibility.FEATURED, pageable)
                VisibilityFilter.NON_FEATURED -> repository.findAllByVisibility(Visibility.DEFAULT, pageable)
                VisibilityFilter.DELETED -> repository.findAllByVisibility(Visibility.DELETED, pageable)
            }
        }

    override fun findPapersByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findByClassAndObservatoryId(paperClass, id) }

    override fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation> =
        retrieveAndConvertIterable { repository.findByClassAndObservatoryId(comparisonClass, id) }

    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { statementRepository.findProblemsByObservatoryId(id, pageable) }

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<ResourceRepresentation> = retrieveAndConvertPaged {
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> repository.findAllListedByClassInAndObservatoryId(classes, id, pageable)
            VisibilityFilter.UNLISTED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.UNLISTED, id, pageable)
            VisibilityFilter.FEATURED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.FEATURED, id, pageable)
            VisibilityFilter.NON_FEATURED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.DEFAULT, id, pageable)
            VisibilityFilter.DELETED -> repository.findAllByClassInAndVisibilityAndObservatoryId(classes, Visibility.DELETED, id, pageable)
        }
    }

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        repository.findByResourceId(id)
            .map { statementRepository.findTimelineByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        repository.findByResourceId(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun update(command: UpdateResourceUseCase.UpdateCommand) {
        // already checked by service
        var found = repository.findByResourceId(command.id).get()

        // update all the properties
        if (command.label != null) found = found.copy(label = command.label)
        if (command.classes != null) {
            if (command.classes.isNotEmpty() && !classRepository.existsAll(command.classes)) {
                throw InvalidClassCollection(command.classes)
            }
            found = found.copy(classes = command.classes)
        }
        if (command.observatoryId != null) found = found.copy(observatoryId = command.observatoryId)
        if (command.organizationId != null) found = found.copy(organizationId = command.organizationId)

        repository.save(found)
    }

    override fun delete(id: ThingId) {
        val resource = repository.findByResourceId(id).orElseThrow { ResourceNotFound.withId(id) }

        if (statementRepository.checkIfResourceHasStatements(resource.id))
            throw ResourceCantBeDeleted(resource.id)

        repository.deleteByResourceId(resource.id)
    }

    override fun removeAll() = repository.deleteAll()

    override fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<ResourceRepresentation> = when {
        classes.isNotEmpty() -> retrieveAndConvertPaged {
            when (visibility) {
                VisibilityFilter.ALL_LISTED -> repository.findAllListedByClassIn(classes, pageable)
                VisibilityFilter.UNLISTED -> repository.findAllByClassInAndVisibility(classes, Visibility.UNLISTED, pageable)
                VisibilityFilter.FEATURED -> repository.findAllByClassInAndVisibility(classes, Visibility.FEATURED, pageable)
                VisibilityFilter.NON_FEATURED -> repository.findAllByClassInAndVisibility(classes, Visibility.DEFAULT, pageable)
                VisibilityFilter.DELETED -> repository.findAllByClassInAndVisibility(classes, Visibility.DELETED, pageable)
            }
        }
        else -> findAllByVisibility(visibility, pageable)
    }

    override fun findAllContributorIds(pageable: Pageable) = repository.findAllContributorIds(pageable)

    override fun markAsVerified(resourceId: ThingId) = setVerifiedFlag(resourceId, true)

    override fun markAsUnverified(resourceId: ThingId) = setVerifiedFlag(resourceId, false)

    override fun loadVerifiedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVerified(true, pageable)

    override fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVerified(false, pageable)

    override fun getPaperVerifiedFlag(id: ThingId): Boolean? =
        repository.findPaperByResourceId(id)
            .map { it.verified }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun markAsFeatured(resourceId: ThingId) {
        val resource = repository.findByResourceId(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.FEATURED
        )
        repository.save(modified)
    }

    override fun markAsNonFeatured(resourceId: ThingId) {
        val resource = repository.findByResourceId(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.DEFAULT
        )
        repository.save(modified)
    }

    override fun markAsUnlisted(resourceId: ThingId) {
        val resource = repository.findByResourceId(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.UNLISTED
        )
        repository.save(modified)
    }

    override fun markAsListed(resourceId: ThingId) {
        val resource = repository.findByResourceId(resourceId)
            .orElseThrow { ResourceNotFound.withId(resourceId) }
        val modified = resource.copy(
            visibility = Visibility.DEFAULT
        )
        repository.save(modified)
    }

    override fun loadFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllPapersByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedPapers(pageable: Pageable): Page<Resource> =
        repository.findAllListedPapers(pageable)

    override fun getFeaturedPaperFlag(id: ThingId): Boolean =
        repository.findPaperByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedPaperFlag(id: ThingId): Boolean =
        repository.findPaperByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedResourceFlag(id: ThingId): Boolean =
        repository.findByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedResourceFlag(id: ThingId): Boolean =
        repository.findByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun loadFeaturedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllFeaturedComparisons(pageable)

    override fun loadNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllNonFeaturedComparisons(pageable)

    override fun loadUnlistedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllUnlistedComparisons(pageable)

    override fun loadListedComparisons(pageable: Pageable): Page<Resource> =
        comparisonRepository.findAllListedComparisons(pageable)

    override fun loadFeaturedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllFeaturedContributions(pageable)

    override fun loadNonFeaturedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllNonFeaturedContributions(pageable)

    override fun loadUnlistedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllUnlistedContributions(pageable)

    override fun loadListedContributions(pageable: Pageable): Page<Resource> =
        contributionRepository.findAllListedContributions(pageable)

    override fun loadFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllFeaturedVisualizations(pageable)

    override fun loadNonFeaturedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllNonFeaturedVisualizations(pageable)

    override fun loadUnlistedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllUnlistedVisualizations(pageable)

    override fun loadListedVisualizations(pageable: Pageable): Page<Resource> =
        visualizationRepository.findAllListedVisualizations(pageable)

    override fun loadFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllFeaturedSmartReviews(pageable)

    override fun loadNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllNonFeaturedSmartReviews(pageable)

    override fun loadUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllUnlistedSmartReviews(pageable)

    override fun loadListedSmartReviews(pageable: Pageable): Page<Resource> =
        smartReviewRepository.findAllListedSmartReviews(pageable)

    override fun getFeaturedContributionFlag(id: ThingId): Boolean =
        contributionRepository.findContributionByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedContributionFlag(id: ThingId): Boolean =
        contributionRepository.findContributionByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedComparisonFlag(id: ThingId): Boolean =
        comparisonRepository.findComparisonByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedComparisonFlag(id: ThingId): Boolean =
        comparisonRepository.findComparisonByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedVisualizationFlag(id: ThingId): Boolean =
        visualizationRepository.findVisualizationByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedVisualizationFlag(id: ThingId): Boolean =
        visualizationRepository.findVisualizationByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getFeaturedSmartReviewFlag(id: ThingId): Boolean =
        smartReviewRepository.findSmartReviewByResourceId(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedSmartReviewFlag(id: ThingId): Boolean =
        smartReviewRepository.findSmartReviewByResourceId(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { repository.findComparisonsByOrganizationId(id, pageable) }

    override fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        retrieveAndConvertPaged { statementRepository.findProblemsByOrganizationId(id, pageable) }

    override fun hasStatements(id: ThingId): Boolean = statementRepository.checkIfResourceHasStatements(id)

    private fun setVerifiedFlag(resourceId: ThingId, verified: Boolean) {
        val result = repository.findByResourceId(resourceId)
        var resultObj = result.orElseThrow { ResourceNotFound.withId(resourceId) }
        resultObj = resultObj.copy(verified = verified)
        repository.save(resultObj)
    }

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"

    private fun countsFor(resources: List<Resource>): Map<ThingId, Long> {
        val resourceIds = resources.map { it.id }.toSet()
        return statementRepository.countStatementsAboutResources(resourceIds)
    }

    private fun formatLabelFor(resources: List<Resource>): Map<ThingId, FormattedLabel?> =
        if (flags.isFormattedLabelsEnabled())
            resources.associate { it.id to templateRepository.formattedLabelFor(it.id, it.classes) }
        else emptyMap()

    private fun retrieveAndConvertPaged(action: () -> Page<Resource>): Page<ResourceRepresentation> {
        val paged = action()
        val statementCounts = countsFor(paged.content)
        val formattedLabelCount = formatLabelFor(paged.content)
        return paged.map { it.toResourceRepresentation(statementCounts, formattedLabelCount) }
    }

    private fun retrieveAndConvertIterable(action: () -> Iterable<Resource>): Iterable<ResourceRepresentation> {
        val resources = action()
        val statementCounts = countsFor(resources.toList())
        val formattedLabelCounts = formatLabelFor(resources.toList())
        return resources.map { it.toResourceRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun retrieveAndConvertOptional(action: () -> Optional<Resource>): Optional<ResourceRepresentation> =
        action().map {
            val count = statementRepository.countStatementsAboutResource(it.id)
            it.toResourceRepresentation(mapOf(it.id to count), formatLabelFor(listOf(it)))
        }

    private fun retrieveAndConvertNullable(action: () -> Resource?): ResourceRepresentation? =
        action()?.let {
            val count = statementRepository.countStatementsAboutResource(it.id)
            it.toResourceRepresentation(mapOf(it.id to count), formatLabelFor(listOf(it)))
        }
}

fun Resource.toResourceRepresentation(usageCounts: StatementCounts, formattedLabels: FormattedLabels): ResourceRepresentation =
    object : ResourceRepresentation {
        override val id: ThingId = this@toResourceRepresentation.id
        override val label: String = this@toResourceRepresentation.label
        override val classes: Set<ThingId> = this@toResourceRepresentation.classes
        override val shared: Long = usageCounts[this@toResourceRepresentation.id] ?: 0
        override val extractionMethod: ExtractionMethod = this@toResourceRepresentation.extractionMethod
        override val jsonClass: String = "resource"
        override val createdAt: OffsetDateTime = this@toResourceRepresentation.createdAt
        override val createdBy: ContributorId = this@toResourceRepresentation.createdBy
        override val observatoryId: ObservatoryId = this@toResourceRepresentation.observatoryId
        override val organizationId: OrganizationId = this@toResourceRepresentation.organizationId
        override val featured: Boolean = this@toResourceRepresentation.visibility == Visibility.FEATURED
        override val unlisted: Boolean = this@toResourceRepresentation.visibility == Visibility.UNLISTED
        override val verified: Boolean = this@toResourceRepresentation.verified ?: false
        override val formattedLabel: FormattedLabel? = formattedLabels[this@toResourceRepresentation.id]
    }
