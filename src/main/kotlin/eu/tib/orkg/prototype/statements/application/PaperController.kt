package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.application.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import java.util.LinkedList
import java.util.Queue
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

const val ID_DOI_PREDICATE = "P26"
const val ID_AUTHOR_PREDICATE = "P27"
const val ID_PUBDATE_MONTH_PREDICATE = "P28"
const val ID_PUBDATE_YEAR_PREDICATE = "P29"
const val ID_RESEARCH_FIELD_PREDICATE = "P30"
const val ID_CONTRIBUTION_PREDICATE = "P31"
const val ID_CONTRIBUTION_CLASS = "Contribution"
const val ID_ORCID_PREDICATE = "HAS_ORCID"
const val ID_AUTHOR_CLASS = "Author"
const val ID_VENUE_CLASS = "Venue"
const val ID_VENUE_PREDICATE = "HAS_VENUE"
const val ID_URL_PREDICATE = "url"
val MAP_PREDICATE_CLASSES = mapOf("P32" to "Problem")

/** Regular expression to check whether an input string is a valid ORCID id.  */
private const val ORCID_REGEX =
    "^\\s*(?:(?:https?://)?orcid.org/)?([0-9]{4})-?([0-9]{4})-?([0-9]{4})-?(([0-9]{4})|([0-9]{3}X))\\s*\$"

@RestController
@RequestMapping("/api/papers/")
class PaperController(
    private val resourceService: ResourceService,
    private val literalService: LiteralService,
    private val predicateService: PredicateService,
    private val statementService: StatementService,
    private val classService: ClassService,
    private val contributorService: ContributorService
) : BaseController() {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<Resource> {
        val resource = insertData(paper, mergeIfExists)
        val location = uriComponentsBuilder
            .path("api/resources/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }

    fun insertData(
        request: CreatePaperRequest,
        mergeIfExists: Boolean
    ): Resource {
        val userId = authenticatedUserId()
        val hasContributionPredicate = predicateService.findById(PredicateId(ID_CONTRIBUTION_PREDICATE)).get().id!!

        val contributionClass = getOrCreateClass(ID_CONTRIBUTION_CLASS, userId)

        val user = contributorService.findByIdOrElseUnknown(userId)
        val organizationId = user.organizationId
        val observatoryId = user.observatoryId

        val predicates: HashMap<String, PredicateId> = HashMap()
        if (request.predicates != null) {
            request.predicates.forEach {
                val surrogateId = it[it.keys.first()]!!
                val predicateId = predicateService.create(userId, it.keys.first()).id!!
                predicates[surrogateId] = predicateId
            }
        }

        if (request.paper.contributions != null) request.paper.contributions.forEach {
            checkContributionData(it.values!!, predicates)
        }

        // check if should be merged or not
        val paperObj = createOrFindPaper(mergeIfExists, request, userId)
        val paperId = paperObj.id!!

        val tempResources: HashMap<String, String> = HashMap()

        // paper contribution data
        if (request.paper.contributions != null) {
            val contributionClassSet = setOf(contributionClass)
            request.paper.contributions.forEach {
                // Create contribution resource whether it has data or not
                val contributionId = resourceService.create(
                    userId,
                    CreateResourceRequest(null, it.name, contributionClassSet),
                    observatoryId,
                    request.paper.extractionMethod,
                    organizationId
                ).id!!
                statementService.create(userId, paperId.value, hasContributionPredicate, contributionId.value)
                // Check if the contribution has more statements to add
                if (it.values != null && it.values.count() > 0) {
                    val resourceQueue: Queue<TempResource> = LinkedList()
                    processContributionData(contributionId, it.values, tempResources, predicates, resourceQueue, userId, observatoryId = observatoryId, extractionMethod = request.paper.extractionMethod, organizationId = organizationId)
                }
            }
        }
        return paperObj
    }

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
            found ?: addNewPaper(userId, request)
        } else {
            addNewPaper(userId, request)
        }
    }

    private fun addNewPaper(userId: UUID, request: CreatePaperRequest): Resource {
        val hasDoiPredicate = predicateService.findById(PredicateId(ID_DOI_PREDICATE)).get().id!!
        val publicationMonthPredicate = predicateService.findById(PredicateId(ID_PUBDATE_MONTH_PREDICATE)).get().id!!
        val publicationYearPredicate = predicateService.findById(PredicateId(ID_PUBDATE_YEAR_PREDICATE)).get().id!!
        val researchFieldPredicate = predicateService.findById(PredicateId(ID_RESEARCH_FIELD_PREDICATE)).get().id!!
        val urlPredicate = predicateService.findById(PredicateId(ID_URL_PREDICATE)).get().id!!

        val user = contributorService.findByIdOrElseUnknown(userId)
        val organizationId = user.organizationId
        val observatoryId = user.observatoryId

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
            statementService.create(userId, paperId.value, hasDoiPredicate, paperDoi.value)
        }

        // paper URL
        if (request.paper.hasUrl()) {
            val paperUrl = literalService.create(userId, request.paper.url!!).id!!
            statementService.create(userId, paperId.value, urlPredicate, paperUrl.value)
        }

        // paper authors
        handleAuthors(request, userId, paperId, observatoryId, organizationId)

        // paper publication date
        if (request.paper.publicationMonth != null)
            statementService.create(
                userId,
                paperId.value,
                publicationMonthPredicate,
                literalService.create(userId, request.paper.publicationMonth.toString()).id!!.value
            )
        if (request.paper.publicationYear != null)
            statementService.create(
                userId,
                paperId.value,
                publicationYearPredicate,
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
            researchFieldPredicate,
            ResourceId(request.paper.researchField).value
        )
        return paperObj
    }

    fun handlePublishingVenue(
        venue: String,
        paperId: ResourceId,
        userId: UUID,
        observatoryId: UUID,
        extractionMethod: ExtractionMethod,
        organizationId: UUID
    ) {
        val venueClass = getOrCreateClass(ID_VENUE_CLASS, userId)
        val venuePredicate = predicateService.findById(PredicateId(ID_VENUE_PREDICATE)).get().id!!
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
                    setOf(venueClass)
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

    private fun getOrCreateClass(classId: String, userId: UUID): ClassId {
        val optionalClass = classService.findById(ClassId(classId))
        return if (optionalClass.isPresent)
            optionalClass.get().id!!
        else
            classService.create(
                userId,
                CreateClassRequest(ClassId(classId), classId, null)
            ).id!!
    }

    fun handleAuthors(
        paper: CreatePaperRequest,
        userId: UUID,
        paperId: ResourceId,
        observatoryId: UUID,
        organizationId: UUID
    ) {
        val hasOrcidPredicate = predicateService.findById(PredicateId(ID_ORCID_PREDICATE)).get().id!!
        val hasAuthorPredicate = predicateService.findById(PredicateId(ID_AUTHOR_PREDICATE)).get().id!!
        val pattern = ORCID_REGEX.toRegex()
        if (paper.paper.authors != null) {
            paper.paper.authors.forEach { it ->
                if (it.id == null) {
                    if (it.label != null && it.orcid != null) {
                        // Check if class exists, add it otherwise
                        val authorClass = classService.findById(ClassId(ID_AUTHOR_CLASS))
                        val authorClassId = if (authorClass.isPresent)
                            authorClass.get().id!!
                        else
                            classService.create(
                                userId,
                                CreateClassRequest(
                                    ClassId(ID_AUTHOR_CLASS),
                                    ID_AUTHOR_CLASS,
                                    null
                                )
                            ).id!!
                        // Check if ORCID is a valid string
                        if (!pattern.matches(it.orcid)) {
                            throw RuntimeException("ORCID <${it.orcid}> is not valid string")
                        }
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
                                    createPageable(
                                        1,
                                        10,
                                        null,
                                        false
                                    ) // TODO: Hide values by using default values for the parameters
                                ).firstOrNull { it.predicate.id == hasOrcidPredicate }
                                    ?: throw RuntimeException("ORCID <$orcidValue> is not attached to any author!")
                            statementService.create(
                                userId,
                                paperId.value,
                                hasAuthorPredicate,
                                (authorStatement.subject as Resource).id!!.value
                            )
                        } else {
                            // create resource
                            val author = resourceService.create(
                                userId,
                                CreateResourceRequest(null, it.label, setOf(authorClassId)),
                                observatoryId,
                                paper.paper.extractionMethod,
                                organizationId
                            )
                            statementService.create(
                                userId,
                                paperId.value,
                                hasAuthorPredicate,
                                author.id!!.value
                            )
                            // Create orcid literal
                            val orcid = literalService.create(userId, orcidValue)
                            // Add ORCID id to the new resource
                            statementService.create(userId, author.id.value, hasOrcidPredicate, orcid.id!!.value)
                        }
                    } else {
                        // create literal and link it
                        statementService.create(
                            userId,
                            paperId.value,
                            hasAuthorPredicate,
                            literalService.create(userId, it.label!!).id!!.value
                        )
                    }
                } else {
                    statementService.create(userId, paperId.value, hasAuthorPredicate, it.id)
                }
            }
        }
    }

    fun checkContributionData(
        data: HashMap<String, List<PaperValue>>,
        predicates: HashMap<String, PredicateId>
    ) {
        for ((predicate, value) in data) {
            val predicateId = if (predicate.startsWith("_")) {
                predicates[predicate]
            } else {
                PredicateId(predicate) //
            }
            if (!predicateService.findById(predicateId).isPresent)
                throw PredicateNotFound(predicate)
            for (resource in value) {
                when {
                    resource.`@id` != null -> { // Add an existing resource or literal
                        when {
                            resource.`@id`.startsWith("L") -> {
                                val id = resource.`@id`
                                if (!literalService.findById(LiteralId(id)).isPresent)
                                    throw RuntimeException("Literal $id is not found")
                            }
                            resource.`@id`.startsWith("R") -> {
                                val id = resource.`@id`
                                if (!resourceService.findById(ResourceId(id)).isPresent)
                                    throw RuntimeException("Resource $id is not found")
                            }
                        }
                    }
                    resource.`class` != null -> { // Check for existing classes
                        val id = resource.`class`
                        if (!classService.findById(ClassId(id)).isPresent)
                            throw RuntimeException("Class $id is not found")
                    }
                }
                if (resource.values != null) {
                    checkContributionData(
                        resource.values,
                        predicates
                    )
                }
            }
        }
    }

    fun processContributionData(
        subject: ResourceId,
        data: HashMap<String, List<PaperValue>>,
        tempResources: HashMap<String, String>,
        predicates: HashMap<String, PredicateId>,
        resourceQueue: Queue<TempResource>,
        userId: UUID,
        recursive: Boolean = false,
        observatoryId: UUID,
        extractionMethod: ExtractionMethod,
        organizationId: UUID
    ) {

        for ((predicate, value) in data) {
            val predicateId = if (predicate.startsWith("_")) {
                predicates[predicate]
            } else {
                PredicateId(predicate)
            }
            for (resource in value) {
                when {
                    resource.`@id` != null -> { // Add an existing resource or literal
                        when {
                            resource.`@id`.startsWith("L") || resource.`@id`.startsWith("R") -> {
                                // Update existing resources with pre-defined classes
                                MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ClassId(it) }?.let {
                                    val res = resourceService.findById(ResourceId(resource.`@id`)).get()
                                    val newClasses = res.classes.toMutableSet()
                                    newClasses.add(it)
                                    resourceService.update(UpdateResourceRequest(res.id, null, newClasses))
                                }
                                statementService.create(userId, subject.value, predicateId, resource.`@id`)
                            }
                            resource.`@id`.startsWith("_") -> {
                                if (!tempResources.containsKey(resource.`@id`))
                                    resourceQueue.add(TempResource(subject, predicateId!!, resource.`@id`))
                                else {
                                    val tempId = tempResources[resource.`@id`]
                                    statementService.create(userId, subject.value, predicateId!!, tempId!!)
                                }
                            }
                        }
                    }
                    resource.text != null -> { // create new literal
                        val newLiteral = literalService.create(
                                userId,
                                resource.text,
                                resource.datatype ?: "xsd:string"
                            ).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newLiteral.value
                        }
                        statementService.create(userId, subject.value, predicateId!!, newLiteral.value)
                    }
                    resource.label != null -> { // create new resource
                        // Check for classes of resource
                        val classes = mutableListOf<ClassId>()
                        // add attached classes
                        if (resource.`class` != null) {
                            classes.add(ClassId(resource.`class`))
                        }
                        // add pre-defined classes
                        MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ClassId(it) }?.let { classes.add(it) }
                        // Create resource
                        val newResource = if (classes.isNotEmpty())
                            resourceService.create(
                                userId,
                                CreateResourceRequest(null, resource.label, classes.toSet()),
                                observatoryId,
                                extractionMethod,
                                organizationId
                            ).id!!
                        else
                            resourceService.create(userId, resource.label, observatoryId, extractionMethod, organizationId).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newResource.value
                        }
                        statementService.create(userId, subject.value, predicateId, newResource.value)
                        if (resource.values != null) {
                            processContributionData(
                                newResource,
                                resource.values,
                                tempResources,
                                predicates,
                                resourceQueue,
                                userId,
                                true,
                                observatoryId,
                                extractionMethod,
                                organizationId
                            )
                        }
                    }
                }
            }
        }
        // Loop until the Queue is empty
        var limit = 50 // this is just to ensure that a user won't add an id that is not there
        while (!recursive && !resourceQueue.isEmpty() && limit > 0) {
            val temp = resourceQueue.remove()
            limit--
            if (tempResources.containsKey(temp.`object`)) {
                val tempId = tempResources[temp.`object`]
                statementService.create(userId, temp.subject.value, temp.predicate, tempId!!)
            } else {
                resourceQueue.add(temp)
            }
        }
    }
}

data class CreatePaperRequest(
    val predicates: List<HashMap<String, String>>?,
    val paper: Paper
)

data class Paper(
    val title: String,
    val doi: String?,
    val authors: List<Author>?,
    val publicationMonth: Int?,
    val publicationYear: Int?,
    val publishedIn: String?,
    val url: String?,
    val researchField: String,
    val contributions: List<Contribution>?,
    val extractionMethod: ExtractionMethod = UNKNOWN
) {
    fun hasPublishedIn(): Boolean =
        publishedIn?.isNotEmpty() != null

    fun hasDOI(): Boolean =
        doi?.isNotEmpty() != null

    fun hasUrl(): Boolean =
        url?.isNotEmpty() != null
}

data class Author(
    val id: String?,
    val label: String?,
    val orcid: String?
)

data class Contribution(
    val name: String,
    val values: HashMap<String, List<PaperValue>>?
)

data class PaperValue(
    val `@id`: String?,
    val `class`: String?,
    val `@temp`: String?,
    val text: String?,
    val datatype: String?,
    val label: String?,
    val values: HashMap<String, List<PaperValue>>?
)

data class TempResource(
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: String
)
