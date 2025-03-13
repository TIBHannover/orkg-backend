package org.orkg.contenttypes.input

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals

data class PublicationInfoCommand(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: ParsedIRI?,
)

sealed interface CreateThingsCommand {
    val resources: Map<String, CreateResourceCommandPart>
    val literals: Map<String, CreateLiteralCommandPart>
    val predicates: Map<String, CreatePredicateCommandPart>
    val classes: Map<String, CreateClassCommandPart>
    val lists: Map<String, CreateListCommandPart>

    fun all(): Map<String, CreateThingCommandPart> =
        resources + literals + predicates + classes + lists
}

sealed interface CreateThingCommandPart {
    val label: String
}

data class CreateResourceCommandPart(
    override val label: String,
    val classes: Set<ThingId> = emptySet(),
) : CreateThingCommandPart

data class CreateClassCommandPart(
    override val label: String,
    val uri: ParsedIRI? = null,
) : CreateThingCommandPart

data class CreateListCommandPart(
    override val label: String,
    val elements: List<String> = emptyList(),
) : CreateThingCommandPart

data class CreateLiteralCommandPart(
    override val label: String,
    val dataType: String = Literals.XSD.STRING.prefixedUri,
) : CreateThingCommandPart

data class CreatePredicateCommandPart(
    override val label: String,
    val description: String? = null,
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
