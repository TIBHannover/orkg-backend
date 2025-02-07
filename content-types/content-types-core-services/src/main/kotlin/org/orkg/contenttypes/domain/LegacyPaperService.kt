package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.domain.identifiers.ORCID
import org.orkg.contenttypes.input.LegacyCreatePaperUseCase
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.OrphanOrcidValue
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateObjectUseCase
import org.orkg.graph.input.CreateObjectUseCase.CreateObjectRequest
import org.orkg.graph.input.CreateObjectUseCase.NamedObject
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
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
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val contributorService: RetrieveContributorUseCase,
    private val objectService: CreateObjectUseCase,
    private val resourceRepository: ResourceRepository,
    private val repository: PaperRepository,
    private val listService: ListUseCases,
) : LegacyPaperUseCases {
    /**
     * Main entry point, to create paper and check contributions
     * Using the Object endpoint to handle recursive object creation
     */
    override fun addPaperContent(
        request: LegacyCreatePaperUseCase.LegacyCreatePaperRequest,
        mergeIfExists: Boolean,
        userUUID: UUID,
    ): ThingId {
        val userId = ContributorId(userUUID)

        // check if paper should be merged or not
        val paperId = createOrFindPaper(mergeIfExists, request, userId)

        // paper contribution data
        if (request.paper.hasContributions()) {
            request.paper.contributions!!.forEach {
                val contributionId = addCompleteContribution(it, request, userUUID)
                // Create statement between paper and contribution
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = userId,
                        subjectId = paperId,
                        predicateId = Predicates.hasContribution,
                        objectId = contributionId
                    )
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
        paperRequest: LegacyCreatePaperUseCase.LegacyCreatePaperRequest,
        userUUID: UUID,
    ): ThingId {
        // Always append Contribution class to custom user classes
        val contributionClasses =
            (listOf(Classes.contribution.value) + jsonObject.classes.orEmpty()).toSet().toList()
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
        request: LegacyCreatePaperUseCase.LegacyCreatePaperRequest,
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
        request: LegacyCreatePaperUseCase.LegacyCreatePaperRequest
    ): ThingId {
        // Do this in a sequential order, first check for DOI and then title, otherwise we create a new paper
        if (request.paper.hasDOI()) {
            val byDOI = resourceService.findByDOI(request.paper.doi!!, classes = setOf(Classes.paper))
            if (byDOI.isPresent) return byDOI.get().id
        }
        val byTitle = resourceService.findAllPapersByTitle(request.paper.title)
        if (byTitle.isNotEmpty()) return byTitle.first().id
        return createNewPaperWithMetadata(userId, request)
    }

    /**
     * Handles the creation of a new paper resource
     * i.e., creates the new paper, meta-data
     */
    private fun createNewPaperWithMetadata(userId: ContributorId, request: LegacyCreatePaperUseCase.LegacyCreatePaperRequest): ThingId {
        val contributor = contributorService.findById(userId).orElse(Contributor.UNKNOWN)
        val organizationId = contributor.organizationId
        val observatoryId = contributor.observatoryId

        // paper title
        val paperId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                label = request.paper.title,
                classes = setOf(Classes.paper),
                extractionMethod = request.paper.extractionMethod,
                contributorId = userId,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        )

        // paper doi
        if (request.paper.hasDOI()) {
            val paperDoi = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = userId,
                    label = request.paper.doi!!
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = userId,
                    subjectId = paperId,
                    predicateId = Predicates.hasDOI,
                    objectId = paperDoi
                )
            )
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = userId,
                    label = request.paper.url!!
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = userId,
                    subjectId = paperId,
                    predicateId = Predicates.hasURL,
                    objectId = paperUrl
                )
            )
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.hasPublicationMonth()) unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = userId,
                subjectId = paperId,
                predicateId = Predicates.monthPublished,
                objectId = literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = userId,
                        label = request.paper.publicationMonth.toString(),
                        datatype = "xsd:integer"
                    )
                )
            )
        )
        if (request.paper.hasPublicationYear()) unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = userId,
                subjectId = paperId,
                predicateId = Predicates.yearPublished,
                objectId = literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = userId,
                        label = request.paper.publicationYear.toString(),
                        datatype = "xsd:integer"
                    )
                )
            )
        )

        // paper published At
        if (request.paper.hasPublishedIn()) handlePublishingVenue(
            request.paper.publishedIn!!, paperId, userId, observatoryId, request.paper.extractionMethod, organizationId
        )

        // paper research field
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = userId,
                subjectId = paperId,
                predicateId = Predicates.hasResearchField,
                objectId = request.paper.researchField
            )
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
        val venuePredicate = predicateService.findById(Predicates.hasVenue).get().id
        // Check if resource exists
        val venueResource = resourceRepository.findAll(
            includeClasses = setOf(Classes.venue),
            label = SearchString.of(venue, exactMatch = true),
            pageable = PageRequest.of(0, 1)
        ).singleOrNull() ?: run {
            val resourceId = resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = venue,
                    classes = setOf(Classes.venue),
                    extractionMethod = extractionMethod,
                    contributorId = userId,
                    observatoryId = observatoryId,
                    organizationId = organizationId
                )
            )
            resourceRepository.findById(resourceId).get()
        }
        // create a statement with the venue resource
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = userId,
                subjectId = paperId,
                predicateId = venuePredicate,
                objectId = venueResource.id
            )
        )
    }

    /**
     * Handles the authors of a paper
     * Create orcid nodes if needed and creates author literal or
     * resource depending on that
     */
    fun handleAuthors(
        paper: LegacyCreatePaperUseCase.LegacyCreatePaperRequest,
        userId: ContributorId,
        paperId: ThingId,
        observatoryId: ObservatoryId,
        organizationId: OrganizationId
    ) {
        if (paper.paper.hasAuthors()) {
            val authors = paper.paper.authors!!.map { it ->
                if (!it.hasId()) {
                    if (it.hasNameAndOrcid()) {
                        // Check if ORCID is a valid string
                        val orcidValue = ORCID.of(it.orcid!!).value
                        // Check if the orcid exists in the system or not
                        val foundOrcid = literalService.findAll(
                            label = SearchString.of(orcidValue, exactMatch = true),
                            pageable = PageRequest.of(0, 1)
                        ).firstOrNull()
                        if (foundOrcid != null) {
                            // Link existing ORCID
                            val authorStatement = statementService.findAll(
                                objectId = foundOrcid.id,
                                pageable = PageRequest.of(0, 10)
                            ).firstOrNull { it.predicate.id == Predicates.hasORCID }
                                ?: throw OrphanOrcidValue(orcidValue)
                            (authorStatement.subject as Resource).id
                        } else {
                            // create resource
                            val authorId = resourceService.create(
                                CreateResourceUseCase.CreateCommand(
                                    label = it.label!!,
                                    classes = setOf(Classes.author),
                                    extractionMethod = paper.paper.extractionMethod,
                                    contributorId = userId,
                                    observatoryId = observatoryId,
                                    organizationId = organizationId
                                )
                            )
                            // Create orcid literal
                            val orcidLiteralId = literalService.create(
                                CreateLiteralUseCase.CreateCommand(
                                    contributorId = userId,
                                    label = orcidValue
                                )
                            )
                            // Add ORCID id to the new resource
                            unsafeStatementUseCases.create(
                                CreateStatementUseCase.CreateCommand(
                                    contributorId = userId,
                                    subjectId = authorId,
                                    predicateId = Predicates.hasORCID,
                                    objectId = orcidLiteralId
                                )
                            )
                            authorId
                        }
                    } else {
                        // create literal
                        literalService.create(
                            CreateLiteralUseCase.CreateCommand(
                                contributorId = userId,
                                label = it.label!!
                            )
                        )
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
                CreateStatementUseCase.CreateCommand(
                    contributorId = userId,
                    subjectId = paperId,
                    predicateId = Predicates.hasAuthors,
                    objectId = listId
                )
            )
        }
    }

    override fun findPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath> =
        repository.findAllPapersRelatedToResource(related, pageable)
}
