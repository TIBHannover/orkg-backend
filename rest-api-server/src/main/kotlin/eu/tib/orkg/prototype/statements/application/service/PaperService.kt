package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.OrcidNotValid
import eu.tib.orkg.prototype.statements.application.OrphanOrcidValue
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class PaperService(
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementService: StatementUseCases,
    private val contributorService: ContributorService,
    private val objectService: ObjectService,
    private val resourceRepository: ResourceRepository, // TODO: remove dependency by pushing code to service
) {
    /**
     * Main entry point, to create paper and check contributions
     * Using the Object endpoint to handle recursive object creation
     */
    fun addPaperContent(
        request: CreatePaperRequest,
        mergeIfExists: Boolean,
        authenticatedUserId: UUID
    ): ResourceRepresentation {
        val userId = ContributorId(authenticatedUserId)

        // check if should be merged or not
        val paperObj = createOrFindPaper(mergeIfExists, request, userId)
        val paperId = paperObj.id

        // paper contribution data
        if (request.paper.hasContributions()) {
            request.paper.contributions!!.forEach {
                val contributionId = addCompleteContribution(it, request, authenticatedUserId)
                // Create statement between paper and contribution
                statementService.add(
                    userId,
                    paperId.value,
                    ObjectService.ContributionPredicate,
                    contributionId.value
                )
            }
        }
        return paperObj
    }

    /**
     * Using the object controller
     * inset the full graph of the contribution
     */
    private fun addCompleteContribution(
        jsonObject: NamedObject,
        paperRequest: CreatePaperRequest,
        userId: UUID,
    ): ResourceId {
        // Always append Contribution class to custom user classes
        val contributionClasses =
            (listOf(ObjectService.ID_CONTRIBUTION_CLASS) + jsonObject.classes.orEmpty()).toSet().toList()
        // Convert Paper structure to Object structure
        val contribution = jsonObject.copy(classes = contributionClasses)
        val objectRequest = CreateObjectRequest(paperRequest.predicates, contribution)
        // Create contribution resource whether it has data or not
        return objectService.createObject(objectRequest, userId).id
    }

    /**
     * Merges a paper if requested and found
     * o/w creates a new paper resource
     */
    private fun createOrFindPaper(
        mergeIfExists: Boolean,
        request: CreatePaperRequest,
        userId: ContributorId
    ): ResourceRepresentation {
        return if (mergeIfExists) {
            mergePapersIfPossible(userId, request)
        } else {
            createNewPaperWithMetadata(userId, request)
        }
    }

    /**
     * Handles the merging logic by looking up papers by title and DOI
     * otherwise it creates a new paper
     */
    private fun mergePapersIfPossible(
        userId: ContributorId,
        request: CreatePaperRequest
    ): ResourceRepresentation {
        // Do this in a sequential order, first check for DOI and then title, otherwise we create a new paper
        if (request.paper.hasDOI()) {
            val byDOI = resourceService.findAllByDOI(request.paper.doi!!)
            if (byDOI.count() > 0)
                return byDOI.first()
        }
        val byTitle = resourceService.findAllByTitle(request.paper.title)
        if (byTitle.count() > 0)
            return byTitle.first()
        return createNewPaperWithMetadata(userId, request)
    }

    /**
     * Handles the creation of a new paper resource
     * i.e., creates the new paper, meta-data
     */
    private fun createNewPaperWithMetadata(userId: ContributorId, request: CreatePaperRequest): ResourceRepresentation {
        val contributor = contributorService.findByIdOrElseUnknown(userId)
        val organizationId = contributor.organizationId
        val observatoryId = contributor.observatoryId

        // paper title
        val paperObj = resourceService.create(
            userId,
            CreateResourceRequest(null, request.paper.title, setOf(ClassId("Paper"))),
            observatoryId,
            request.paper.extractionMethod,
            organizationId
        )
        val paperId = paperObj.id

        // paper doi
        if (request.paper.hasDOI()) {
            val paperDoi = literalService.create(userId, request.paper.doi!!).id
            statementService.add(userId, paperId.value, ObjectService.DoiPredicate, paperDoi.value)
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(userId, request.paper.url!!).id
            statementService.add(userId, paperId.value, ObjectService.UrlPredicate, paperUrl.value)
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.hasPublicationMonth())
            statementService.add(
                userId,
                paperId.value,
                ObjectService.PublicationMonthPredicate,
                literalService.create(userId, request.paper.publicationMonth.toString()).id.value
            )
        if (request.paper.hasPublicationYear())
            statementService.add(
                userId,
                paperId.value,
                ObjectService.PublicationYearPredicate,
                literalService.create(userId, request.paper.publicationYear.toString()).id.value
            )

        // paper published At
        if (request.paper.hasPublishedIn())
            handlePublishingVenue(
                request.paper.publishedIn!!,
                paperId,
                userId,
                observatoryId,
                request.paper.extractionMethod,
                organizationId
            )

        // paper research field
        statementService.add(
            userId,
            paperId.value,
            ObjectService.ResearchFieldPredicate,
            ResourceId(request.paper.researchField).value
        )
        return paperObj
    }

    /**
     * Handles the venues of the papers
     * i.e., reusing venues across papers if existing
     */
    fun handlePublishingVenue(
        venue: String,
        paperId: ResourceId,
        userId: ContributorId,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ) {
        val venuePredicate = predicateService.findById(ObjectService.VenuePredicate).get().id
        val pageable = PageRequest.of(1, 10)
        // Check if resource exists
        var venueResource = resourceRepository.findAllByLabel(venue, pageable).firstOrNull()
        if (venueResource == null) {
            // If not override object with new venue resource
            val representation = resourceService.create(
                userId,
                CreateResourceRequest(
                    null,
                    venue,
                    setOf(ObjectService.VenueClass)
                ),
                observatoryId,
                extractionMethod,
                organizationId
            )
            venueResource = resourceRepository.findByResourceId(representation.id).get()
        }
        // create a statement with the venue resource
        statementService.add(
            userId,
            paperId.value,
            venuePredicate,
            venueResource.id!!.value
        )
    }

    /**
     * Handles the authors of a paper
     * Create orcid nodes if needed and creates author literal or
     * resource depending on that
     */
    fun handleAuthors(
        paper: CreatePaperRequest,
        userId: ContributorId,
        paperId: ResourceId,
        observatoryId: ObservatoryId,
        organizationId: OrganizationId
    ) {
        val pattern = ObjectService.ORCID_REGEX.toRegex()
        if (paper.paper.hasAuthors()) {
            paper.paper.authors!!.forEach { it ->
                if (!it.isExistingAuthor()) {
                    if (it.hasNameAndOrcid()) {
                        // Check if ORCID is a valid string
                        if (!pattern.matches(it.orcid!!))
                            throw OrcidNotValid(it.orcid)
                        // Remove te http://orcid.org prefix from the ORCID value
                        val indexClean = it.orcid.lastIndexOf('/')
                        val orcidValue = if (indexClean == -1) it.orcid else it.orcid.substring(indexClean + 1)
                        // Check if the orcid exists in the system or not
                        val foundOrcid = literalService.findAllByLabel(orcidValue).firstOrNull()
                        if (foundOrcid != null) {
                            // Link existing ORCID
                            val authorStatement =
                                statementService.findAllByObject(
                                    foundOrcid.id.value,
                                    PageRequest.of(
                                        1,
                                        10
                                    ) // TODO: Hide values by using default values for the parameters
                                ).firstOrNull { it.predicate.id == ObjectService.OrcidPredicate }
                                    ?: throw OrphanOrcidValue(orcidValue)
                            statementService.add(
                                userId,
                                paperId.value,
                                ObjectService.AuthorPredicate,
                                (authorStatement.subject as ResourceRepresentation).id.value
                            )
                        } else {
                            // create resource
                            val author = resourceService.create(
                                userId,
                                CreateResourceRequest(null, it.label!!, setOf(ObjectService.AuthorClass)),
                                observatoryId,
                                paper.paper.extractionMethod,
                                organizationId
                            )
                            statementService.add(
                                userId,
                                paperId.value,
                                ObjectService.AuthorPredicate,
                                author.id.value
                            )
                            // Create orcid literal
                            val orcid = literalService.create(userId, orcidValue)
                            // Add ORCID id to the new resource
                            statementService.add(
                                userId,
                                author.id.value,
                                ObjectService.OrcidPredicate,
                                orcid.id.value
                            )
                        }
                    } else {
                        // create literal and link it
                        statementService.add(
                            userId,
                            paperId.value,
                            ObjectService.AuthorPredicate,
                            literalService.create(userId, it.label!!).id.value
                        )
                    }
                } else {
                    statementService.add(userId, paperId.value, ObjectService.AuthorPredicate, it.id!!)
                }
            }
        }
    }
}

/**
 * Main entry point, basic skeleton of a paper
 */
data class CreatePaperRequest(
    val predicates: List<HashMap<String, String>>?,
    val paper: Paper
)

/**
 * Concrete paper holder class
 * contains meta-information of papers
 * and helper methods
 */
data class Paper(
    val title: String,
    val doi: String?,
    val authors: List<Author>?,
    val publicationMonth: Int?,
    val publicationYear: Int?,
    val publishedIn: String?,
    val url: String?,
    val researchField: String,
    val contributions: List<NamedObject>?,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
) {
    /**
     * Check if the paper has a published in venue
     */
    fun hasPublishedIn(): Boolean =
        publishedIn?.isNotEmpty() == true

    /**
     * Check if the paper has a DOI
     */
    fun hasDOI(): Boolean =
        doi?.isNotEmpty() == true

    /**
     * Check if the paper has a URL
     */
    fun hasUrl(): Boolean =
        url?.isNotEmpty() == true

    /**
     * Check if the paper has contributions
     */
    fun hasContributions() =
        !contributions.isNullOrEmpty()

    /**
     * Check if the paper has authors
     */
    fun hasAuthors() =
        !authors.isNullOrEmpty()

    /**
     * Check if the paper has a publication month value
     */
    fun hasPublicationMonth() =
        publicationMonth != null

    /**
     * Check if the paper has a publication year value
     */
    fun hasPublicationYear() =
        publicationYear != null
}

/**
 * Author class container
 */
data class Author(
    val id: String?,
    val label: String?,
    val orcid: String?
) {
    /**
     * Check if the author has a name (label)
     * and an ORCID at the same time
     */
    fun hasNameAndOrcid() =
        label != null && orcid != null

    /**
     * Check if the author is an existing resource
     * i.e., the id of the author is not null
     */
    fun isExistingAuthor() =
        !id.isNullOrEmpty()
}
