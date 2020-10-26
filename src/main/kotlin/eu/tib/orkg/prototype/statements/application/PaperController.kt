package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.application.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.application.ObjectController.Constants
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/papers/")
class PaperController(
    private val resourceService: ResourceService,
    private val literalService: LiteralService,
    private val predicateService: PredicateService,
    private val statementService: StatementService,
    private val contributorService: ContributorService,
    private val objectController: ObjectController
) : BaseController() {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<Resource> {
        val resource = addPaperContent(paper, mergeIfExists)
        val location = uriComponentsBuilder
            .path("api/resources/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }

    /**
     * Main entry point, to create paper and check contributions
     * Using the Object endpoint to handle recursive object creation
     */
    fun addPaperContent(
        request: CreatePaperRequest,
        mergeIfExists: Boolean
    ): Resource {
        val userId = authenticatedUserId()

        // check if should be merged or not
        val paperObj = createOrFindPaper(mergeIfExists, request, userId)
        val paperId = paperObj.id!!

        // paper contribution data
        if (request.paper.hasContributions()) {
            request.paper.contributions!!.forEach {
                val contributionId = addCompleteContribution(it, request)
                // Create statement between paper and contribution
                statementService.create(userId, paperId.value, Constants.ContributionPredicate, contributionId.value)
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
        paperRequest: CreatePaperRequest
    ): ResourceId {
        // Convert Paper structure to Object structure
        val contribution = jsonObject.copy(classes = listOf(Constants.ID_CONTRIBUTION_CLASS))
        val objectRequest = CreateObjectRequest(paperRequest.predicates, contribution)
        // Create contribution resource whether it has data or not
        return objectController.createObject(objectRequest).id!!
    }

    /**
     * Merges a paper if requested and found
     * o/w creates a new paper resource
     */
    private fun createOrFindPaper(
        mergeIfExists: Boolean,
        request: CreatePaperRequest,
        userId: UUID
    ): Resource {
        return if (mergeIfExists) {
            val byTitle = resourceService.findAllByTitle(request.paper.title)
            var found: Resource? = null
            if (byTitle.count() != 0) {
                found = if (request.paper.hasDOI()) {
                    // filter on both DOI an title
                    byTitle.intersect(resourceService.findAllByDOI(request.paper.doi!!)).firstOrNull()
                } else {
                    byTitle.firstOrNull()
                }
            }
            found ?: createNewPaperWithMetadata(userId, request)
        } else {
            createNewPaperWithMetadata(userId, request)
        }
    }

    /**
     * Handles the creation of a new paper resource
     * i.e., creates the new paper, meta-data
     */
    private fun createNewPaperWithMetadata(userId: UUID, request: CreatePaperRequest): Resource {
        val contributor = contributorService.findByIdOrElseUnknown(ContributorId(userId))
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
        val paperId = paperObj.id!!

        // paper doi
        if (request.paper.hasDOI()) {
            val paperDoi = literalService.create(userId, request.paper.doi!!).id!!
            statementService.create(userId, paperId.value, Constants.DoiPredicate, paperDoi.value)
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(userId, request.paper.url!!).id!!
            statementService.create(userId, paperId.value, Constants.UrlPredicate, paperUrl.value)
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.hasPublicationMonth())
            statementService.create(
                userId,
                paperId.value,
                Constants.PublicationMonthPredicate,
                literalService.create(userId, request.paper.publicationMonth.toString()).id!!.value
            )
        if (request.paper.hasPublicationYear())
            statementService.create(
                userId,
                paperId.value,
                Constants.PublicationYearPredicate,
                literalService.create(userId, request.paper.publicationYear.toString()).id!!.value
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
        statementService.create(
            userId,
            paperId.value,
            Constants.ResearchFieldPredicate,
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
        userId: UUID,
        observatoryId: UUID,
        extractionMethod: ExtractionMethod,
        organizationId: UUID
    ) {
        val venuePredicate = predicateService.findById(Constants.VenuePredicate).get().id!!
        val pageable = createPageable(1, 10, null, false)
        // Check if resource exists
        var venueResource = resourceService.findAllByLabel(pageable, venue).firstOrNull()
        if (venueResource == null) {
            // If not override object with new venue resource
            venueResource = resourceService.create(
                userId,
                CreateResourceRequest(
                    null,
                    venue,
                    setOf(Constants.VenueClass)
                ),
                observatoryId,
                extractionMethod,
                organizationId
            )
        }
        // create a statement with the venue resource
        statementService.create(
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
        userId: UUID,
        paperId: ResourceId,
        observatoryId: UUID,
        organizationId: UUID
    ) {
        val pattern = Constants.ORCID_REGEX.toRegex()
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
                                    foundOrcid.id!!.value,
                                    createPageable(1, 10, null, false) // TODO: Hide values by using default values for the parameters
                                ).firstOrNull { it.predicate.id == Constants.OrcidPredicate }
                                    ?: throw OrphanOrcidValue(orcidValue)
                            statementService.create(
                                userId,
                                paperId.value,
                                Constants.AuthorPredicate,
                                (authorStatement.subject as Resource).id!!.value
                            )
                        } else {
                            // create resource
                            val author = resourceService.create(
                                userId,
                                CreateResourceRequest(null, it.label!!, setOf(Constants.AuthorClass)),
                                observatoryId,
                                paper.paper.extractionMethod,
                                organizationId
                            )
                            statementService.create(
                                userId,
                                paperId.value,
                                Constants.AuthorPredicate,
                                author.id!!.value
                            )
                            // Create orcid literal
                            val orcid = literalService.create(userId, orcidValue)
                            // Add ORCID id to the new resource
                            statementService.create(userId, author.id.value, Constants.OrcidPredicate, orcid.id!!.value)
                        }
                    } else {
                        // create literal and link it
                        statementService.create(
                            userId,
                            paperId.value,
                            Constants.AuthorPredicate,
                            literalService.create(userId, it.label!!).id!!.value
                        )
                    }
                } else {
                    statementService.create(userId, paperId.value, Constants.AuthorPredicate, it.id!!)
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
    val extractionMethod: ExtractionMethod = UNKNOWN
) {
    /**
     * Check if the paper has a published in venue
     */
    fun hasPublishedIn(): Boolean =
        publishedIn?.isNotEmpty() != null

    /**
     * Check if the paper has a DOI
     */
    fun hasDOI(): Boolean =
        doi?.isNotEmpty() != null

    /**
     * Check if the paper has a URL
     */
    fun hasUrl(): Boolean =
        url?.isNotEmpty() != null

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
