package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/objects/")
class ObjectController(
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
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Resource> {
        val resource = createObject(obj)
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }

    /**
     * Creates an object into the ORKG
     * and object here is the term, like a json-object
     * and not as an object in the RDF-statement.
     * This object allows for the flexibility of adding sub-graphs into
     * the ORKG that are not rooted in a paper.
     */
    fun createObject(request: CreateObjectRequest): Resource {
        // Get provenance info
        val userId = authenticatedUserId()
        val contributor = contributorService.findByIdOrElseUnknown(userId)
        val organizationId = contributor.organizationId
        val observatoryId = contributor.observatoryId

        // Handle predicates (temp and existing)
        val predicates: HashMap<String, PredicateId> = HashMap()
        if (request.hasTempPredicates()) {
            request.predicates!!.forEach {
                val surrogateId = it[it.keys.first()]!!
                val predicateId = predicateService.create(userId, it.keys.first()).id!!
                predicates[surrogateId] = predicateId
            }
        }

        // Check if object statements are valid
        if (request.resource.hasSubsequentStatements())
                checkObjectStatements(request.resource.values!!, predicates)

        val tempResources: HashMap<String, String> = HashMap()

        // Create the resource
        val classes = if (request.resource.isTyped())
                request.resource.classes!!.map { ClassId(it) }.toSet()
            else
                emptySet()
        val resource = resourceService.create(
            userId,
            CreateResourceRequest(null, request.resource.name, classes),
            observatoryId,
            request.resource.extractionMethod,
            organizationId
        )
        val resourceId = resource.id!!

        // Check if the contribution has more statements to add
        if (request.resource.hasSubsequentStatements()) {
            val resourceQueue: Queue<TempResource> = LinkedList()
            goThroughStatementsRecursively(
                resourceId,
                request.resource.values!!,
                tempResources,
                predicates,
                resourceQueue,
                userId,
                observatoryId = observatoryId,
                extractionMethod = request.resource.extractionMethod,
                organizationId = organizationId
            )
        }
        return resource
    }

    /**
     * Check for the validity of the object statements
     * make sure that all the referred to existing resources/literals
     * are existing in the graph
     */
    fun checkObjectStatements(
        data: HashMap<String, List<ObjectStatement>>,
        predicates: HashMap<String, PredicateId>
    ) {
        for ((predicate, value) in data) {
            val predicateId = extractPredicate(predicate, predicates)
            if (!predicateService.findById(predicateId).isPresent)
                throw PredicateNotFound(predicate)
            for (jsonObject in value) {
                when {
                    jsonObject.isExisting() -> { // Add an existing resource or literal
                        when {
                            jsonObject.isExistingLiteral() -> {
                                val id = jsonObject.`@id`!!
                                if (!literalService.findById(LiteralId(id)).isPresent)
                                    throw LiteralNotFound(id)
                            }
                            jsonObject.isExistingResource() -> {
                                val id = jsonObject.`@id`!!
                                if (!resourceService.findById(ResourceId(id)).isPresent)
                                    throw ResourceNotFound(id)
                            }
                        }
                    }
                    jsonObject.isTyped() -> { // Check for existing classes
                        jsonObject.classes!!.forEach {
                            if (!classService.findById(ClassId(it)).isPresent)
                                throw ClassNotFound(it)
                        }
                    }
                }
                if (jsonObject.hasSubsequentStatements())
                    checkObjectStatements(jsonObject.values!!, predicates)
            }
        }
    }

    /**
     * Process every resource/literal in the statements
     * do this recursively for all subsequent statements
     */
    fun goThroughStatementsRecursively(
        subject: ResourceId,
        data: HashMap<String, List<ObjectStatement>>,
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
            val predicateId = extractPredicate(predicate, predicates)
            for (resource in value) {
                when {
                    resource.isExisting() -> { // Add an existing resource or literal
                        when {
                            resource.isExistingResource() || resource.isExistingLiteral() -> {
                                // Update existing resources with pre-defined classes
                                typeResourceBasedOnPredicate(predicateId, resource)
                                statementService.create(userId, subject.value, predicateId!!, resource.`@id`!!)
                            }
                            resource.isTempResource() -> {
                                if (!tempResources.containsKey(resource.`@id`))
                                    resourceQueue.add(TempResource(subject, predicateId!!, resource.`@id`!!))
                                else {
                                    val tempId = tempResources[resource.`@id`]
                                    statementService.create(userId, subject.value, predicateId!!, tempId!!)
                                }
                            }
                        }
                    }
                    resource.isNewLiteral() -> { // create new literal
                        val newLiteral = literalService.create(
                            userId,
                            resource.text!!,
                            resource.datatype ?: "xsd:string"
                        ).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newLiteral.value
                        }
                        statementService.create(userId, subject.value, predicateId!!, newLiteral.value)
                    }
                    resource.isNewResource() -> { // create new resource
                        // Check for classes of resource
                        val classes = mutableListOf<ClassId>()
                        // add attached classes
                        if (resource.isTyped()) {
                            resource.classes!!.forEach {
                                classes.add(ClassId(it))
                            }
                        }
                        // add pre-defined classes
                        MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ClassId(it) }?.let { classes.add(it) }
                        // Create resource
                        val newResource = if (classes.isNotEmpty())
                            resourceService.create(
                                userId,
                                CreateResourceRequest(null, resource.label!!, classes.toSet()),
                                observatoryId,
                                extractionMethod,
                                organizationId
                            ).id!!
                        else
                            resourceService.create(
                                userId,
                                resource.label!!,
                                observatoryId,
                                extractionMethod,
                                organizationId
                            ).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newResource.value
                        }
                        statementService.create(userId, subject.value, predicateId, newResource.value)
                        if (resource.hasSubsequentStatements()) {
                            goThroughStatementsRecursively(
                                newResource,
                                resource.values!!,
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

    /**
     * Type an existing resource with the range of the predicate if required
     */
    private fun typeResourceBasedOnPredicate(
        predicateId: PredicateId?,
        resource: ObjectStatement
    ) {
        MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ClassId(it) }?.let {
            val res = resourceService.findById(ResourceId(resource.`@id`!!)).get()
            val newClasses = res.classes.toMutableSet()
            newClasses.add(it)
            resourceService.update(UpdateResourceRequest(res.id, null, newClasses))
        }
    }

    /**
     * Find a predicate in the temp list of predicates
     * or create a new PredicateId object for it
     */
    private fun extractPredicate(
        predicate: String,
        predicates: HashMap<String, PredicateId>
    ): PredicateId? {
        return if (predicate.startsWith("_"))
            predicates[predicate]
        else
            PredicateId(predicate)
    }

    /**
     * Constants companion object
     */
    companion object Constants {
        // IDs of predicates
        const val ID_DOI_PREDICATE = "P26"
        const val ID_AUTHOR_PREDICATE = "P27"
        const val ID_PUBDATE_MONTH_PREDICATE = "P28"
        const val ID_PUBDATE_YEAR_PREDICATE = "P29"
        const val ID_RESEARCH_FIELD_PREDICATE = "P30"
        const val ID_CONTRIBUTION_PREDICATE = "P31"
        const val ID_URL_PREDICATE = "url"
        const val ID_ORCID_PREDICATE = "HAS_ORCID"
        const val ID_VENUE_PREDICATE = "HAS_VENUE"
        // IDs of classes
        const val ID_CONTRIBUTION_CLASS = "Contribution"
        const val ID_AUTHOR_CLASS = "Author"
        const val ID_VENUE_CLASS = "Venue"
        // Miscellaneous
        val MAP_PREDICATE_CLASSES = mapOf("P32" to "Problem")
        /** Regular expression to check whether an input string is a valid ORCID id.  */
        const val ORCID_REGEX =
            "^\\s*(?:(?:https?://)?orcid.org/)?([0-9]{4})-?([0-9]{4})-?([0-9]{4})-?(([0-9]{4})|([0-9]{3}X))\\s*\$"

        // Public properties
        val contributionPredicate: PredicateId
            get() = PredicateId(ID_CONTRIBUTION_PREDICATE)

        val doiPredicate: PredicateId
            get() = PredicateId(ID_DOI_PREDICATE)

        val authorPredicate: PredicateId
            get() = PredicateId(ID_AUTHOR_PREDICATE)

        val publicationMonthPredicate: PredicateId
            get() = PredicateId(ID_PUBDATE_MONTH_PREDICATE)

        val publicationYearPredicate: PredicateId
            get() = PredicateId(ID_PUBDATE_YEAR_PREDICATE)

        val researchFieldPredicate: PredicateId
            get() = PredicateId(ID_RESEARCH_FIELD_PREDICATE)

        val orcidPredicate: PredicateId
            get() = PredicateId(ID_ORCID_PREDICATE)

        val venuePredicate: PredicateId
            get() = PredicateId(ID_VENUE_PREDICATE)

        val urlPredicate: PredicateId
            get() = PredicateId(ID_URL_PREDICATE)

        val contributionClass: ClassId
            get() = ClassId(ID_CONTRIBUTION_CLASS)

        val authorClass: ClassId
            get() = ClassId(ID_AUTHOR_CLASS)

        val venueClass: ClassId
            get() = ClassId(ID_VENUE_CLASS)
    }
}

data class CreateObjectRequest(
    val predicates: List<HashMap<String, String>>?,
    val resource: NamedObject
) {
    /**
     * Check if the object has a set
     * of predicates to be preocessed
     */
    fun hasTempPredicates() =
        this.predicates != null && this.predicates.count() > 0
}

data class NamedObject(
    val name: String,
    val classes: List<String>?,
    val values: HashMap<String, List<ObjectStatement>>?,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
) {
    /**
     * Check if the object has a set
     * of statements to be added recursively
     */
    fun hasSubsequentStatements() =
        this.values != null && this.values.count() > 0

    /**
     * Check if the resource is typed
     * i.e., it has classes
     */
    fun isTyped() =
        this.classes != null && this.classes.isNotEmpty()
}

data class ObjectStatement(
    val `@id`: String?,
    val classes: List<String>?,
    val `@temp`: String?,
    val text: String?,
    val datatype: String?,
    val label: String?,
    val values: HashMap<String, List<ObjectStatement>>?
) {

    /**
     * Indicate if the resource is an existing thing
     * i.e., the @id property is used in the json object
     */
    fun isExisting() =
        this.`@id` != null

    /**
     * Check if the resource is existing
     * and that the id starts with an R
     */
    fun isExistingResource() =
        this.isExisting() && this.`@id`!!.startsWith("R")

    /**
     * Check if the literal exists
     * and that the id starts with an L
     */
    fun isExistingLiteral() =
        this.isExisting() && this.`@id`!!.startsWith("L")

    /**
     * Check if the resource is a temp resource
     * i.e., its id starts with an _
     */
    fun isTempResource() =
        this.`@id`!!.startsWith("_")

    /**
     * Check if the resource is typed
     * i.e., it has classes
     */
    fun isTyped() =
        this.classes != null && this.classes.isNotEmpty()

    /**
     * Check if this is a new resource to be created
     * i.e., the label property in the json object is used
     */
    fun isNewResource() =
        this.label != null

    /**
     * Check if this is a new literal to be created
     * i.e., the text property in the json object is used
     */
    fun isNewLiteral() =
        this.text != null

    /**
     * Check if the object has a set
     * of statements to be added recursively
     */
    fun hasSubsequentStatements() =
        this.values != null && this.values.count() > 0
}

data class TempResource(
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: String
)
