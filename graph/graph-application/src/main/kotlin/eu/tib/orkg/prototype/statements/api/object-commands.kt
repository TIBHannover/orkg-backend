package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import kotlin.collections.HashMap


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
            !this.predicates.isNullOrEmpty()
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
            !this.values.isNullOrEmpty()

        /**
         * Check if the resource is typed
         * i.e., it has classes
         */
        fun isTyped() =
            !this.classes.isNullOrEmpty()
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
         * Indicate if the entity is an existing thing
         * i.e., the @id property is used in the json object
         */
        fun isExisting() =
            this.`@id` != null

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
            !this.classes.isNullOrEmpty()

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
            !this.values.isNullOrEmpty()
    }

    data class TempResource(
        val subject: ThingId,
        val predicate: ThingId,
        val `object`: String
    )
}
