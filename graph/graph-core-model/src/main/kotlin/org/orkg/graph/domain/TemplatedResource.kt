package org.orkg.graph.domain

import org.orkg.common.ThingId
import kotlin.collections.List

/**
 * Class representing the components to build the formatted label
 * for a resource that is typed with a class (created from template)
 */
data class TemplatedResource(
    /**
     * The id of the resource
     */
    val id: ThingId,
    /**
     * The id of the resource used on this resource
     */
    val templateId: ThingId,
    /**
     * The label of the template applied on this resource
     */
    val label: String,
    /**
     * The class of the resource that is typed based on a template
     */
    val classId: String,
    /**
     * The formatting string; used to create the final formatted label
     * This string contains placeholders for property IDs that should be replaced by values
     */
    val format: String,
    /**
     * A list of predicate IDs that the resource has on the top level
     */
    val predicates: List<String>,
    /**
     * In the same order of the predicates list;
     * A list of values that corresponds to the labels of the objects of each predicate
     */
    val values: List<String>,
) {
    /**
     * A map created from combining predicates and values; used for placeholder lookup
     */
    private val components: Map<String, String>
        get() = this.predicates.zip(this.values).associate { it }

    /**
     * Composes the formatted label from the given format rule
     * and the set of property/value pairs
     */
    fun composeFormattedLabel(): String {
        val pattern = """\{[a-zA-Z0-9:_-]+}""".toRegex()
        val matches = pattern.findAll(format)
        var formattedString = format
        matches.forEach {
            val predId = format.substring(
                startIndex = it.groups.first()!!.range.first + 1,
                endIndex = it.groups.first()!!.range.last
            )
            if (components.containsKey(predId)) {
                formattedString =
                    formattedString.replaceFirst(
                        "{$predId}",
                        components[predId]
                            ?: error("Format pattern contains predicate {$predId}, that doesn't exists in the statements")
                    )
            }
        }
        return formattedString
    }
}
