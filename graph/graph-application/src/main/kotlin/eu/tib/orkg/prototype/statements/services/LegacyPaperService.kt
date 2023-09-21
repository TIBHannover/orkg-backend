package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.CreateObjectUseCase.CreateObjectRequest
import eu.tib.orkg.prototype.statements.api.CreateObjectUseCase.NamedObject
import eu.tib.orkg.prototype.statements.api.CreatePaperUseCase
import eu.tib.orkg.prototype.statements.api.CreatePaperUseCase.CreatePaperRequest
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.LegacyRetrievePaperUseCase
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.OrcidNotValid
import eu.tib.orkg.prototype.statements.application.OrphanOrcidValue
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.PaperResourceWithPath
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PaperRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class LegacyPaperService(
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementService: StatementUseCases,
    private val contributorService: ContributorService,
    private val objectService: ObjectService,
    private val resourceRepository: ResourceRepository,
    private val repository: PaperRepository,
    private val listService: ListService,
) : LegacyRetrievePaperUseCase, CreatePaperUseCase {
    /**
     * Main entry point, to create paper and check contributions
     * Using the Object endpoint to handle recursive object creation
     */
    override fun addPaperContent(
        request: CreatePaperRequest,
        mergeIfExists: Boolean,
        userUUID: UUID,
    ): ThingId {
        val userId = ContributorId(userUUID)

        // check if should be merged or not
        val paperId = createOrFindPaper(mergeIfExists, request, userId)

        // paper contribution data
        if (request.paper.hasContributions()) {
            request.paper.contributions!!.forEach {
                val contributionId = addCompleteContribution(it, request, userUUID)
                // Create statement between paper and contribution
                statementService.add(
                    userId, paperId, ObjectService.ContributionPredicate, contributionId
                )
            }
        }
        return paperId
    }

    /**
     * Using the object controller
     * inset the full graph of the contribution
     */
    private fun addCompleteContribution(
        jsonObject: NamedObject,
        paperRequest: CreatePaperRequest,
        userUUID: UUID,
    ): ThingId {
        // Always append Contribution class to custom user classes
        val contributionClasses =
            (listOf(ObjectService.ID_CONTRIBUTION_CLASS) + jsonObject.classes.orEmpty()).toSet().toList()
        // Convert Paper structure to Object structure
        val contribution = jsonObject.copy(classes = contributionClasses)
        val objectRequest = CreateObjectRequest(paperRequest.predicates, contribution)
        // Create contribution resource whether it has data or not
        return objectService.createObject(objectRequest, null, userUUID)
    }

    /**
     * Merges a paper if requested and found
     * o/w creates a new paper resource
     */
    private fun createOrFindPaper(
        mergeIfExists: Boolean,
        request: CreatePaperRequest,
        userId: ContributorId
    ): ThingId {
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
    ): ThingId {
        // Do this in a sequential order, first check for DOI and then title, otherwise we create a new paper
        if (request.paper.hasDOI()) {
            val byDOI = resourceService.findByDOI(request.paper.doi!!)
                .filter { Classes.paper in it.classes }
            if (byDOI.isPresent) return byDOI.get().id
        }
        val byTitle = resourceService.findAllByTitle(request.paper.title)
        if (byTitle.count() > 0) return byTitle.first().id
        return createNewPaperWithMetadata(userId, request)
    }

    /**
     * Handles the creation of a new paper resource
     * i.e., creates the new paper, meta-data
     */
    private fun createNewPaperWithMetadata(userId: ContributorId, request: CreatePaperRequest): ThingId {
        val contributor = contributorService.findById(userId).orElse(Contributor.UNKNOWN)
        val organizationId = contributor.organizationId
        val observatoryId = contributor.observatoryId

        // paper title
        val paperId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                label = request.paper.title,
                classes = setOf(ThingId("Paper")),
                extractionMethod = request.paper.extractionMethod,
                contributorId = userId,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        )

        // paper doi
        if (request.paper.hasDOI()) {
            val paperDoi = literalService.create(userId, request.paper.doi!!).id
            statementService.add(userId, paperId, ObjectService.DoiPredicate, paperDoi)
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(userId, request.paper.url!!).id
            statementService.add(userId, paperId, ObjectService.UrlPredicate, paperUrl)
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.hasPublicationMonth()) statementService.add(
            userId,
            paperId,
            ObjectService.PublicationMonthPredicate,
            literalService.create(userId, request.paper.publicationMonth.toString(), datatype = "xsd:integer").id
        )
        if (request.paper.hasPublicationYear()) statementService.add(
            userId,
            paperId,
            ObjectService.PublicationYearPredicate,
            literalService.create(userId, request.paper.publicationYear.toString(), datatype = "xsd:integer").id
        )

        // paper published At
        if (request.paper.hasPublishedIn()) handlePublishingVenue(
            request.paper.publishedIn!!, paperId, userId, observatoryId, request.paper.extractionMethod, organizationId
        )

        // paper research field
        statementService.add(
            userId,
            paperId,
            ObjectService.ResearchFieldPredicate,
            request.paper.researchField
        )
        return paperId
    }

    /**
     * Handles the venues of the papers
     * i.e., reusing venues across papers if existing
     */
    fun handlePublishingVenue(
        venue: String,
        paperId: ThingId,
        userId: ContributorId,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ) {
        val venuePredicate = predicateService.findById(ObjectService.VenuePredicate).get().id
        // Check if resource exists
        val venueResource = resourceRepository.findAllByClassAndLabel(
            `class` = ObjectService.VenueClass,
            labelSearchString = SearchString.of(venue, exactMatch = true),
            pageable = PageRequest.of(0, 1)
        ).singleOrNull() ?: run {
            val resourceId = resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = venue,
                    classes = setOf(ObjectService.VenueClass),
                    extractionMethod = extractionMethod,
                    contributorId = userId,
                    observatoryId = observatoryId,
                    organizationId = organizationId
                )
            )
            resourceRepository.findById(resourceId).get()
        }
        // create a statement with the venue resource
        statementService.add(
            userId, paperId, venuePredicate, venueResource.id
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
        paperId: ThingId,
        observatoryId: ObservatoryId,
        organizationId: OrganizationId
    ) {
        val pattern = ObjectService.ORCID_REGEX.toRegex()
        if (paper.paper.hasAuthors()) {
            val authors = paper.paper.authors!!.map { it ->
                if (!it.hasId()) {
                    if (it.hasNameAndOrcid()) {
                        // Check if ORCID is a valid string
                        if (!pattern.matches(it.orcid!!)) throw OrcidNotValid(it.orcid)
                        // Remove te http://orcid.org prefix from the ORCID value
                        val indexClean = it.orcid.lastIndexOf('/')
                        val orcidValue = if (indexClean == -1) it.orcid else it.orcid.substring(indexClean + 1)
                        // Check if the orcid exists in the system or not
                        val foundOrcid = literalService.findAllByLabel(
                            SearchString.of(orcidValue, exactMatch = true),
                            PageRequest.of(0, 1)
                        ).firstOrNull()
                        if (foundOrcid != null) {
                            // Link existing ORCID
                            val authorStatement = statementService.findAllByObject(
                                foundOrcid.id, PageRequest.of(
                                    0, 10
                                ) // TODO: Hide values by using default values for the parameters
                            ).firstOrNull { it.predicate.id == ObjectService.OrcidPredicate }
                                ?: throw OrphanOrcidValue(orcidValue)
                            (authorStatement.subject as Resource).id
                        } else {
                            // create resource
                            val authorId = resourceService.create(
                                CreateResourceUseCase.CreateCommand(
                                    label = it.label!!,
                                    classes = setOf(ObjectService.AuthorClass),
                                    extractionMethod = paper.paper.extractionMethod,
                                    contributorId = userId,
                                    observatoryId = observatoryId,
                                    organizationId = organizationId
                                )
                            )
                            // Create orcid literal
                            val orcid = literalService.create(userId, orcidValue)
                            // Add ORCID id to the new resource
                            statementService.add(
                                userId, authorId, ObjectService.OrcidPredicate, orcid.id
                            )
                            authorId
                        }
                    } else {
                        // create literal
                        literalService.create(userId, it.label!!).id
                    }
                } else {
                    it.id!!
                }
            }
            val listId = listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = authors,
                    contributorId = userId
                )
            )
            statementService.create(
                userId = userId,
                subject = paperId,
                predicate = ObjectService.hasAuthorsPredicate,
                `object` = listId
            )
        }
    }

    override fun findPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath> =
        repository.findAllPapersRelatedToResource(related, pageable)
}