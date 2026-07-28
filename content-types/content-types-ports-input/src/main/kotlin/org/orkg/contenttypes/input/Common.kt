package org.orkg.contenttypes.input

import org.orkg.common.IRI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals

data class PublicationInfoCommand(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: IRI?,
)

sealed interface CreateThingsCommand {
    val resources: Map<String, CreateResourceCommandPart>
    val literals: Map<String, CreateLiteralCommandPart>
    val predicates: Map<String, CreatePredicateCommandPart>
    val classes: Map<String, CreateClassCommandPart>
    val lists: Map<String, CreateListCommandPart>

    fun all(): Map<String, CreateThingCommandPart> =
        resources + literals + predicates + classes + lists

    fun tempIds(): List<String> =
        listOf(resources.keys, literals.keys, predicates.keys, classes.keys, lists.keys).flatten()

    fun uris(): List<IRI> =
        listOf(
            resources.values.mapNotNull { it.uri },
            predicates.values.mapNotNull { it.uri },
            classes.values.mapNotNull { it.uri },
            lists.values.mapNotNull { it.uri },
        ).flatten()
}

sealed interface CreateThingCommandPart {
    val label: String
}

data class CreateResourceCommandPart(
    override val label: String,
    val classes: Set<ThingId> = emptySet(),
    val uri: IRI? = null,
) : CreateThingCommandPart

data class CreateClassCommandPart(
    override val label: String,
    val uri: IRI? = null,
) : CreateThingCommandPart

data class CreateListCommandPart(
    override val label: String,
    val elements: List<String> = emptyList(),
    val uri: IRI? = null,
) : CreateThingCommandPart

data class CreateLiteralCommandPart(
    override val label: String,
    val dataType: String = Literals.XSD.STRING.prefixedUri,
) : CreateThingCommandPart

data class CreatePredicateCommandPart(
    override val label: String,
    val description: String? = null,
    val uri: IRI? = null,
) : CreateThingCommandPart

data class CreateContributionCommandPart(
    val label: String,
    val classes: Set<ThingId> = emptySet(),
    val statements: Map<String, List<StatementObject>>,
) {
    data class StatementObject(
        val id: String,
        val statements: Map<String, List<StatementObject>>? = null,
    )
}
