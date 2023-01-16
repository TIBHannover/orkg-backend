package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.ObjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val RESOURCE_EXISTING_KEY = "resource"
const val LITERAL_EXISTING_KEY = "literal"
const val PREDICATE_EXISTING_KEY = "predicate"
const val CLASS_EXISTING_KEY = "class"

@RestController
@RequestMapping("/api/objects/")
class ObjectController(
    private val resourceService: ResourceUseCases,
    private val service: ObjectService,
) : BaseController() {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        val resource = service.createObject(obj, null, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable id: ResourceId,
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        resourceService
            .findById(id)
            .orElseThrow { ResourceNotFound() }
        val resource = service.createObject(obj, id, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }
}

// <editor-fold desc="Data Classes">

data class CreateObjectRequest(
    val predicates: List<HashMap<String, String>>?,
    val resource: NamedObject
) {
    /**
     * Check if the object has a set
     * of predicates to be processed
     */
    fun hasTempPredicates() =
        this.predicates != null && this.predicates.isNotEmpty()
}

data class NamedObject(
    val name: String,
    val classes: List<String>?,
    val values: HashMap<String, List<ObjectStatement>>?,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization()

) {
    /**
     * Check if the object has a set
     * of statements to be added recursively
     */
    fun hasSubsequentStatements() =
        this.values != null && this.values.isNotEmpty()

    /**
     * Check if the resource is typed
     * i.e., it has classes
     */
    fun isTyped() =
        this.classes != null && this.classes.isNotEmpty()
}

data class ObjectStatement(
    val `@id`: String?,
    val `@type`: String?,
    val classes: List<String>?,
    val `@temp`: String?,
    val text: String?,
    val datatype: String?,
    val label: String?,
    val values: HashMap<String, List<ObjectStatement>>?
) {

    /**
     * Indicate if the entity is an existing thing
     * i.e., the @id property is used in the json object
     */
    fun isExisting() =
        this.`@id` != null

    /**
     * Check if an entity is existing and explicitly typed.
     * Allowed types are: [class, resource, predicate, literal]
     */
    fun isTypedExisting() =
        this.isExisting() && this.`@type` != null

    /**
     * Check if the entity is existing as a resource
     * If it is typed with resource
     * o/w if the id starts with an R
     */
    fun isExistingResource() =
        this.isExisting() && when (this.`@type`) {
            null -> this.`@id`!!.startsWith("R")
            RESOURCE_EXISTING_KEY -> true
            else -> false
        }

    /**
     * Check if the entity is existing as a literal
     * If it is typed with literal
     * o/w if the id starts with an L
     */
    fun isExistingLiteral() =
        this.isExisting() && when (this.`@type`) {
            null -> this.`@id`!!.startsWith("L")
            LITERAL_EXISTING_KEY -> true
            else -> false
        }

    /**
     * Check if the entity is existing as a class
     * If it is typed with class
     */
    fun isExistingClass() =
        this.isTypedExisting() && this.`@type` == CLASS_EXISTING_KEY

    /**
     * Check if the entity is existing as a predicate
     * If it is typed with predicate
     */
    fun isExistingPredicate() =
        this.isTypedExisting() && this.`@type` == PREDICATE_EXISTING_KEY

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
        this.values != null && this.values.isNotEmpty()
}

data class TempResource(
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: String
)

// </editor-fold>
