package org.orkg.graph.domain

import java.util.*
import kotlin.collections.List
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateObjectUseCase
import org.orkg.graph.input.CreateObjectUseCase.CreateObjectRequest
import org.orkg.graph.input.CreateObjectUseCase.ObjectStatement
import org.orkg.graph.input.CreateObjectUseCase.TempResource
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.stereotype.Service

@Service
class ObjectService(
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementService: StatementUseCases,
    private val classService: ClassUseCases,
    private val listService: ListUseCases,
    private val thingService: ThingService,
    private val contributorService: RetrieveContributorUseCase,
) : CreateObjectUseCase {

    /**
     * Creates an object into the ORKG
     * and object here is the term, like a json-object
     * and not as an object in the RDF-statement.
     * This object allows for the flexibility of adding sub-graphs into
     * the ORKG that are not rooted in a paper.
     */
    override fun createObject(
        request: CreateObjectRequest,
        existingThingId: ThingId?,
        userUUID: UUID,
    ): ThingId {
        // Get provenance info
        val userId = ContributorId(userUUID)
        val contributor = contributorService.findById(userId).orElse(Contributor.UNKNOWN)
        val organizationId: OrganizationId
        val observatoryId: ObservatoryId
        // if comparisons is assigned a conference i.e, organizationId will be available and observatoryId  will not be.
        if (request.resource.organizationId != OrganizationId.UNKNOWN && request.resource.observatoryId == ObservatoryId.UNKNOWN) {
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
                val predicateId = predicateService.create(
                    CreatePredicateUseCase.CreateCommand(
                        label = it.keys.first(),
                        contributorId = userId
                    )
                )
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
                CreateResourceUseCase.CreateCommand(
                    label = request.resource.name,
                    classes = request.resource.classes.toThingIds(),
                    extractionMethod = request.resource.extractionMethod,
                    contributorId = userId,
                    observatoryId = observatoryId,
                    organizationId = organizationId,
                )
            )

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
        return resourceId
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
                if (jsonObject.isExisting() && !jsonObject.isTempResource()) // Add an existing entity
                    checkIfThingExists(ThingId(jsonObject.`@id`!!))
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
                        if (!jsonObject.isTempResource())
                            statementService.add(userId, subject, predicateId!!, ThingId(jsonObject.`@id`!!))
                        else {
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
                    jsonObject.isNewLiteral() -> { // create new literal
                        val newLiteral = literalService.create(
                            CreateCommand(
                                contributorId = userId,
                                label = jsonObject.text!!,
                                datatype = jsonObject.datatype ?: "xsd:string"
                            )
                        )
                        if (jsonObject.`@temp` != null) {
                            tempResources[jsonObject.`@temp`!!] = newLiteral
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
                        val newResource = resourceService.create(
                            CreateResourceUseCase.CreateCommand(
                                label = jsonObject.label!!,
                                classes = classes.toSet(),
                                contributorId = userId,
                                extractionMethod = extractionMethod,
                                observatoryId = observatoryId,
                                organizationId = organizationId,
                            )
                        )
                        if (jsonObject.`@temp` != null) {
                            tempResources[jsonObject.`@temp`!!] = newResource
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
     * Check if a predicate is existing
     * o/w throw out a suitable exception
     */
    private fun checkIfPredicateExists(predicate: String) {
        if (!predicateService.exists(ThingId(predicate))) throw PredicateNotFound(predicate)
    }

    /**
     * Check if a class exists, otherwise throw out suitable exception.
     */
    private fun checkIfClassExists(it: String) {
        if (!classService.exists(ThingId(it))) throw ClassNotFound.withId(it)
    }

    private fun checkIfThingExists(id: ThingId) {
        if (!thingService.exists(id))
            throw ThingNotFound(id)
    }

    /**
     * Check if a list exists, otherwise throw out suitable exception.
     */
    private fun checkIfListExists(listId: String) {
        val id = ThingId(listId)
        if (!listService.exists(id)) throw ListNotFound(id)
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
        // Miscellaneous
        val MAP_PREDICATE_CLASSES = mapOf("P32" to "Problem")
    }

    // </editor-fold>

    private fun List<String>?.toThingIds(): Set<ThingId> = this?.map { ThingId(it) }?.toSet() ?: emptySet()
}
