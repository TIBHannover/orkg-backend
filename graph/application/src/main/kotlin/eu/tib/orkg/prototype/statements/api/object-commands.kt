package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import kotlin.collections.HashMap

const val RESOURCE_EXISTING_KEY = "resource"
const val LITERAL_EXISTING_KEY = "literal"
const val PREDICATE_EXISTING_KEY = "predicate"
const val CLASS_EXISTING_KEY = "class"

interface CreateObjectUseCase {

    fun createObject(
        request: CreateObjectRequest,
        existingThingId: ThingId? = null,
        userUUID: UUID,
    ): ThingId

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
        val subject: ThingId,
        val predicate: ThingId,
        val `object`: String
    )
}
