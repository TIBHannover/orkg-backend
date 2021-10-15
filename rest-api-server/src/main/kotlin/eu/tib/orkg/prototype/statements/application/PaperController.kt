package eu.tib.orkg.prototype.statements.application

import java.util.logging.Logger
import eu.tib.orkg.prototype.constants.AuthorClass
import eu.tib.orkg.prototype.constants.AuthorPredicate
import eu.tib.orkg.prototype.constants.ContributionPredicate
import eu.tib.orkg.prototype.constants.DoiPredicate
import eu.tib.orkg.prototype.constants.ID_CONTRIBUTION_CLASS
import eu.tib.orkg.prototype.constants.ORCID_REGEX
import eu.tib.orkg.prototype.constants.OrcidPredicate
import eu.tib.orkg.prototype.constants.PublicationMonthPredicate
import eu.tib.orkg.prototype.constants.PublicationYearPredicate
import eu.tib.orkg.prototype.constants.ResearchFieldPredicate
import eu.tib.orkg.prototype.constants.UrlPredicate
import eu.tib.orkg.prototype.constants.VenueClass
import eu.tib.orkg.prototype.constants.VenuePredicate
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/papers/")
@CrossOrigin(origins = arrayOf("http://localhost:8080"))
class PaperController(
    private val resourceService: ResourceService,
    private val literalService: LiteralService,
    private val predicateService: PredicateService,
    private val statementService: StatementService,
    private val contributorService: ContributorService,
    private val objectController: ObjectController
) : BaseController() {

    private val logger = Logger.getLogger("Paper")

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<Resource> {
        logger.info("P-1")
        val resource = addPaperContent(paper, mergeIfExists)
        val location = uriComponentsBuilder
            .path("api/resources/")
            .buildAndExpand(resource.id)
            .toUri()
        logger.info("P-10")
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
        logger.info("P-2")
        //val userId = ContributorId(authenticatedUserId())
        val userId = ContributorId(keycloakAuthenticatedUserId())
        logger.info("P-3")
        // check if should be merged or not
        val paperObj = createOrFindPaper(mergeIfExists, request, userId)
        logger.info("P-4")
        val paperId = paperObj.id!!
        logger.info("P-5")

        // paper contribution data
        if (request.paper.hasContributions()) {
            request.paper.contributions!!.forEach {
                val contributionId = addCompleteContribution(it, request)
                // Create statement between paper and contribution
                logger.info("P-6")
                statementService.add(userId, paperId.value, ContributionPredicate, contributionId.value)
            }
        }
        logger.info("P-9")
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
        logger.info("P-8")
        // Convert Paper structure to Object structure
        val contribution = jsonObject.copy(classes = listOf(ID_CONTRIBUTION_CLASS))
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
        userId: ContributorId
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
    private fun createNewPaperWithMetadata(userId: ContributorId, request: CreatePaperRequest): Resource {
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
        val paperId = paperObj.id!!

        // paper doi
        if (request.paper.hasDOI()) {
            val paperDoi = literalService.create(userId, request.paper.doi!!).id!!
            statementService.add(userId, paperId.value, DoiPredicate, paperDoi.value)
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(userId, request.paper.url!!).id!!
            statementService.add(userId, paperId.value, UrlPredicate, paperUrl.value)
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.hasPublicationMonth())
            statementService.add(
                userId,
                paperId.value,
                PublicationMonthPredicate,
                literalService.create(userId, request.paper.publicationMonth.toString()).id!!.value
            )
        if (request.paper.hasPublicationYear())
            statementService.add(
                userId,
                paperId.value,
                PublicationYearPredicate,
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
        statementService.add(
            userId,
            paperId.value,
            ResearchFieldPredicate,
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
        val venuePredicate = predicateService.findById(VenuePredicate).get().id!!
        val pageable = PageRequest.of(1, 10)
        // Check if resource exists
        var venueResource = resourceService.findAllByLabelExactly(pageable, venue).firstOrNull()
        if (venueResource == null) {
            // If not override object with new venue resource
            venueResource = resourceService.create(
                userId,
                CreateResourceRequest(
                    null,
                    venue,
                    setOf(VenueClass)
                ),
                observatoryId,
                extractionMethod,
                organizationId
            )
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
        val pattern = ORCID_REGEX.toRegex()
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
                                    PageRequest.of(1, 10) // TODO: Hide values by using default values for the parameters
                                ).firstOrNull { it.predicate.id == OrcidPredicate }
                                    ?: throw OrphanOrcidValue(orcidValue)
                            statementService.add(
                                userId,
                                paperId.value,
                                AuthorPredicate,
                                (authorStatement.subject as Resource).id!!.value
                            )
                        } else {
                            // create resource
                            val author = resourceService.create(
                                userId,
                                CreateResourceRequest(null, it.label!!, setOf(AuthorClass)),
                                observatoryId,
                                paper.paper.extractionMethod,
                                organizationId
                            )
                            statementService.add(
                                userId,
                                paperId.value,
                                AuthorPredicate,
                                author.id!!.value
                            )
                            // Create orcid literal
                            val orcid = literalService.create(userId, orcidValue)
                            // Add ORCID id to the new resource
                            statementService.add(userId, author.id!!.value, OrcidPredicate, orcid.id!!.value)
                        }
                    } else {
                        // create literal and link it
                        statementService.add(
                            userId,
                            paperId.value,
                            AuthorPredicate,
                            literalService.create(userId, it.label!!).id!!.value
                        )
                    }
                } else {
                    statementService.add(userId, paperId.value, AuthorPredicate, it.id!!)
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
    val doi: String? = "",
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
    val label: String? = "",
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
