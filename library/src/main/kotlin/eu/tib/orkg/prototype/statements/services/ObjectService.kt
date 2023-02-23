package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ClassNotFound
import eu.tib.orkg.prototype.statements.application.CreateObjectRequest
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.LiteralNotFound
import eu.tib.orkg.prototype.statements.application.ObjectStatement
import eu.tib.orkg.prototype.statements.application.PredicateNotFound
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.TempResource
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.stereotype.Service

@Service
class ObjectService(
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementService: StatementUseCases,
    private val classService: ClassUseCases,
    private val contributorService: ContributorService,
) {

    /**
     * Creates an object into the ORKG
     * and object here is the term, like a json-object
     * and not as an object in the RDF-statement.
     * This object allows for the flexibility of adding sub-graphs into
     * the ORKG that are not rooted in a paper.
     */
    fun createObject(
        request: CreateObjectRequest,
        existingThingId: ThingId? = null,
        userUUID: UUID,
    ): ResourceRepresentation {
        // Get provenance info
        val userId = ContributorId(userUUID)
        val contributor = contributorService.findByIdOrElseUnknown(userId)
        val organizationId: OrganizationId
        val observatoryId: ObservatoryId
        // if comparisons is assigned a conference i.e, organizationId will be available and observatoryId  will not be.
        if (request.resource.organizationId != OrganizationId.createUnknownOrganization() && request.resource.observatoryId == ObservatoryId.createUnknownObservatory()) {
            organizationId = request.resource.organizationId
            observatoryId = request.resource.observatoryId
        } else {
            organizationId = contributor.organizationId
            observatoryId = contributor.observatoryId
        }

        // Handle predicates (temp and existing)
        val predicates: HashMap<String, ThingId> = HashMap()
        if (request.hasTempPredicates()) {
            request.predicates!!.forEach {
                val surrogateId = it[it.keys.first()]!!
                val predicateId = predicateService.create(userId, it.keys.first()).id
                predicates[surrogateId] = predicateId
            }
        }

        // Check if object statements are valid
        if (request.resource.hasSubsequentStatements()) checkObjectStatements(request.resource.values!!, predicates)
        if (request.resource.isTyped()) request.resource.classes!!.forEach { checkIfClassExists(it) }

        val tempResources: HashMap<String, ThingId> = HashMap()

        // Create the resource
        val resourceId = existingThingId
            ?: resourceService.create(
                userId,
                CreateResourceRequest(null, request.resource.name, request.resource.classes.toThingIds()),
                observatoryId,
                request.resource.extractionMethod,
                organizationId
            ).id

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
        return resourceService.findById(resourceId).get()
    }

    /**
     * Check for the validity of the object statements
     * make sure that all the referred to existing resources/literals
     * are existing in the graph
     */
    fun checkObjectStatements(
        data: HashMap<String, List<ObjectStatement>>,
        predicates: HashMap<String, ThingId>
    ) {
        for ((predicate, value) in data) {
            val predicateId = extractPredicate(predicate, predicates)
            checkIfPredicateExists(predicateId!!.value)
            for (jsonObject in value) {
                if (jsonObject.isExisting()) // Add an existing resource or literal
                    when {
                        jsonObject.isExistingLiteral() -> checkIfLiteralExists(jsonObject.`@id`!!)
                        jsonObject.isExistingResource() -> checkIfResourceExists(jsonObject.`@id`!!)
                        jsonObject.isExistingPredicate() -> checkIfPredicateExists(jsonObject.`@id`!!)
                        jsonObject.isExistingClass() -> checkIfClassExists(jsonObject.`@id`!!)
                    }
                if (jsonObject.isTyped()) // Check for existing classes
                    jsonObject.classes!!.forEach { checkIfClassExists(it) }
                if (jsonObject.hasSubsequentStatements()) checkObjectStatements(jsonObject.values!!, predicates)
            }
        }
    }

    /**
     * Process every resource/literal in the statements
     * do this recursively for all subsequent statements
     */
    fun goThroughStatementsRecursively(
        subject: ThingId,
        data: HashMap<String, List<ObjectStatement>>,
        tempResources: HashMap<String, ThingId>,
        predicates: HashMap<String, ThingId>,
        resourceQueue: Queue<TempResource>,
        userId: ContributorId,
        recursive: Boolean = false,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ) {
        for ((predicate, value) in data) {
            val predicateId = extractPredicate(predicate, predicates)
            for (jsonObject in value) {
                when {
                    jsonObject.isExisting() -> { // Add an existing resource or literal
                        when {
                            jsonObject.isExistingResource() || jsonObject.isExistingLiteral() || jsonObject.isExistingPredicate() || jsonObject.isExistingClass() -> {
                                if (jsonObject.isExistingResource()) {
                                    // Update existing resources with pre-defined classes
                                    typeResourceBasedOnPredicate(predicateId, jsonObject)
                                }
                                statementService.add(userId, subject, predicateId!!, ThingId(jsonObject.`@id`!!))
                            }
                            jsonObject.isTempResource() -> {
                                if (!tempResources.containsKey(jsonObject.`@id`)) resourceQueue.add(
                                    TempResource(
                                        subject,
                                        predicateId!!,
                                        jsonObject.`@id`!!
                                    )
                                )
                                else {
                                    val tempId = tempResources[jsonObject.`@id`]
                                    statementService.add(userId, subject, predicateId!!, tempId!!)
                                }
                            }
                        }
                    }
                    jsonObject.isNewLiteral() -> { // create new literal
                        val newLiteral = literalService.create(
                            userId, jsonObject.text!!, jsonObject.datatype ?: "xsd:string"
                        ).id
                        if (jsonObject.`@temp` != null) {
                            tempResources[jsonObject.`@temp`] = newLiteral
                        }
                        statementService.add(userId, subject, predicateId!!, newLiteral)
                    }
                    jsonObject.isNewResource() -> { // create new resource
                        // Check for classes of resource
                        val classes = mutableListOf<ThingId>()
                        // add attached classes
                        if (jsonObject.isTyped()) {
                            jsonObject.classes!!.forEach {
                                classes.add(ThingId(it))
                            }
                        }
                        // add pre-defined classes
                        MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ThingId(it) }?.let { classes.add(it) }
                        // Create resource
                        val newResource = if (classes.isNotEmpty()) resourceService.create(
                            userId,
                            CreateResourceRequest(null, jsonObject.label!!, classes.toSet()),
                            observatoryId,
                            extractionMethod,
                            organizationId
                        ).id
                        else resourceService.create(
                            userId, jsonObject.label!!, observatoryId, extractionMethod, organizationId
                        ).id
                        if (jsonObject.`@temp` != null) {
                            tempResources[jsonObject.`@temp`] = newResource
                        }
                        statementService.add(userId, subject, predicateId, newResource)
                        if (jsonObject.hasSubsequentStatements()) {
                            goThroughStatementsRecursively(
                                newResource,
                                jsonObject.values!!,
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
        handleTempResourcesInQueue(resourceQueue, tempResources, recursive, userId)
    }

    /**
     * Go through the queue of temp resources
     * and add appropriate statements until queue is empty
     * or the limit (50) is reached
     */
    private fun handleTempResourcesInQueue(
        queue: Queue<TempResource>,
        tempResources: HashMap<String, ThingId>,
        isRecursive: Boolean,
        userId: ContributorId
    ) {
        // Loop until the Queue is empty
        var limit = 50 // this is just to ensure that a user won't add an id that is not there
        while (!isRecursive && !queue.isEmpty() && limit > 0) {
            val temp = queue.remove()
            limit--
            if (tempResources.containsKey(temp.`object`)) {
                val tempId = tempResources[temp.`object`]
                statementService.add(userId, temp.subject, temp.predicate, tempId!!)
            } else {
                queue.add(temp)
            }
        }
    }

    // <editor-fold desc="Helper Functions">

    /**
     * Type an existing resource with the range of the predicate if required
     */
    private fun typeResourceBasedOnPredicate(
        predicateId: ThingId?,
        resource: ObjectStatement
    ) {
        MAP_PREDICATE_CLASSES[predicateId!!.value]?.let { ThingId(it) }?.let {
            val res = resourceService.findById(ThingId(resource.`@id`!!)).get()
            val newClasses = res.classes.toMutableSet()
            newClasses.add(it)
            resourceService.update(UpdateResourceRequest(res.id, null, newClasses))
        }
    }

    /**
     * Check if a predicate is existing
     * o/w throw out a suitable exception
     */
    private fun checkIfPredicateExists(predicate: String) {
        if (!predicateService.exists(ThingId(predicate))) throw PredicateNotFound(predicate)
    }

    /**
     * Check if a literal is existing
     * o/w throw out a suitable exception
     */
    private fun checkIfLiteralExists(literalId: String) {
        if (!literalService.exists(ThingId(literalId))) throw LiteralNotFound(literalId)
    }

    /**
     * Check if a resource is existing
     * o/w throw out a suitable exception
     */
    private fun checkIfResourceExists(resourceId: String) {
        if (!resourceService.exists(ThingId(resourceId))) throw ResourceNotFound.withId(resourceId)
    }

    /**
     * Check if a class exists, otherwise throw out suitable exception.
     */
    private fun checkIfClassExists(it: String) {
        if (!classService.exists(ThingId(it))) throw ClassNotFound.withId(it)
    }

    /**
     * Find a predicate in the temp list of predicates
     * or create a new ThingId object for it
     */
    private fun extractPredicate(
        predicate: String,
        predicates: HashMap<String, ThingId>
    ): ThingId? {
        return if (predicate.startsWith("_")) predicates[predicate]
        else ThingId(predicate)
    }

    // </editor-fold>

    // <editor-fold desc="Constants">

    /**
     * Constants companion object
     */
    companion object Constants {
        // IDs of predicates
        const val ID_DOI_PREDICATE = "P26"
        private const val ID_AUTHOR_PREDICATE = "P27"
        private const val ID_PUBDATE_MONTH_PREDICATE = "P28"
        private const val ID_PUBDATE_YEAR_PREDICATE = "P29"
        private const val ID_RESEARCH_FIELD_PREDICATE = "P30"
        const val ID_CONTRIBUTION_PREDICATE = "P31"
        private const val ID_URL_PREDICATE = "url"
        private const val ID_ORCID_PREDICATE = "HAS_ORCID"
        private const val ID_VENUE_PREDICATE = "HAS_VENUE"

        // IDs of classes
        const val ID_CONTRIBUTION_CLASS = "Contribution"
        private const val ID_AUTHOR_CLASS = "Author"
        private const val ID_VENUE_CLASS = "Venue"

        // Miscellaneous
        val MAP_PREDICATE_CLASSES = mapOf("P32" to "Problem")

        /** Regular expression to check whether an input string is a valid ORCID id.  */
        const val ORCID_REGEX =
            "^\\s*(?:(?:https?://)?orcid.org/)?([0-9]{4})-?([0-9]{4})-?([0-9]{4})-?(([0-9]{4})|([0-9]{3}X))\\s*\$"

        // Properties
        val ContributionPredicate = ThingId(ID_CONTRIBUTION_PREDICATE)
        val DoiPredicate = ThingId(ID_DOI_PREDICATE)
        val AuthorPredicate = ThingId(ID_AUTHOR_PREDICATE)
        val PublicationMonthPredicate = ThingId(ID_PUBDATE_MONTH_PREDICATE)
        val PublicationYearPredicate = ThingId(ID_PUBDATE_YEAR_PREDICATE)
        val ResearchFieldPredicate = ThingId(ID_RESEARCH_FIELD_PREDICATE)
        val OrcidPredicate = ThingId(ID_ORCID_PREDICATE)
        val VenuePredicate = ThingId(ID_VENUE_PREDICATE)
        val UrlPredicate = ThingId(ID_URL_PREDICATE)
        val ContributionClass = ThingId(ID_CONTRIBUTION_CLASS)
        val AuthorClass = ThingId(ID_AUTHOR_CLASS)
        val VenueClass = ThingId(ID_VENUE_CLASS)
    }

    // </editor-fold>

    private fun List<String>?.toThingIds(): Set<ThingId> = this?.map { ThingId(it) }?.toSet() ?: emptySet()
}
